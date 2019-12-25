package syncer.syncerservice.util.JDRedisClient;


import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.util.jedis.ObjectUtils;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerservice.util.jedis.pool.JDJedisClientPool;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class JDRedisJedisClient implements JDRedisClient {
    JDJedisClientPool jdJedisClientPool;



    public JDRedisJedisClient(String host, Integer port, String password) {
        jdJedisClientPool=new JDJedisClientPool(host,port,null,password,0);
    }


    @Override
    public String get(final Long dbNum,byte[] key) {
        return jdJedisClientPool.get(StringUtils.toString(key));
    }

    @Override
    public String get(final Long dbNum,String key) {
        return jdJedisClientPool.get(key);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        return jdJedisClientPool.set(key,value);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        return jdJedisClientPool.set(key,value,ms);
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        return jdJedisClientPool.append(key,value);
    }


    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        return jdJedisClientPool.lpush(key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        return jdJedisClientPool.lpush(key,ms,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        return jdJedisClientPool.lpush(key, ObjectUtils.listBytes(value));
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        return jdJedisClientPool.lpush(key,ms, ObjectUtils.listBytes(value));
    }






    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        return jdJedisClientPool.sadd(key,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        return jdJedisClientPool.sadd(key,ms,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        byte[][]bytesMembers=ObjectUtils.setBytes(members);
        return jdJedisClientPool.sadd(key,bytesMembers);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        byte[][]bytesMembers=ObjectUtils.setBytes(members);
        return jdJedisClientPool.sadd(key,ms,bytesMembers);
    }


    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        return jdJedisClientPool.zadd(key,value);
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        return jdJedisClientPool.zadd(key,ms,value);
    }



    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        return jdJedisClientPool.hmset(key,hash);
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        return jdJedisClientPool.hmset(key,ms,hash);
    }


    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return jdJedisClientPool.restore(key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return jdJedisClientPool.restoreReplace(key,ttl,serializedValue);

    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        return jdJedisClientPool.restoreReplace(key,ttl,serializedValue,highVersion);
    }



    @Override
    public Object send(byte[] cmd, byte[]... args) {
        return jdJedisClientPool.send(cmd,args);
    }

    @Override
    public void select(Integer dbNum) {

    }

}
