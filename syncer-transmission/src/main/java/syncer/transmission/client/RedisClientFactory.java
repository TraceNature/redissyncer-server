package syncer.transmission.client;

import syncer.replica.constant.RedisBranchTypeEnum;
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
                redisClient = new JedisPipeLineClient(host,port,password,count,errorCount,taskId);
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
