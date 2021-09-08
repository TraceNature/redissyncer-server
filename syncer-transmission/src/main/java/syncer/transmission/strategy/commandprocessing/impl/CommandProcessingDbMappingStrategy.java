// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.strategy.commandprocessing.impl;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.Event;
import syncer.replica.event.iter.datatype.*;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.transmission.client.RedisClient;
import syncer.transmission.exception.KeyWeed0utException;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.Arrays;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/23
 */
@Builder
@Getter
@Setter
@Slf4j

public class CommandProcessingDbMappingStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;
    private TaskModel taskModel;
    private Integer dbNum = 0;

    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        try {


            //DUMP格式数据
            Event event = eventEntity.getEvent();
            if (event instanceof DumpKeyValuePairEvent) {
                DumpKeyValuePairEvent dumpKeyValuePair = (DumpKeyValuePairEvent) event;

                if (null == dumpKeyValuePair.getValue() || null == dumpKeyValuePair.getKey()) {
                    return;
                }
                RedisDB db = dumpKeyValuePair.getDb();
                RedisDB newDb = new RedisDB();
                BeanUtils.copyProperties(db, newDb);
                try {
                    dbMapping(eventEntity, newDb);
                } catch (KeyWeed0utException e) {
                    log.debug("全量数据key[{}]不符合DB映射规则，被抛弃..", JSON.toJSONString(eventEntity));
                    //抛弃此kv
                    return;
                }

                SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getRealKeyCount().incrementAndGet();
            }

            //分批格式数据
            if (event instanceof BatchedKeyValuePairEvent<?, ?>) {
                BatchedKeyValuePairEvent batchedKeyValuePair = (BatchedKeyValuePairEvent) event;
                if ((batchedKeyValuePair.getBatch() == 0 && null == batchedKeyValuePair.getValue()) || null == batchedKeyValuePair.getValue()) {
                    return;
                }
                SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getRealKeyCount().incrementAndGet();
                RedisDB db = batchedKeyValuePair.getDb();
                RedisDB newDb = new RedisDB();
                BeanUtils.copyProperties(db, newDb);
                try {
                    dbMapping(eventEntity, newDb);
                } catch (KeyWeed0utException e) {
                    log.debug("全量数据key[{}]不符合DB映射规则，被抛弃..", JSON.toJSONString(eventEntity));
                    //抛弃此kv
                    return;
                }
            }

            //增量数据
            if (event instanceof DefaultCommand) {
                DefaultCommand defaultCommand = (DefaultCommand) event;
                if (Arrays.equals(defaultCommand.getCommand(), "SELECT".getBytes())) {
                    int commDbNum = Integer.parseInt(new String(defaultCommand.getArgs()[0]));
                    dbNum = commDbNum;
                    try {
                        commanddbMapping(eventEntity, commDbNum, defaultCommand);
                    } catch (KeyWeed0utException e) {
                        //抛弃此kv
                        log.debug("增量数据key[{}]不符合DB映射规则，被抛弃..", JSON.toJSONString(eventEntity));
                        return;
                    }
                } else {
                    try {
                        commanddbMapping(eventEntity, dbNum);
                    } catch (KeyWeed0utException e) {
                        //抛弃此kv
                        log.debug("增量数据key[{}]不符合DB映射规则，被抛弃.. 原因[{}]", JSON.toJSONString(eventEntity), e.getMessage());
                        return;
                    }
                }

                SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getRealKeyCount().incrementAndGet();
            }


            //继续执行下一Filter节点
            toNext(replication, eventEntity,taskModel);
        } catch (Exception e) {
            throw new StartegyNodeException(e.getMessage() + "->DbMappingStrategy", e.getCause());
        }
    }

    @Override
    public void toNext(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        if (null != next) {
            next.run(replication, eventEntity,taskModel);
        }
    }

    @Override
    public void setNext(CommonProcessingStrategy nextStrategy) {
        this.next = nextStrategy;
    }

    /**
     * 全量KV DBNUM映射
     *
     * @param eventEntity
     * @param db
     * @throws KeyWeed0utException
     */
    void dbMapping(KeyValueEventEntity eventEntity, RedisDB db) throws KeyWeed0utException {
        Event event = eventEntity.getEvent();
        long dbbnum = db.getCurrentDbNumber();
        int dbNumInt = Math.toIntExact(db.getCurrentDbNumber());

        if (null != eventEntity.getDbMapper() && eventEntity.getDbMapper().size() > 0) {
            if (eventEntity.getDbMapper().containsKey(dbNumInt)) {
                dbbnum = eventEntity.getDbMapper().get(dbNumInt);
            } else {
                //忽略本key
                throw new KeyWeed0utException("key抛弃");
            }
        }

        if (event instanceof DumpKeyValuePairEvent) {
            DumpKeyValuePairEvent dumpKeyValuePair = (DumpKeyValuePairEvent) event;
            db.setCurrentDbNumber(dbbnum);
            dumpKeyValuePair.setDb(db);
            eventEntity.setEvent(dumpKeyValuePair);
        } else if (event instanceof BatchedKeyValuePairEvent<?, ?>) {
            BatchedKeyValuePairEvent batchedKeyValuePair = (BatchedKeyValuePairEvent) event;
            db.setCurrentDbNumber(dbbnum);
            batchedKeyValuePair.setDb(db);
            eventEntity.setEvent(batchedKeyValuePair);
        } else if (event instanceof BatchedKeyStringValueHashEvent) {
            BatchedKeyStringValueHashEvent stringValueHashEvent = (BatchedKeyStringValueHashEvent) event;
            db.setCurrentDbNumber(dbbnum);
            stringValueHashEvent.setDb(db);
            eventEntity.setEvent(stringValueHashEvent);
        }
        eventEntity.setDbNum(dbbnum);

    }

    /**
     * 增量KV DBNUM映射
     *
     * @param eventEntity
     * @param correntDbNum
     * @throws KeyWeed0utException
     */
    void commanddbMapping(KeyValueEventEntity eventEntity, Integer correntDbNum, DefaultCommand command) throws KeyWeed0utException {

        if (null != eventEntity.getDbMapper() && eventEntity.getDbMapper().size() > 0) {
            if (eventEntity.getDbMapper().containsKey(correntDbNum)) {
                Integer newNum = eventEntity.getDbMapper().get(correntDbNum);
                byte[][] dbData = command.getArgs();
                dbData[0] = String.valueOf(newNum).getBytes();
                command.setArgs(dbData);
            } else {
                //忽略本key
                throw new KeyWeed0utException("key抛弃");
            }
        }
        eventEntity.setEvent(command);
    }

    void commanddbMapping(KeyValueEventEntity eventEntity, Integer correntDbNum) throws KeyWeed0utException {
        if (null != eventEntity.getDbMapper() && eventEntity.getDbMapper().size() > 0) {
            if (!eventEntity.getDbMapper().containsKey(correntDbNum)) {
                //忽略本key
                throw new KeyWeed0utException("key抛弃");
            }
        }
    }
}
