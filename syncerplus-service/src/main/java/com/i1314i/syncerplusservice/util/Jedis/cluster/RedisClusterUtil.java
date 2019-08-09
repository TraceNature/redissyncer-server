package com.i1314i.syncerplusservice.util.Jedis.cluster;

import com.i1314i.syncerplusservice.util.Jedis.JedisClusterFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

public class RedisClusterUtil {

    private static JedisCluster jedis = null;

    //可用连接实例的最大数目，默认为8；
    //如果赋值为-1，则表示不限制，如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)
    private static Integer MAX_TOTAL = 1024;
    //控制一个pool最多有多少个状态为idle(空闲)的jedis实例，默认值是8
    private static Integer MAX_IDLE = 200;
    //等待可用连接的最大时间，单位是毫秒，默认值为-1，表示永不超时。
    //如果超过等待时间，则直接抛出JedisConnectionException
    private static Integer MAX_WAIT_MILLIS = 10000;
    //在borrow(用)一个jedis实例时，是否提前进行validate(验证)操作；
    //如果为true，则得到的jedis实例均是可用的
    private static Boolean TEST_ON_BORROW = true;
    //在空闲时检查有效性, 默认false
    private static Boolean TEST_WHILE_IDLE = true;
    //是否进行有效性检查
    private static Boolean TEST_ON_RETURN = true;

    //访问密码
    private static String AUTH = "1234@abcd";

    /**
     * 静态块，初始化Redis连接池
     */
    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
        /*注意：
            在高版本的jedis jar包，比如本版本2.9.0，JedisPoolConfig没有setMaxActive和setMaxWait属性了
            这是因为高版本中官方废弃了此方法，用以下两个属性替换。
            maxActive  ==>  maxTotal
            maxWait==>  maxWaitMillis
         */
            config.setMaxTotal(MAX_TOTAL);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT_MILLIS);
            config.setTestOnBorrow(TEST_ON_BORROW);
            config.setTestWhileIdle(TEST_WHILE_IDLE);
            config.setTestOnReturn(TEST_ON_RETURN);

            Set<HostAndPort> jedisClusterNode = new HashSet<HostAndPort>();
            jedisClusterNode.add(new HostAndPort("114.67.100.239", 8002));
            jedisClusterNode.add(new HostAndPort("114.67.100.239", 8003));
            jedisClusterNode.add(new HostAndPort("114.67.100.238", 8002));
            jedisClusterNode.add(new HostAndPort("114.67.100.238", 8003));
            jedisClusterNode.add(new HostAndPort("114.67.100.240", 8002));
            jedisClusterNode.add(new HostAndPort("114.67.100.240", 8003));
            jedis = new JedisCluster(jedisClusterNode,1000,1000,5,config);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static JedisCluster getJedis(){
        return jedis;
    }


    public static void main(String[] args) throws ParseException {

            JedisClusterFactory jedisClusterFactory=new JedisClusterFactory();
         GenericObjectPoolConfig  config=new GenericObjectPoolConfig();
            config.setMaxTotal(100);
            config.setMinIdle(10);
            jedisClusterFactory.setSoTimeout(10000000);
            jedisClusterFactory.setConnectionTimeout(10000000);
            if(!StringUtils.isEmpty("")){
                jedisClusterFactory.setPassWord("");
            }

    }

}
