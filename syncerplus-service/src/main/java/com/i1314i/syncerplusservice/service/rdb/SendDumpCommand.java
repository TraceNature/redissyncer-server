package com.i1314i.syncerplusservice.service.rdb;

import com.i1314i.syncerplusredis.event.Event;
import com.i1314i.syncerplusredis.replicator.Replicator;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public interface SendDumpCommand {
     void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor);
     void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor, TestJedisClient targetJedisClientPool);
}
