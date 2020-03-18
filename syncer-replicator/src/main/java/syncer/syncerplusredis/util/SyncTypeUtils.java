package syncer.syncerplusredis.util;

import javafx.concurrent.Task;
import syncer.syncerplusredis.constant.OffsetPlace;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.constant.TaskType;
import syncer.syncerplusredis.entity.FileType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description SyncType获取
 * @Date 2020/3/17
 */
public class SyncTypeUtils {
    private final static Map<Integer,SyncType>syncTypeMap=new HashMap<>();
    /**\
     * 任务类型 全量+增量/全量/增量
     */
    private final static Map<Integer, TaskType>taskTypeMap=new HashMap<>();

    public static final Map<Integer, OffsetPlace>offSetMap=new HashMap<>();

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
     * @param offsetPlaceCode
     * @return
     */
    public static OffsetPlace getOffsetPlace(int offsetPlaceCode){
        if(offSetMap.containsKey(offsetPlaceCode)){
            return offSetMap.get(offsetPlaceCode);
        }
        return OffsetPlace.ENDBUFFER;
    }
}
