package syncer.transmission.client.impl;

import syncer.jedis.Jedis;
import syncer.jedis.Pipeline;
import syncer.transmission.client.impl.JedisPipeLineClient;
import syncer.transmission.entity.KVPersistenceDataEntity;

public interface JedisRetryRunner {
    void run();
}
