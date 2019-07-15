package com.i1314i.syncerplusservice.util.Jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 平行时空
 * @created 2018-06-14 22:18
 **/
public class JedisClusterUtils {
    private static JedisClusterFactory jedisClusterFactory=null;
    private static GenericObjectPoolConfig config=null;
    private static Set<String> jedisClusterNodes;
    static {
        String password="Zhan1234";
        jedisClusterFactory=new JedisClusterFactory();
        config=new GenericObjectPoolConfig();
        config.setMaxTotal(100);
        config.setMinIdle(10);
        jedisClusterFactory.setSoTimeout(100000);
        jedisClusterNodes=new HashSet<>();
        jedisClusterNodes.add("127.0.0.1:6380");
        jedisClusterNodes.add("127.0.0.1:6381");
        jedisClusterNodes.add("127.0.0.1:6382");
        jedisClusterFactory.setJedisClusterNodes(jedisClusterNodes);
        jedisClusterFactory.setGenericObjectPoolConfig(config);


    }
}
