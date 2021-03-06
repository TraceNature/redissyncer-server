package syncer.replica.util;

import syncer.replica.constant.RedisType;
import syncer.replica.status.TaskStatus;
import syncer.replica.type.FileType;
import syncer.replica.type.SyncType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description SyncType获取
 * @Date 2020/3/17
 */
public class SyncTypeUtils {
    private final static Map<Integer, SyncType> syncTypeMap=new HashMap<>();
    /**\
     * 任务类型 全量+增量/全量/增量
     */
    private final static Map<Integer, TaskType>taskTypeMap=new HashMap<>();

    public static final Map<Integer, OffsetPlace>offSetMap=new HashMap<>();


    public static final Map<Integer, RedisType>redisTypeMap=new HashMap<>();

    public static final Map<Integer, TaskStatus>taskStatusTypeMap=new HashMap<>();

    static {
        syncTypeMap.put(SyncType.SYNC.getCode(),SyncType.SYNC);
        syncTypeMap.put(SyncType.RDB.getCode(),SyncType.RDB);
        syncTypeMap.put(SyncType.AOF.getCode(),SyncType.AOF);
        syncTypeMap.put(SyncType.MIXED.getCode(),SyncType.MIXED);
        syncTypeMap.put(SyncType.ONLINERDB.getCode(),SyncType.ONLINERDB);
        syncTypeMap.put(SyncType.ONLINEAOF.getCode(),SyncType.ONLINEAOF);
        syncTypeMap.put(SyncType.ONLINEMIXED.getCode(),SyncType.ONLINEMIXED);
        syncTypeMap.put(SyncType.COMMANDDUMPUP.getCode(),SyncType.COMMANDDUMPUP);

        taskTypeMap.put(TaskType.TOTAL.getCode(),TaskType.TOTAL);
        taskTypeMap.put(TaskType.STOCKONLY.getCode(),TaskType.STOCKONLY);
        taskTypeMap.put(TaskType.INCREMENTONLY.getCode(),TaskType.INCREMENTONLY);

        offSetMap.put(OffsetPlace.ENDBUFFER.getCode(),OffsetPlace.ENDBUFFER);
        offSetMap.put(OffsetPlace.BEGINBUFFER.getCode(),OffsetPlace.BEGINBUFFER);


        redisTypeMap.put(RedisType.SINGLE.getCode(),RedisType.SINGLE);
        redisTypeMap.put(RedisType.CLUSTER.getCode(),RedisType.CLUSTER);
        redisTypeMap.put(RedisType.FILE.getCode(),RedisType.FILE);
        redisTypeMap.put(RedisType.SENTINEL.getCode(),RedisType.SENTINEL);
        redisTypeMap.put(RedisType.KAFKA.getCode(),RedisType.KAFKA);
        redisTypeMap.put(RedisType.NONE.getCode(),RedisType.NONE);

        taskStatusTypeMap.put(TaskStatus.STARTING.getCode(),TaskStatus.STARTING);
        taskStatusTypeMap.put(TaskStatus.CREATING.getCode(),TaskStatus.CREATING);
        taskStatusTypeMap.put(TaskStatus.CREATED.getCode(),TaskStatus.CREATED);
        taskStatusTypeMap.put(TaskStatus.FINISH.getCode(),TaskStatus.FINISH);
        taskStatusTypeMap.put(TaskStatus.RDBRUNNING.getCode(),TaskStatus.RDBRUNNING);
        taskStatusTypeMap.put(TaskStatus.COMMANDRUNNING.getCode(),TaskStatus.COMMANDRUNNING);
        taskStatusTypeMap.put(TaskStatus.BROKEN.getCode(),TaskStatus.BROKEN);
        taskStatusTypeMap.put(TaskStatus.STOP.getCode(),TaskStatus.STOP);



    }

    /**
     * 通过code获取SyncType
     * @param syncTypeCode
     * @return
     */
    public static SyncType getSyncType(int syncTypeCode){
        if(syncTypeMap.containsKey(syncTypeCode)){
            return syncTypeMap.get(syncTypeCode);
        }
        return SyncType.SYNC;
    }

    public static SyncType getSyncType(FileType fileType){
        SyncType syncType=null;
        for(Map.Entry<Integer,SyncType> entry : syncTypeMap.entrySet()){
            if(entry.getValue().getFileType().equals(fileType)){
                syncType=entry.getValue();
                break;
            }
        }

        return syncType;
    }

    /**
     * 通过code获取SyncType
     * @param taskTypeCode
     * @return
     */
    public static TaskType getTaskType(int taskTypeCode){
        if(taskTypeMap.containsKey(taskTypeCode)){
            return taskTypeMap.get(taskTypeCode);
        }
        return TaskType.TOTAL;
    }

    /**
     * 通过code获取TaskType
     * @param taskStatusType
     * @return
     */
    public static TaskStatus getTaskStatusType(int taskStatusType){
        if(taskStatusTypeMap.containsKey(taskStatusType)){
            return taskStatusTypeMap.get(taskStatusType);
        }
        return TaskStatus.STOP;
    }


    /**
     * 通过code获取TaskType
     * @param offsetPlaceCode
     * @return
     */
    public static OffsetPlace getOffsetPlace(int offsetPlaceCode){
        if(offSetMap.containsKey(offsetPlaceCode)){
            return offSetMap.get(offsetPlaceCode);
        }
        return OffsetPlace.ENDBUFFER;
    }



    /**
     * 通过code获取RedisBranchType
     * @param sourceRedisType
     * @return
     */
    public static RedisType getRedisType(int sourceRedisType){
        if(redisTypeMap.containsKey(sourceRedisType)){
            return redisTypeMap.get(sourceRedisType);
        }
        return RedisType.SINGLE;
    }
}
