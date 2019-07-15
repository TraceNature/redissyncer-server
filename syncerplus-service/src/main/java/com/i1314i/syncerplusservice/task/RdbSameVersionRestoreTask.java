package com.i1314i.syncerplusservice.task;

import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.service.exception.TaskRestoreException;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.SetParams;

import java.util.concurrent.Callable;


/**
 * 相同版本(>3.0)之间的数据迁移
 */
@Slf4j
public class RdbSameVersionRestoreTask implements Callable<Object> {
    private DumpKeyValuePair mkv;
    long ms;
    private RedisClient redisClient;
    private ConnectionPool pool;
    private boolean status;
    private StringBuffer info;


    public RdbSameVersionRestoreTask(DumpKeyValuePair mkv, long ms, RedisClient redisClient, ConnectionPool pool, boolean status, StringBuffer info) {
        this.mkv = mkv;
        this.ms = ms;
        this.redisClient = redisClient;
        this.pool = pool;
        this.status = status;
        this.info = info;

    }

    @Override
    public Object call() throws Exception {
        Object r = null;
        int i = 3;
        try {
            while (i > 0) {

                r = redisClient.restore(mkv.getRawKey(), ms, mkv.getValue(), status);
                if (r.equals("OK")) {
                    i = -1;
                    info.append(mkv.getKey());
                    info.append("->");
                    info.append(r.toString());
                    log.info(info.toString());
                    break;
                }
                i--;


            }

        } catch (Exception e) {

            log.info("restore error: " + e.getMessage());
        } finally {

            if (redisClient != null) {
                pool.release(redisClient);
            }
        }

        return r;
    }
}