package syncer.syncerservice.util.JDRedisClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.util.EnumUtils;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.jedis.StringUtils;


public class JDRedisLettuceClient implements JDRedisClient {
    RedisCommands<String, String> syncCommands;
    private Integer currentDbNum=0;
    public JDRedisLettuceClient() {
        RedisClient client = RedisClient.create(RedisURI.create("redis://192.168.37.128:7000"));
        RedisClient redisClient = RedisClient.create("redis://@localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        syncCommands= connection.sync();
    }


    @Override
    public String get(final Long dbNum,byte[] key) {
        selectDb(dbNum);
        return syncCommands.get(StringUtils.toString(key));
    }

    @Override
    public String get(final Long dbNum,String key) {
        selectDb(dbNum);
        return syncCommands.get(key);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        selectDb(dbNum);
        return syncCommands.set(Strings.byteToString(key),Strings.byteToString(value));

    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        selectDb(dbNum);
        return syncCommands.set(Strings.byteToString(key),Strings.byteToString(value), SetArgs.Builder.px(ms));
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        selectDb(dbNum);
        return syncCommands.append(Strings.byteToString(key),Strings.byteToString(value));
    }


    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        selectDb(dbNum);
        return syncCommands.lpush(Strings.byteToString(key),Strings.byteToString(value));
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        selectDb(dbNum);
        Long res=syncCommands.lpush(Strings.byteToString(key),Strings.byteToString(value));
        syncCommands.expire(Strings.byteToString(key),ms);
        return res;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        selectDb(dbNum);
        return syncCommands.lpush(Strings.byteToString(key),Strings.byteToString(value));
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        selectDb(dbNum);
        Long res=syncCommands.lpush(Strings.byteToString(key),Strings.byteToString(value));
        syncCommands.expire(Strings.byteToString(key),ms);
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, List<byte[]> value) {
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        return null;
    }


    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        selectDb(dbNum);
        return syncCommands.sadd(Strings.byteToString(key),Strings.byteToString(members));
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        selectDb(dbNum);
        Long res= syncCommands.sadd(Strings.byteToString(key),Strings.byteToString(members));
        syncCommands.expire(Strings.byteToString(key),ms);
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        selectDb(dbNum);
        return syncCommands.sadd(Strings.byteToString(key),Strings.byteToString(members));
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        selectDb(dbNum);
        Long res= syncCommands.sadd(Strings.byteToString(key),Strings.byteToString(members));
        syncCommands.expire(Strings.byteToString(key),ms);
        return res;
    }


    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        selectDb(dbNum);
        return  syncCommands.zadd(Strings.byteToString(key),toScoredValues(value));

    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        selectDb(dbNum);
        Long res= syncCommands.zadd(Strings.byteToString(key),toScoredValues(value));
        syncCommands.expire(Strings.byteToString(key),ms);
        return res;
    }



    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        selectDb(dbNum);
        return syncCommands.hmset(Strings.byteToString(key),toHmsetValue(hash));
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        selectDb(dbNum);
        String res= syncCommands.hmset(Strings.byteToString(key),toHmsetValue(hash));
        syncCommands.expire(Strings.byteToString(key),ms);
        return res;
    }



    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        selectDb(dbNum);
        return restoreReplace(dbNum,key,ttl,serializedValue,true);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        selectDb(dbNum);
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        selectDb(dbNum);
        Lock lock=new ReentrantLock();
        lock.lock();
        try {
            if(highVersion){
                return syncCommands.restore(Strings.byteToString(key),serializedValue,RestoreArgs.Builder.ttl(ttl).replace());
            }else {
                if(syncCommands.del(Strings.byteToString(key))>=-1){
                    return syncCommands.restore(Strings.byteToString(key),ttl,serializedValue);
                }else {
                    return syncCommands.restore(Strings.byteToString(key),ttl,serializedValue);
                }
            }
        }finally {
            lock.unlock();
        }

    }




    @Override
    public Object send(byte[] cmd, byte[]... args) {
        RedisCodec<String, String> codec = StringCodec.UTF8;
        CommandArgs newargs = new CommandArgs<>(codec);
        for (byte[]v:args){
            newargs.add(v);
        }
        return  syncCommands.dispatch(EnumUtils.getSexEnumByCode(cmd), new StatusOutput<>(codec),
                newargs);
    }

    @Override
    public void select(Integer dbNum) {

    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        return null;
    }

    void selectDb(Long dbNum){
        if(dbNum!=null&&!currentDbNum.equals(dbNum.intValue())){
            currentDbNum=dbNum.intValue();
            syncCommands.select(dbNum.intValue());
        }
    }


    private  ScoredValue<String>[] toScoredValues(Set<ZSetEntry> value){
        ScoredValue<String>[]res=new ScoredValue[value.size()];
        int i=0;
        for (ZSetEntry data:value
             ) {
            res[i++]=ScoredValue.fromNullable(data.getScore(),Strings.byteToString(data.getElement()));
        }
        return res;
    }

    private Map<String, String>  toHmsetValue(Map<byte[], byte[]> hash){
        Map<String,String>res=new HashMap<>();
        for (Map.Entry<byte[],byte[]>data:hash.entrySet()
             ) {
            res.put(Strings.byteToString(data.getKey()),Strings.byteToString(data.getValue()));
        }
        return res;
    }
}
