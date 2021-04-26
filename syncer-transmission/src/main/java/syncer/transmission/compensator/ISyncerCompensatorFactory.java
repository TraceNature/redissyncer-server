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

package syncer.transmission.compensator;

import syncer.replica.constant.RedisType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.compensator.impl.MultiThreadSyncerCompensator;
import syncer.transmission.compensator.impl.PipeLineSyncerCompensator;

/**
 * 补偿机制
 */
public class ISyncerCompensatorFactory {
    public static ISyncerCompensator createRedisClient(RedisType redisType, String taskId, RedisClient client){
        ISyncerCompensator iSyncerCompensator = null;
        switch (redisType) {
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
