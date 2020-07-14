package syncer.syncerservice.compensator;

import syncer.syncerplusredis.constant.RedisBranchTypeEnum;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisJedisClusterClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisJedisPipeLineClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 补偿机制
 */
public class ISyncerCompensatorFactory {
    public static ISyncerCompensator createJDRedisClient(RedisBranchTypeEnum branchTypeEnum,String taskId,JDRedisClient client){
        ISyncerCompensator iSyncerCompensator = null;
        switch (branchTypeEnum) {
        case SINGLE:
            iSyncerCompensator=new PipeLineSyncerCompensator();
            break;
        case CLUSTER:
            iSyncerCompensator = new MultiThreadSyncerCompensator(taskId,client);
            break;
        case SENTINEL:
            iSyncerCompensator=new PipeLineSyncerCompensator();
            break;
        default:
            break;

    }
        return iSyncerCompensator;
}

}
