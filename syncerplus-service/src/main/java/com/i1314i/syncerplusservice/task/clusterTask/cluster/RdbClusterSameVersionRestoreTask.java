package com.i1314i.syncerplusservice.task.clusterTask.cluster;

import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisCluster;

import java.util.Date;
import java.util.concurrent.Callable;
@Slf4j
public class RdbClusterSameVersionRestoreTask implements Callable<Object> {
    private DumpKeyValuePair mkv;
    long ms;
    private JedisClusterPlus redisClient;
    private boolean status;
    private StringBuffer info;


    public RdbClusterSameVersionRestoreTask(DumpKeyValuePair mkv, long ms, JedisClusterPlus redisClient, boolean status, StringBuffer info) {
        this.mkv = mkv;
        this.ms = ms;
        this.redisClient = redisClient;
        this.status = status;
        this.info = info;

    }

    @Override
    public Object call() throws Exception {
        Object r = null;
        int i = 3;
        try {
            while (i > 0) {

                //待验证
                int ttl= (int) (ms/1000);
                if(redisClient.del(mkv.getKey())>=0){
                    r = redisClient.restore(mkv.getKey(), ttl, mkv.getValue());
                }


                if (r.equals("OK")) {
                    i = -1;
                    info.append(new String(mkv.getKey()));
                    info.append("->");
                    info.append(r.toString());
                    log.info(info.toString());
                    break;
                }
                i--;


            }


            if(i!=-1){
                log.warn("key : {} not copy",new String(mkv.getKey()));
            }


        } catch (Exception e) {

            log.warn("restore error: {}" , e.getMessage());
        } finally {

            if (redisClient != null) {
//                redisClient.close();
            }
        }

        return r;
    }
}
