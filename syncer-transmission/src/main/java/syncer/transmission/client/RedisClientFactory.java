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

package syncer.transmission.client;

import syncer.replica.util.RedisBranchTypeEnum;
import syncer.transmission.client.impl.JedisMultiExecPipeLineClient;
import syncer.transmission.client.impl.JedisPipeLineClient;
import syncer.transmission.client.impl.RedisJedisClusterClient;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
public class RedisClientFactory {
    public static RedisClient createRedisClient(RedisBranchTypeEnum branchType, String host, Integer port, String password, int count, long errorCount, String taskId, String jimUrl, String cfsUrl) {
        RedisClient redisClient = null;
        switch (branchType) {
            case SINGLE:
//                redisClient = new JedisPipeLineClient(host,port,password,count,errorCount,taskId);
                redisClient = new JedisMultiExecPipeLineClient(host,port,password,count,errorCount,taskId);
//                redisClient = new JDRedisJedisPipeLineClient(host,port,password,count,taskId);
//                redisClient = new JDRedisJedisClient(host,port,password);
                break;
            case CLUSTER:
                redisClient = new RedisJedisClusterClient(host,password,taskId);
                break;
            case SENTINEL:
                redisClient = new JedisPipeLineClient(host,port,password,count,errorCount,taskId);
                break;
            case JIMDB:
//                redisClient = new JimDb2Client(jimUrl,cfsUrl,taskId);
                break;
            default:
                break;
        }
        return redisClient;
    }
}
