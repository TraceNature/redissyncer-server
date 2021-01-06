package syncer.replica.util;

import syncer.replica.constant.OffsetPlace;
import syncer.replica.entity.*;

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


    public static final Map<Integer, RedisBranchType>redisBranchTypeMap=new HashMap<>();

    public static final Map<Integer, TaskStatusType>taskStatusTypeMap=new HashMap<>();

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


        redisBranchTypeMap.put(RedisBranchType.SINGLE.getCode(),RedisBranchType.SINGLE);
        redisBranchTypeMap.put(RedisBranchType.CLUSTER.getCode(),RedisBranchType.CLUSTER);
        redisBranchTypeMap.put(RedisBranchType.FILE.getCode(),RedisBranchType.FILE);
        redisBranchTypeMap.put(RedisBranchType.SENTINEL.getCode(),RedisBranchType.SENTINEL);

        taskStatusTypeMap.put(TaskStatusType.RUN.getCode(),TaskStatusType.RUN);
        taskStatusTypeMap.put(TaskStatusType.CREATING.getCode(),TaskStatusType.CREATING);
        taskStatusTypeMap.put(TaskStatusType.CREATED.getCode(),TaskStatusType.CREATED);
        taskStatusTypeMap.put(TaskStatusType.RUN.getCode(),TaskStatusType.RUN);
        taskStatusTypeMap.put(TaskStatusType.RDBRUNING.getCode(),TaskStatusType.RDBRUNING);
        taskStatusTypeMap.put(TaskStatusType.COMMANDRUNING.getCode(),TaskStatusType.COMMANDRUNING);
        taskStatusTypeMap.put(TaskStatusType.BROKEN.getCode(),TaskStatusType.BROKEN);
        taskStatusTypeMap.put(TaskStatusType.STOP.getCode(),TaskStatusType.STOP);



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
    public static TaskStatusType getTaskStatusType(int taskStatusType){
        if(taskStatusTypeMap.containsKey(taskStatusType)){
            return taskStatusTypeMap.get(taskStatusType);
        }
        return TaskStatusType.STOP;
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
    public static RedisBranchType getRedisBranchType(int sourceRedisType){
        if(redisBranchTypeMap.containsKey(sourceRedisType)){
            return redisBranchTypeMap.get(sourceRedisType);
        }
        return RedisBranchType.SINGLE;
    }
}
