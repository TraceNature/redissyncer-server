package syncer.syncerservice.util.jedis.pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.util.Pool;
import syncer.syncerservice.util.jedis.JDJedis;


public class JDJedisPoolAbstract  extends Pool<JDJedis> {

    public JDJedisPoolAbstract() {
        super();
    }

    public JDJedisPoolAbstract(GenericObjectPoolConfig poolConfig, PooledObjectFactory<JDJedis> factory) {
        super(poolConfig, factory);
    }

    @Override
    public void returnBrokenResource(JDJedis resource) {
        super.returnBrokenResource(resource);
    }

    @Override
    public void returnResource(JDJedis resource) {
        super.returnResource(resource);
    }
}
