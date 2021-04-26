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

import lombok.extern.slf4j.Slf4j;
import syncer.common.config.BreakPointConfig;
import syncer.common.constant.BreakpointContinuationType;
import syncer.replica.constant.RedisType;
import syncer.transmission.client.impl.JedisMultiExecPipeLineClient;
import syncer.transmission.client.impl.JedisPipeLineClient;
import syncer.transmission.client.impl.RedisJedisClusterClient;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
@Slf4j
public class RedisClientFactory {
    public static RedisClient createRedisClient(RedisType redisType, String host, Integer port, String password, String sourceHost, Integer sourcePort, int count, long errorCount, String taskId, String jimUrl, String cfsUrl) {
        RedisClient redisClient = null;
        switch (redisType) {
            case SINGLE:
                if(BreakPointConfig.getBreakpointContinuationType().equals(BreakpointContinuationType.v1)){
                    redisClient = new JedisPipeLineClient(host,port,password,count,errorCount,taskId);
                }else {
                    redisClient = new JedisMultiExecPipeLineClient(host,port,password,sourceHost,sourcePort,count,errorCount,taskId);
                }

                log.info("host[{}],port[{}] , {} client init success",host,port,BreakPointConfig.getBreakpointContinuationType());
//                redisClient = new JDRedisJedisPipeLineClient(host,port,password,count,taskId);
//                redisClient = new JDRedisJedisClient(host,port,password);
                break;
            case CLUSTER:
                redisClient = new RedisJedisClusterClient(host,password,taskId);
                break;
            case SENTINEL:
                redisClient = new JedisPipeLineClient(host,port,password,count,errorCount,taskId);
                break;
//            case JIMDB:
//                redisClient = new JimDb2Client(jimUrl,cfsUrl,taskId);
//                break;
            default:
                break;
        }
        return redisClient;
    }
}
