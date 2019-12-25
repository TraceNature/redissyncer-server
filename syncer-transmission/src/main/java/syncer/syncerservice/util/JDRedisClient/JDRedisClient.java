package syncer.syncerservice.util.JDRedisClient;

import syncer.syncerplusredis.rdb.datatype.ZSetEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface JDRedisClient {


    //STRING
//    String set(final byte[]key,final byte[]value);
//    String set(final byte[]key,final byte[]value,long ms);
//    Long append(final byte[] key, final byte[] value);
    String get(final Long dbNum,final byte[]key);
    String get(final Long dbNum,final String key);
    String set(final Long dbNum,final byte[]key,final byte[]value);
    String set(final Long dbNum,final byte[]key,final byte[]value,long ms);
    Long append(final Long dbNum,final byte[] key, final byte[] value);

    //LIST
//    Long lpush(final  byte[]key,final byte[]...value);
//    Long lpush( final byte[]key,long ms,final byte[]...value);
//
//    Long lpush(final  byte[]key,final List<byte[]> value);
//    Long lpush( final byte[]key,long ms,final List<byte[]>value);

    Long lpush(final Long dbNum,final  byte[]key,final byte[]...value);
    Long lpush(final Long dbNum,final byte[]key,long ms,final byte[]...value);

    Long lpush(final Long dbNum,final  byte[]key,final List<byte[]> value);
    Long lpush(final Long dbNum,final byte[]key,long ms,final List<byte[]>value);

    //SET
//    Long sadd(final byte[] key, final byte[]... members);
//    Long sadd(final byte[] key,long ms, final byte[]... members);
//    Long sadd(final byte[] key, final Set<byte[]>  members);
//    Long sadd(final byte[] key,long ms, final Set<byte[]> members);

    Long sadd(final Long dbNum,final byte[] key, final byte[]... members);
    Long sadd(final Long dbNum,final byte[] key,long ms, final byte[]... members);
    Long sadd(final Long dbNum,final byte[] key, final Set<byte[]>  members);
    Long sadd(final Long dbNum,final byte[] key,long ms, final Set<byte[]> members);


    //ZSET
//    Long zadd(byte[]key, Set<ZSetEntry> value);
//    Long zadd(byte[]key,Set<ZSetEntry> value,long ms);

    Long zadd(final Long dbNum,byte[]key, Set<ZSetEntry> value);
    Long zadd(final Long dbNum,byte[]key,Set<ZSetEntry> value,long ms);

    //HASH
//
//    String hmset(final byte[] key, final Map<byte[], byte[]> hash);
//    String hmset(final byte[] key, final Map<byte[], byte[]> hash,long ms);

    String hmset(final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash);
    String hmset(final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash,long ms);

    //DUMP
//    String restore(final byte[] key, final int ttl, final byte[] serializedValue);
//    String restoreReplace(final byte[] key, final int ttl, final byte[] serializedValue);
//    String restoreReplace(byte[] key, int ttl, byte[] serializedValue,boolean highVersion);

    String restore(final Long dbNum,final byte[] key, final long ttl, final byte[] serializedValue);
    String restoreReplace(final Long dbNum,final byte[] key, final long ttl, final byte[] serializedValue);
    String restoreReplace(final Long dbNum,byte[] key, long ttl, byte[] serializedValue,boolean highVersion);

    Object send(final byte[] cmd, final byte[]... args);

    void select(final Integer dbNum);

    Long pexpire(Long dbNum,byte[]key,long ms);
}
