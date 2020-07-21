package syncer.syncerservice.compensator;

import syncer.syncerplusredis.constant.RedisBranchTypeEnum;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

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
