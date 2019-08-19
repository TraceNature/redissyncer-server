package com.i1314i.syncerplusservice.util.Jedis.pool;

import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.util.Pool;

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
