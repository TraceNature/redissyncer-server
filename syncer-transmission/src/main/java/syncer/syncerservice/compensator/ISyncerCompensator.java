package syncer.syncerservice.compensator;

import syncer.syncerplusredis.rdb.datatype.ZSetEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 补偿机制
 */
public interface ISyncerCompensator {

    //STRING
    void set(final Long dbNum,final byte[]key,final byte[]value,String res);
    void set(final Long dbNum,final byte[]key,final byte[]value,long ms,String res);
    void append(final Long dbNum,final byte[] key, final byte[] value,String res);
    //LIST
    void lpush(final Long dbNum,final  byte[]key,final byte[][]value,Long res);
    void lpush(final Long dbNum,final byte[]key,long ms,final byte[][]value,Long res);

    void lpush(final Long dbNum,final  byte[]key,final List<byte[]> value,Long res);
    void lpush(final Long dbNum,final byte[]key,long ms,final List<byte[]>value,Long res);

    //SET
    void sadd(final Long dbNum,final byte[] key, final byte[][] members,Long res);
    void sadd(final Long dbNum,final byte[] key,long ms, final byte[][] members,Long res);
    void sadd(final Long dbNum,final byte[] key, final Set<byte[]> members,Long res);
    void sadd(final Long dbNum,final byte[] key,long ms, final Set<byte[]> members,Long res);
    //ZSET
    void zadd(final Long dbNum,byte[]key, Set<ZSetEntry> value,Long res);
    void zadd(final Long dbNum,byte[]key,Set<ZSetEntry> value,long ms,Long res);
    //HASH
    void hmset(final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash,String res);
    void hmset(final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash,long ms,String res);
    //DUMP
    void restore(final Long dbNum,final byte[] key, final int ttl, final byte[] serializedValue,String res);
    void restoreReplace(final Long dbNum,final byte[] key, final int ttl, final byte[] serializedValue,String res);
    void restoreReplace(final Long dbNum,byte[] key, int ttl, byte[] serializedValue,boolean highVersion,String res);
    void send(final byte[] cmd,Object res, final byte[]... args);
    void select(final Integer dbNum);


}
