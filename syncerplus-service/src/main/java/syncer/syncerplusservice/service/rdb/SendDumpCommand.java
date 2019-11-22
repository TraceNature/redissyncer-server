package syncer.syncerplusservice.service.rdb;

import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusservice.pool.ConnectionPool;
import syncer.syncerplusservice.util.Jedis.TestJedisClient;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public interface SendDumpCommand {
     void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor);
     void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor, TestJedisClient targetJedisClientPool);
}
