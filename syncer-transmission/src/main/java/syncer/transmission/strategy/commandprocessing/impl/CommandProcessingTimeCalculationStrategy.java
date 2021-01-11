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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.event.Event;
import syncer.replica.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.replica.rdb.sync.datatype.DumpKeyValuePair;
import syncer.replica.replication.Replication;
import syncer.transmission.client.RedisClient;
import syncer.transmission.exception.KeyWeed0utException;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;

import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 全量kv剩余过期时间计算节点
 * @Date 2020/12/22
 */
@Builder
@Getter
@Setter
@Slf4j
public class CommandProcessingTimeCalculationStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;


    public CommandProcessingTimeCalculationStrategy(CommonProcessingStrategy next, RedisClient client, String taskId) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
    }

    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity) throws StartegyNodeException {
        try{
            Event event=eventEntity.getEvent();
            if (event instanceof DumpKeyValuePair) {
                DumpKeyValuePair dumpKeyValuePair= (DumpKeyValuePair) event;
                Long time=dumpKeyValuePair.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }


            if (event instanceof BatchedKeyValuePair<?, ?>) {
                BatchedKeyValuePair batchedKeyValuePair = (BatchedKeyValuePair) event;
                if(batchedKeyValuePair.getBatch()==0&&null==batchedKeyValuePair.getValue()){
                    return;
                }
                Long time=batchedKeyValuePair.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }
            //继续执行下一Filter节点
            toNext(replication,eventEntity);
        }catch (Exception e){
            throw new StartegyNodeException(e.getMessage()+"->CommandProcessingTimeCalculationStrategy",e.getCause());
        }
    }

    @Override
    public void toNext(Replication replication, KeyValueEventEntity eventEntity) throws StartegyNodeException {
        if(null!=next){
            next.run(replication,eventEntity);
        }
    }

    @Override
    public void setNext(CommonProcessingStrategy nextStrategy) {
        this.next=nextStrategy;
    }

    void timeCalculation(KeyValueEventEntity eventEntity,Long time) throws KeyWeed0utException {
        Long ms=0L;
        if (Objects.isNull(time)) {
            ms = 0L;
        } else {
            ms = time - System.currentTimeMillis();
            if(ms<0L){
                //key已经过期 忽略本key
                throw new KeyWeed0utException("key过期被抛弃");
            }
        }
        eventEntity.setMs(ms);
    }
}
