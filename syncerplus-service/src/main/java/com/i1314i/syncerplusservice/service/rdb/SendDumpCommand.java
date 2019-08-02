package com.i1314i.syncerplusservice.service.rdb;

import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public interface SendDumpCommand {
     void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor);
     void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor, TestJedisClient targetJedisClientPool);
}
