package syncer.syncerservice.util.JDRedisClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;

public class JDRedisRedissonClient implements JDRedisClient {

    public JDRedisRedissonClient() {
        Config config = new Config();
        config.setCodec(new ByteArrayCodec());
        SingleServerConfig singleSerververConfig = config.useSingleServer();
        singleSerververConfig.setAddress("redis://127.0.0.1:6379");

    }

    @Override
    public String get(final Long dbNum,byte[] key) {
        return null;
    }

    @Override
    public String get(final Long dbNum,String key) {
        return null;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        return null;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        return null;
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        return null;
    }



    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        return null;
    }


    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        return null;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        return null;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        return null;
    }


    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        return null;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        return null;
    }


    @Override
    public String restore(Long dbNum, byte[] key, int ttl, byte[] serializedValue) {
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, int ttl, byte[] serializedValue) {
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, int ttl, byte[] serializedValue, boolean highVersion) {
        return null;
    }

    @Override
    public Object send(byte[] cmd, byte[]... args) {
        return null;
    }

    @Override
    public void select(Integer dbNum) {

    }
}
