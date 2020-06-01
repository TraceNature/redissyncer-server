package syncer.syncerservice.util.JDRedisClient;

import syncer.syncerplusredis.constant.RedisBranchTypeEnum;
import syncer.syncerservice.util.JDRedisClient.jimdb.JimDb2Client;


public class JDRedisClientFactory {


    public static JDRedisClient createJDRedisClient(RedisBranchTypeEnum branchTypeEnum,String host, Integer port, String password, int count,String taskId,String jimUrl, String cfsUrl) {
        JDRedisClient redisClient = null;
        switch (branchTypeEnum) {
            case SINGLE:
redisClient = new JDRedisJedisPipeLineClient(host,port,password,count,taskId);
//                redisClient = new JDRedisJedisClient(host,port,password);
                break;
            case CLUSTER:
                redisClient = new JDRedisJedisClusterClient(host,password,taskId);
                break;
            case SENTINEL:
//                redisClient = new JDRedisJedisPipeLineClient(host,port,password,count,taskId);
                break;
            case JIMDB:
                redisClient = new JimDb2Client(jimUrl,cfsUrl,taskId);
                break;
             default:
                 break;
        }
        return redisClient;
    }

}
