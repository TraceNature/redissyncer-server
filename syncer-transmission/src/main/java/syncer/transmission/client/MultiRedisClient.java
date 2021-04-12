package syncer.transmission.client;

import syncer.replica.datatype.rdb.zset.ZSetEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 断点续传2.0
 */
public interface MultiRedisClient {

    /**
     * STRING
     * @param dbNum
     * @param key
     * @return
     */
    String get(String replid,long offset,final Long dbNum,final byte[]key);
    String get(String replid,long offset,final Long dbNum,final String key);
    String set(String replid,long offset,final Long dbNum,final byte[]key,final byte[]value);
    String set(String replid,long offset,final Long dbNum,final byte[]key,final byte[]value,long ms);
    Long append(String replid,long offset,final Long dbNum,final byte[] key, final byte[] value);


    /**
     * LIST
     * @param dbNum
     * @param key
     * @param value
     * @return
     */
    Long lpush(String replid,long offset,final Long dbNum,final  byte[]key,final byte[]...value);
    Long lpush(String replid,long offset,final Long dbNum,final byte[]key,long ms,final byte[]...value);

    Long lpush(String replid,long offset,final Long dbNum,final  byte[]key,final List<byte[]> value);
    Long lpush(String replid,long offset,final Long dbNum,final byte[]key,long ms,final List<byte[]>value);

    Long rpush(String replid,long offset,final Long dbNum,final  byte[]key,final byte[]...value);
    Long rpush(String replid,long offset,final Long dbNum,final byte[]key,long ms,final byte[]...value);

    Long rpush(String replid,long offset,final Long dbNum,final  byte[]key,final List<byte[]> value);
    Long rpush(String replid,long offset,final Long dbNum,final byte[]key,long ms,final List<byte[]>value);


    /**
     * SET
     * @param dbNum
     * @param key
     * @param members
     * @return
     */
    Long sadd(String replid,long offset,final Long dbNum,final byte[] key, final byte[]... members);
    Long sadd(String replid,long offset,final Long dbNum,final byte[] key,long ms, final byte[]... members);
    Long sadd(String replid,long offset,final Long dbNum,final byte[] key, final Set<byte[]> members);
    Long sadd(String replid,long offset,final Long dbNum,final byte[] key,long ms, final Set<byte[]> members);


    /**
     * ZSET
     * @param dbNum
     * @param key
     * @param value
     * @return
     */
    Long zadd(String replid,long offset,final Long dbNum,byte[]key, Set<ZSetEntry> value);
    Long zadd(String replid,long offset,final Long dbNum, byte[]key, Set<ZSetEntry> value, long ms);

    /**
     * HASH
     * @param dbNum
     * @param key
     * @param hash
     * @return
     */
    String hmset(String replid,long offset,final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash);
    String hmset(String replid,long offset,final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash,long ms);


    /**
     * DUMP
     * @param dbNum
     * @param key
     * @param ttl
     * @param serializedValue
     * @return
     */
    String restore(String replid,long offset,final Long dbNum,final byte[] key, final long ttl, final byte[] serializedValue);
    String restoreReplace(String replid,long offset,final Long dbNum,final byte[] key, final long ttl, final byte[] serializedValue);
    String restoreReplace(String replid,long offset,final Long dbNum,byte[] key, long ttl, byte[] serializedValue,boolean highVersion);
    Object send(String replid,long offset,final byte[] cmd, final byte[]... args);
    void select(String replid,long offset,final Integer dbNum);
    Long pexpire(String replid,long offset,Long dbNum,byte[]key,long ms);
}
