package syncer.syncerservice.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.constant.RedisCommandTypeEnum;
import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.PostRdbSyncEvent;
import syncer.syncerplusredis.event.PreRdbSyncEvent;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.rdb.datatype.DB;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.*;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.RedisCommandTypeUtils;
import syncer.syncerservice.util.SyncTaskUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;


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

    public KeyValueRdbSyncEventFilter(CommonFilter next, JDRedisClient client, String taskId, double redisVersion) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.redisVersion = redisVersion;
    }

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) {
        Event event=eventEntity.getEvent();
        //全量同步开始



        if (event instanceof PreRdbSyncEvent) {
            if(eventEntity.getFileType().equals(FileType.ONLINERDB)
                    ||eventEntity.getFileType().equals(FileType.RDB)
                    ||eventEntity.getFileType().equals(FileType.ONLINEAOF)
                    ||eventEntity.getFileType().equals(FileType.AOF)
                    ||eventEntity.getFileType().equals(FileType.ONLINEMIXED)
                    ||eventEntity.getFileType().equals(FileType.MIXED)){

                log.warn("taskId为[{}]的文件任务全量同步开始..",taskId);
                SyncTaskUtils.editTaskMsg(taskId,"文件全量同步开始[同步任务启动]");
            }else{
                log.warn("taskId为[{}]的任务全量同步开始..",taskId);
                SyncTaskUtils.editTaskMsg(taskId,"全量同步开始[同步任务启动]");
            }


        }

        //全量同步结束
        if (event instanceof PostRdbSyncEvent) {

            if(eventEntity.getFileType().equals(FileType.ONLINERDB)
                    ||eventEntity.getFileType().equals(FileType.RDB)
                    ||eventEntity.getFileType().equals(FileType.ONLINEAOF)
                    ||eventEntity.getFileType().equals(FileType.AOF)
                    ||eventEntity.getFileType().equals(FileType.ONLINEMIXED)
                    ||eventEntity.getFileType().equals(FileType.MIXED)){
                log.warn("taskId为[{}]的文件任务全量同步结束[任务完成]..",taskId);

                SyncTaskUtils.editTaskMsg(taskId,"文件同步结束[任务完成] 时间(ms)：");
                SyncTaskUtils.stopCreateThread(taskId);
            }else {
                if(eventEntity.getTaskRunTypeEnum().equals(TaskRunTypeEnum.TOTAL)){
                    String time=(System.currentTimeMillis()-date.getTime())/(1000)+":s";
                    log.warn("taskId为[{}]的任务全量同步结束..进入增量同步模式 time:[{}] ",taskId,time);
                    SyncTaskUtils.editTaskMsg(taskId,"全量同步结束进入增量同步 时间(ms)： 进入增量状态");
                }else if(eventEntity.getTaskRunTypeEnum().equals(TaskRunTypeEnum.STOCKONLY)){
                    log.warn("taskId为[{}]的任务全量同步结束[任务完成]..",taskId);
                    SyncTaskUtils.editTaskMsg(taskId,"全量同步结束[任务完成] 时间(ms)：");
                    SyncTaskUtils.stopCreateThread(taskId);
                }
            }


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
                    if (valueString.getBatch() == 0) {
                        client.set(duNum,valueString.getKey(), valueString.getValue());
                    } else {
                        client.append(duNum,valueString.getKey(), valueString.getValue());
                    }
                }else {
                    if (valueString.getBatch() == 0) {
                        client.set(duNum,valueString.getKey(), valueString.getValue(),ms);
                    } else {
                        client.append(duNum,valueString.getKey(), valueString.getValue());
                    }
                }


            }else if(typeEnum.equals(RedisCommandTypeEnum.LIST)){

                //list类型
                BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                if (ms == null || ms <= 0L) {
                    client.lpush(duNum,valueList.getKey(), valueList.getValue());
                }else {
                    client.lpush(duNum,valueList.getKey(),ms, valueList.getValue());
                }


            }else if(typeEnum.equals(RedisCommandTypeEnum.SET)){

                //set类型
                BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                if (ms == null || ms<=  0L) {
                    client.sadd(duNum,valueSet.getKey(), valueSet.getValue());
                }else {
                    client.sadd(duNum,valueSet.getKey(),ms, valueSet.getValue());
                }

            }else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                //zset类型

                BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                if (ms == null || ms <=  0L) {
                    client.zadd(duNum,valueZSet.getKey(), valueZSet.getValue());
                }else {
                    client.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),ms);
                }

            }else if(typeEnum.equals(RedisCommandTypeEnum.HASH)){
                //hash类型

                BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;
                if (ms == null || ms <= 0L) {
                    client.hmset(duNum,valueHash.getKey(), valueHash.getValue());
                }else {
                    client.hmset(duNum,valueHash.getKey(), valueHash.getValue(),ms);
                }

            }
        }



        if (event instanceof DumpKeyValuePair) {

            DumpKeyValuePair valueDump = (DumpKeyValuePair) event;
            Long ms=eventEntity.getMs();
            DB db=valueDump.getDb();
            Long duNum=db.getDbNumber();
            Integer ttl=Integer.MAX_VALUE;
            if(ms>=Long.valueOf(Integer.MAX_VALUE)){
                ttl=Integer.MAX_VALUE;
            }else {
                ttl= Math.toIntExact(ms);
            }


            if (valueDump.getValue() != null) {
                    if (ms == null || ms <= 0L) {
                        if (redisVersion< 3.0) {
                           String res= client.restoreReplace(duNum,valueDump.getKey(), 0, valueDump.getValue(),false);
                        } else {
                            client.restoreReplace(duNum,valueDump.getKey(), 0, valueDump.getValue());
                        }
                    }else {
                        if (redisVersion< 3.0) {
                            String res=client.restoreReplace(duNum,valueDump.getKey(),  ttl, valueDump.getValue(),false);
                            if(null!=res&&res.equalsIgnoreCase("OK")){
                                if(ms>=Long.valueOf(Integer.MAX_VALUE)){
                                    client.pexpire(duNum,valueDump.getKey(),ms);
                                }
                            }else {
                                if(ms>=Long.valueOf(Integer.MAX_VALUE)){
                                    client.pexpire(duNum,valueDump.getKey(),ms);
                                }
                            }
                        } else {
                            String res=client.restoreReplace(duNum,valueDump.getKey(), ttl, valueDump.getValue());
                            System.out.println(res);
                            if(res.equalsIgnoreCase("OK")){
                                if(ms>=Long.valueOf(Integer.MAX_VALUE)){
                                    client.pexpire(duNum,valueDump.getKey(),ms);
                                }
                            }else {
                                if(ms>=Long.valueOf(Integer.MAX_VALUE)){
                                    client.pexpire(duNum,valueDump.getKey(),ms);
                                }
                            }
                        }
                    }
            }
        }

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);

    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) {
        if(null!=next){
            next.run(replicator,eventEntity);
        }

    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }
}
