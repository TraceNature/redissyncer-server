package com.i1314i.syncerplusservice.task;

import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class RdbRestoreTask implements Callable<Object> {
    private DumpKeyValuePair mkv;
    long ms;
    private RedisClient redisClient;
    private ConnectionPool pool;
    private boolean status;
    private StringBuffer info;

    public RdbRestoreTask(DumpKeyValuePair mkv, long ms, RedisClient redisClient, ConnectionPool pool, boolean status) {
        this.mkv = mkv;
        this.ms = ms;
        this.redisClient = redisClient;
        this.pool = pool;
        this.status = status;
        this.info=new StringBuffer();
    }

    public RdbRestoreTask(DumpKeyValuePair mkv, long ms, RedisClient redisClient, ConnectionPool pool, boolean status, StringBuffer info) {
        this.mkv = mkv;
        this.ms = ms;
        this.redisClient = redisClient;
        this.pool = pool;
        this.status = status;
        this.info = info;
    }

    @Override
    public Object call() throws Exception {

        Object r = redisClient.restore(mkv.getRawKey(), ms, mkv.getValue(), status);
        pool.release(redisClient);
        info.append(mkv.getKey());
        info.append("->");
        info.append(r.toString());
        log.info(info.toString());
        return r;
    }
}
