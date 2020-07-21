package syncer.syncerservice.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.constant.RedisCommandTypeEnum;
import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.PostRdbSyncEvent;
import syncer.syncerplusredis.event.PreRdbSyncEvent;
import syncer.syncerplusredis.rdb.datatype.DB;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.*;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TimeUtils;
import syncer.syncerservice.compensator.ISyncerCompensator;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.RedisCommandTypeUtils;

import java.util.Date;


/**
 * 命令全量命令解析Filter
 */
@Builder
@Slf4j
@Getter
@Setter
public class KeyValueRdbSyncEventFilter implements CommonFilter {
    private CommonFilter next;
    private JDRedisClient client;
    private String taskId;
    private double redisVersion;
    private Date date;


    public KeyValueRdbSyncEventFilter(CommonFilter next, JDRedisClient client, String taskId, double redisVersion, Date date) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.redisVersion = redisVersion;
        this.date = new Date();
    }
    

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {

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
                TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "文件全量同步开始[同步任务启动]",TaskStatusType.RDBRUNING);

            }else{
                log.warn("taskId为[{}]的任务全量同步开始..",taskId);

                TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "全量同步开始[同步任务启动]",TaskStatusType.RDBRUNING);
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

                TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "文件同步结束[任务完成] 时间(ms)："+time,TaskStatusType.STOP);

            }else {

                if(eventEntity.getTaskRunTypeEnum().equals(TaskRunTypeEnum.TOTAL)){

                    log.warn("taskId为[{}]的任务全量同步结束..进入增量同步模式 time:[{}] ",taskId,time);
                    TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "全量同步结束进入增量同步 时间(ms)："+time+" 进入增量状态",TaskStatusType.COMMANDRUNING);


                }else if(eventEntity.getTaskRunTypeEnum().equals(TaskRunTypeEnum.STOCKONLY)){
                    log.warn("taskId为[{}]的任务全量同步结束[任务完成]..",taskId);
                    TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "全量同步结束[任务完成] 时间(ms)："+time,TaskStatusType.STOP);
                }
            }



                   return;
        }

        if (event instanceof BatchedKeyValuePair<?, ?>) {
            BatchedKeyValuePair batchedKeyValuePair = (BatchedKeyValuePair) event;
            DB db=batchedKeyValuePair.getDb();
            Long duNum=db.getDbNumber();
            Long ms=eventEntity.getMs();
            RedisCommandTypeEnum typeEnum= RedisCommandTypeUtils.getRedisCommandTypeEnum(batchedKeyValuePair.getValueRdbType());

            if(batchedKeyValuePair.getBatch()==0&&null==batchedKeyValuePair.getValue()){
                return;
            }

            //String类型
            if(typeEnum.equals(RedisCommandTypeEnum.STRING)){

                BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                if (ms == null || ms <= 0L) {

                    Long res=client.append(duNum,valueString.getKey(), valueString.getValue());
                    iSyncerCompensator.append(duNum,valueString.getKey(), valueString.getValue(),res);

                }else {
                    Long res=client.append(duNum,valueString.getKey(), valueString.getValue());
                    iSyncerCompensator.append(duNum,valueString.getKey(), valueString.getValue(),res);

                }


            }else if(typeEnum.equals(RedisCommandTypeEnum.LIST)){

                //list类型
                BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                if (ms == null || ms <= 0L) {
                    Long res=client.rpush(duNum,valueList.getKey(), valueList.getValue());
                    iSyncerCompensator.rpush(duNum,valueList.getKey(), valueList.getValue(),res);
                }else {
                    Long res= client.rpush(duNum,valueList.getKey(),ms, valueList.getValue());
                    iSyncerCompensator.rpush(duNum,valueList.getKey(), ms,valueList.getValue(),res);
                }


            }else if(typeEnum.equals(RedisCommandTypeEnum.SET)){

                //set类型
                BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                if (ms == null || ms<=  0L) {
                    Long res= client.sadd(duNum,valueSet.getKey(), valueSet.getValue());
                    iSyncerCompensator.sadd(duNum,valueSet.getKey(), valueSet.getValue(),res);
                }else {
                    Long res= client.sadd(duNum,valueSet.getKey(),ms, valueSet.getValue());
                    iSyncerCompensator.sadd(duNum,valueSet.getKey(), ms,valueSet.getValue(),res);
                }

            }else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                //zset类型

                BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                if (ms == null || ms <=  0L) {
                    Long res=client.zadd(duNum,valueZSet.getKey(), valueZSet.getValue());
                    iSyncerCompensator.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),res);
                }else {
                    Long res=client.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),ms);
                    iSyncerCompensator.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),ms,res);
                }

            }else if(typeEnum.equals(RedisCommandTypeEnum.HASH)){
                //hash类型

                BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;
                if (ms == null || ms <= 0L) {
                    String res=client.hmset(duNum,valueHash.getKey(), valueHash.getValue());
                    iSyncerCompensator.hmset(duNum,valueHash.getKey(), valueHash.getValue(),res);
                }else {
                    String res= client.hmset(duNum,valueHash.getKey(), valueHash.getValue(),ms);
                    iSyncerCompensator.hmset(duNum,valueHash.getKey(), valueHash.getValue(),ms,res);
                }

            }
        }



        if (event instanceof DumpKeyValuePair) {
            DumpKeyValuePair valueDump = (DumpKeyValuePair) event;
            Long ms=eventEntity.getMs();
            DB db=valueDump.getDb();
            Long duNum=db.getDbNumber();
            long ttl=ms;
            if (valueDump.getValue() != null) {
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
            }
        }

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);
        }catch (Exception e){
            throw new FilterNodeException(e.getMessage()+"->KeyValueRdbSyncEventFilter",e.getCause());
        }
    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {
        if(null!=next){
            next.run(replicator,eventEntity);
        }

    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }
}
