package com.i1314i.syncerplusservice.task.singleTask.lowerVersion.defaultVersion;

import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.concurrent.Callable;


/**
 * 相同版本(<3.0)之间的数据迁移
 */
@Slf4j
public class RdbVersionLowerRestoreTask implements Callable<Object> {
    private DumpKeyValuePair mkv;
    long ms;
    private RedisClient redisClient;
    private ConnectionPool pool;
    private boolean status;
    private StringBuffer info;
    private Jedis targetJedis;

    public RdbVersionLowerRestoreTask(DumpKeyValuePair mkv, long ms, RedisClient redisClient, ConnectionPool pool, boolean status, StringBuffer info, Jedis targetJedis) {
        this.mkv = mkv;
        this.ms = ms;
        this.redisClient = redisClient;
        this.pool = pool;
        this.status = status;
        this.info = info;
        this.targetJedis = targetJedis;
    }


    @Override
    public Object call() throws Exception {

        Object r = null;
        int i = 3;
        try {
            while (i > 0) {

                r = TestJedisClient.restorebyteObject(mkv.getKey(), mkv.getValue(), mkv.getExpiredSeconds(), targetJedis, status);
                if (r.equals("OK")) {
                    info.append(new String(mkv.getKey()));
                    info.append("->");
                    info.append(r.toString());
                    log.info(info.toString());
                    i = -1;
                    break;
                }
                i--;


            }

            if(i!=-1){
                log.warn("key :{} not copy", new String(mkv.getKey()));
            }

        } catch (Exception e) {

            log.warn("restore error: {}" , e.getMessage());

        } finally {

            if (redisClient != null) {
                pool.release(redisClient);
            }

            if(targetJedis!=null){
                targetJedis.close();
            }
        }
        return r;
    }
}