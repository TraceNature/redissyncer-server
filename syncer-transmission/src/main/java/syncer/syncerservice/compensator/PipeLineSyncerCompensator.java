package syncer.syncerservice.compensator;

import syncer.syncerjedis.Jedis;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2019/12/24
 */
public class PipeLineSyncerCompensator implements ISyncerCompensator {


    private Jedis client;

    @Override
    public void set(Long dbNum, byte[] key, byte[] value, String res) {

    }

    @Override
    public void set(Long dbNum, byte[] key, byte[] value, long ms, String res) {

    }

    @Override
    public void append(Long dbNum, byte[] key, byte[] value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, byte[][] value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, byte[][] value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, List<byte[]> value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, List<byte[]> value, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, byte[][] members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, long ms, byte[][] members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, Set<byte[]> members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members, Long res) {

    }

    @Override
    public void zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, Long res) {

    }

    @Override
    public void zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms, Long res) {

    }

    @Override
    public void hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, String res) {

    }

    @Override
    public void hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms, String res) {

    }

    @Override
    public void restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue, String res) {

    }

    @Override
    public void restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, String res) {

    }

    @Override
    public void restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion, String res) {

    }

    @Override
    public void send(byte[] cmd, Object res, byte[]... args) {

    }

    @Override
    public void select(Integer dbNum) {
    }
}
