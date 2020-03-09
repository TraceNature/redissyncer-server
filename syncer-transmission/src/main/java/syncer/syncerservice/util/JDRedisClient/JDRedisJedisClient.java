package syncer.syncerservice.util.JDRedisClient;


import com.alibaba.fastjson.JSON;
import syncer.syncerjedis.Jedis;
import syncer.syncerjedis.params.SetParams;
import syncer.syncerplusredis.constant.PipeLineCompensatorEnum;
import syncer.syncerplusredis.entity.EventEntity;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.exception.KeyWeed0utException;
import syncer.syncerservice.util.jedis.ObjectUtils;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerservice.util.jedis.cmd.JedisProtocolCommand;
import syncer.syncerservice.util.jedis.pool.JDJedisClientPool;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class JDRedisJedisClient implements JDRedisClient {
    JDJedisClientPool jdJedisClientPool;
    private Lock commitLock=new ReentrantLock();

    private Integer currentDbNum=0;
    private Jedis client=null;

    public JDRedisJedisClient(String host, Integer port, String password) {
        jdJedisClientPool=new JDJedisClientPool(host,port,null,password,0);
        client=jdJedisClientPool.getResource();
    }



    @Override
    public String get(final Long dbNum,byte[] key) {
        return client.get(StringUtils.toString(key));
    }

    @Override
    public String get(final Long dbNum,String key) {
        return client.get(key);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        return client.set(key,value);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {

        return client.set(key, value, SetParams.setParams().px(ms));
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        return client.append(key,value);
    }


    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        return client.lpush(key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        Long res=client.lpush(key, value);
        client.pexpire(key,ms);
        return res;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {

        return client.lpush(key, ObjectUtils.listBytes(value));
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        Long result = client.lpush(key, ObjectUtils.listBytes(value));
        client.pexpire(key,ms);
        return result;
    }






    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        return client.sadd(key,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        Long result = client.sadd(key, members);
        client.pexpire(key,ms);
        return result;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        byte[][]bytesMembers=ObjectUtils.setBytes(members);
        return client.sadd(key,bytesMembers);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        byte[][]bytesMembers=ObjectUtils.setBytes(members);
        Long result = client.sadd(key, bytesMembers);
        client.pexpire(key,ms);
        return result;
    }


    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        return client.zadd(key, ObjectUtils.zsetBytes(value));
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        Long result = client.zadd(key, ObjectUtils.zsetBytes(value));
        client.pexpire(key,ms);
        return result;
    }



    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        return client.hmset(key,hash);
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        String result = client.hmset(key,hash);
        client.pexpire(key,ms);
        return result;
    }


    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return client.restore(key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return client.restoreReplace(key,ttl,serializedValue);

    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        String result=null;
        if(highVersion){
            result = client.restoreReplace(key,ttl,serializedValue);
        }else {
            if (client.del(key) >= 0) {
                result = client.restore(key,ttl, serializedValue);
            }else {
                result = client.restore(key, ttl, serializedValue);
            }
        }

        return result;
    }



    @Override
    public Object send(byte[] cmd, byte[]... args) {
        if(Arrays.equals(cmd,"SELECT".getBytes())) {
            Long commDbNum = Long.valueOf(new String(args[0]));
            selectDb(commDbNum);
            return null;
        }
        return client.sendCommand(JedisProtocolCommand.builder().raw(cmd).build(),args);
    }

    @Override
    public void select(Integer dbNum) {
        selectDb(Long.valueOf(dbNum));
    }

    void selectDb(Long dbNum){
        commitLock.lock();
        try{
            if(dbNum!=null&&!currentDbNum.equals(dbNum.intValue())){
                currentDbNum=dbNum.intValue();
                client.select(dbNum.intValue());
            }
        }finally {
            commitLock.unlock();
        }

    }



    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        return client.pexpire(new String(key),ms);
    }

}
