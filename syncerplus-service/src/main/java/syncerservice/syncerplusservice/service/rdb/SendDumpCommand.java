package syncerservice.syncerplusservice.service.rdb;

import syncerservice.syncerplusredis.event.Event;
import syncerservice.syncerplusredis.replicator.Replicator;
import syncerservice.syncerplusservice.pool.ConnectionPool;
import syncerservice.syncerplusservice.util.Jedis.TestJedisClient;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public interface SendDumpCommand {
     void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor);
     void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor, TestJedisClient targetJedisClientPool);
}
