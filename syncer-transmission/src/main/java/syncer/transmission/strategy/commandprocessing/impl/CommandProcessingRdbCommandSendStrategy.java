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
import org.sqlite.core.DB;
import syncer.common.util.TimeUtils;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.Event;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.iter.datatype.*;
import syncer.replica.event.start.PreRdbSyncEvent;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.replica.status.TaskStatus;
import syncer.replica.type.FileType;
import syncer.replica.util.TaskRunTypeEnum;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.constants.RedisCommandTypeEnum;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.util.RedisCommandTypeUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.Date;

/**
 * @author zhanenqiang
 * @Description 存量命令写入
 * @Date 2020/12/23
 */
@Builder
@Slf4j
@Getter
@Setter
public class CommandProcessingRdbCommandSendStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;
    private double redisVersion;
    private Date date;
    private TaskModel taskModel;

    public CommandProcessingRdbCommandSendStrategy(CommonProcessingStrategy next, RedisClient client, String taskId, double redisVersion, Date date,TaskModel taskModel) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.redisVersion = redisVersion;
        this.date = new Date();
        this.taskModel=taskModel;
    }


    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        try {
            Event event=eventEntity.getEvent();
            //全量同步开始
            ISyncerCompensator iSyncerCompensator=eventEntity.getISyncerCompensator();
            if (event instanceof PreRdbSyncEvent) {
                log.warn("taskId为[{}]的全量数据到达同步程序同步开始..，当前时间：{}",taskId, TimeUtils.getNowTimeString());
                if(eventEntity.getFileType().equals(FileType.ONLINERDB)
                        ||eventEntity.getFileType().equals(FileType.RDB)
                        ||eventEntity.getFileType().equals(FileType.ONLINEAOF)
                        ||eventEntity.getFileType().equals(FileType.AOF)
                        ||eventEntity.getFileType().equals(FileType.ONLINEMIXED)
                        ||eventEntity.getFileType().equals(FileType.MIXED)){
                    log.warn("taskId为[{}]的文件任务全量同步开始..",taskId);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"文件全量同步开始[同步任务启动]");
//                    SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "文件全量同步开始[同步任务启动]", TaskStatusType.RDBRUNING);
                }else{
                    log.warn("taskId为[{}]的任务全量同步开始..",taskId);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"全量同步开始[同步任务启动]");
//                    SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "全量同步开始[同步任务启动]",TaskStatusType.RDBRUNING);
                }
            }

            //全量同步结束
            if (event instanceof PostRdbSyncEvent) {
                String time=(System.currentTimeMillis()-date.getTime())/(1000)+":s";
                if(eventEntity.getFileType().equals(FileType.ONLINERDB)
                        ||eventEntity.getFileType().equals(FileType.RDB)
                        ||eventEntity.getFileType().equals(FileType.ONLINEAOF)
                        ||eventEntity.getFileType().equals(FileType.AOF)
                        ||eventEntity.getFileType().equals(FileType.ONLINEMIXED)
                        ||eventEntity.getFileType().equals(FileType.MIXED)){
                    log.warn("taskId为[{}]的文件任务全量同步结束[任务完成]..时间为:"+time,taskId);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"文件同步结束[任务完成] 时间(ms)："+time);
//                    SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "文件同步结束[任务完成] 时间(ms)："+time,TaskStatusType.STOP);
                }else {
                    if(eventEntity.getTaskRunTypeEnum().equals(TaskRunTypeEnum.TOTAL)){
                        log.warn("taskId为[{}]的任务全量同步结束..进入增量同步模式 time:[{}] ",taskId,time);
                        SingleTaskDataManagerUtils.updateThreadMsg(taskId,"全量同步结束进入增量同步 时间(ms)："+time+" 进入增量状态");
//                        SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "全量同步结束进入增量同步 时间(ms)："+time+" 进入增量状态",TaskStatusType.COMMANDRUNING);
                    }else if(eventEntity.getTaskRunTypeEnum().equals(TaskRunTypeEnum.STOCKONLY)){
                        log.warn("taskId为[{}]的任务全量同步结束[任务完成]..",taskId);
                        SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "全量同步结束[任务完成] 时间(ms)："+time, TaskStatus.STOP);
//                        SingleTaskDataManagerUtils.updateThreadMsg(taskId,"全量同步结束[任务完成] 时间(ms)："+time);
                    }
                }
                return;
            }

            if (event instanceof BatchedKeyValuePairEvent<?, ?>) {
                BatchedKeyValuePairEvent batchedKeyValuePair = (BatchedKeyValuePairEvent) event;
                RedisDB db=batchedKeyValuePair.getDb();
                Long duNum=db.getCurrentDbNumber();
                Long ms=eventEntity.getMs();
                RedisCommandTypeEnum typeEnum= RedisCommandTypeUtils.getRedisCommandTypeEnum(batchedKeyValuePair.getValueRdbType());
                if(batchedKeyValuePair.getBatch()==0&&null==batchedKeyValuePair.getValue()){
                    return;
                }

                //String类型
                if(typeEnum.equals(RedisCommandTypeEnum.STRING)){

                    BatchedKeyStringValueStringEvent valueString = (BatchedKeyStringValueStringEvent) event;
                    if (ms == null || ms <= 0L) {
                        Long res=client.append(duNum,valueString.getKey(), valueString.getValue());
                        iSyncerCompensator.append(duNum,valueString.getKey(), valueString.getValue(),res);
                    }else {
                        Long res=client.append(duNum,valueString.getKey(), valueString.getValue());
                        iSyncerCompensator.append(duNum,valueString.getKey(), valueString.getValue(),res);
                    }
                }else if(typeEnum.equals(RedisCommandTypeEnum.LIST)){
                    //list类型
                    BatchedKeyStringValueListEvent valueList = (BatchedKeyStringValueListEvent) event;
                    if (ms == null || ms <= 0L) {
                        Long res=client.rpush(duNum,valueList.getKey(), valueList.getValue());
                        iSyncerCompensator.rpush(duNum,valueList.getKey(), valueList.getValue(),res);
                    }else {
                        Long res= client.rpush(duNum,valueList.getKey(),ms, valueList.getValue());
                        iSyncerCompensator.rpush(duNum,valueList.getKey(), ms,valueList.getValue(),res);
                    }
                }else if(typeEnum.equals(RedisCommandTypeEnum.SET)){

                    //set类型
                    BatchedKeyStringValueSetEvent valueSet = (BatchedKeyStringValueSetEvent) event;
                    if (ms == null || ms<=  0L) {
                        Long res= client.sadd(duNum,valueSet.getKey(), valueSet.getValue());
                        iSyncerCompensator.sadd(duNum,valueSet.getKey(), valueSet.getValue(),res);
                    }else {
                        Long res= client.sadd(duNum,valueSet.getKey(),ms, valueSet.getValue());
                        iSyncerCompensator.sadd(duNum,valueSet.getKey(), ms,valueSet.getValue(),res);
                    }
                }else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                    //zset类型
                    BatchedKeyStringValueZSetEvent valueZSet = (BatchedKeyStringValueZSetEvent) event;
                    if (ms == null || ms <=  0L) {
                        Long res=client.zadd(duNum,valueZSet.getKey(), valueZSet.getValue());
                        iSyncerCompensator.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),res);
                    }else {
                        Long res=client.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),ms);
                        iSyncerCompensator.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),ms,res);
                    }
                }else if(typeEnum.equals(RedisCommandTypeEnum.HASH)){
                    //hash类型
                    BatchedKeyStringValueHashEvent valueHash = (BatchedKeyStringValueHashEvent) event;
                    if (ms == null || ms <= 0L) {
                        String res=client.hmset(duNum,valueHash.getKey(), valueHash.getValue());
                        iSyncerCompensator.hmset(duNum,valueHash.getKey(), valueHash.getValue(),res);
                    }else {
                        String res= client.hmset(duNum,valueHash.getKey(), valueHash.getValue(),ms);
                        iSyncerCompensator.hmset(duNum,valueHash.getKey(), valueHash.getValue(),ms,res);
                    }

                }
            }



            if (event instanceof DumpKeyValuePairEvent) {
                DumpKeyValuePairEvent valueDump = (DumpKeyValuePairEvent) event;
                Long ms=eventEntity.getMs();
                RedisDB db=valueDump.getDb();
                Long duNum=db.getCurrentDbNumber();
                long ttl=ms;
                if (valueDump.getValue() != null) {
                    if(null==ms||ms<=0L){
                        ttl=0L;
                    }
                    if (redisVersion< 3.0) {
                        String res=client.restoreReplace(duNum,valueDump.getKey(), ttl, valueDump.getValue(),false);
                        iSyncerCompensator.restoreReplace(duNum,valueDump.getKey(), ttl, valueDump.getValue(),false,res);
                    } else {
                        String res=client.restoreReplace(duNum,valueDump.getKey(), ttl, valueDump.getValue());
                        iSyncerCompensator.restoreReplace(duNum,valueDump.getKey(), ttl, valueDump.getValue(),res);
                    }
                    /**
                     if (ms == null || ms <= 0L) {
                     if (redisVersion< 3.0) {
                     String res=client.restoreReplace(duNum,valueDump.getKey(), 0, valueDump.getValue(),false);
                     iSyncerCompensator.restoreReplace(duNum,valueDump.getKey(), 0, valueDump.getValue(),false,res);
                     } else {
                     String res=client.restoreReplace(duNum,valueDump.getKey(), 0, valueDump.getValue());
                     iSyncerCompensator.restoreReplace(duNum,valueDump.getKey(), 0, valueDump.getValue(),res);
                     }
                     }else {
                     if (redisVersion< 3.0) {
                     String res= client.restoreReplace(duNum,valueDump.getKey(),  ttl, valueDump.getValue(),false);
                     iSyncerCompensator.restoreReplace(duNum,valueDump.getKey(),  ttl, valueDump.getValue(),false,res);
                     } else {
                     String res=client.restoreReplace(duNum,valueDump.getKey(), ttl, valueDump.getValue());
                     iSyncerCompensator.restoreReplace(duNum,valueDump.getKey(), ttl, valueDump.getValue(),res);
                     }
                     }
                     **/
                }
            }

            //继续执行下一Filter节点
            toNext(replication,eventEntity,taskModel);
        }catch (Exception e){
            throw new StartegyNodeException(e.getMessage()+"->RdbCommandSendStrategy",e.getCause());
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

}
