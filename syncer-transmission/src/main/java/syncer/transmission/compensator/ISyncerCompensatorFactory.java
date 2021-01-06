package syncer.transmission.compensator;

import syncer.replica.constant.RedisBranchTypeEnum;
import syncer.transmission.client.RedisClient;
import syncer.transmission.compensator.impl.MultiThreadSyncerCompensator;
import syncer.transmission.compensator.impl.PipeLineSyncerCompensator;

/**
 * 补偿机制
 */
public class ISyncerCompensatorFactory {
    public static ISyncerCompensator createRedisClient(RedisBranchTypeEnum branchTypeEnum, String taskId, RedisClient client){
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
