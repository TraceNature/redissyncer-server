package syncer.syncerservice.util.JDRedisClient;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.po.KVPersistenceDataEntity;
import syncer.syncerservice.po.StringCompensatorEntity;
import syncer.syncerservice.util.CommandCompensatorUtils;
import syncer.syncerservice.util.CompensatorUtils;
import syncer.syncerservice.util.EliminationAlgorithm.lru.LruCache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhanenqiang
 * @Description Lettuce pipeline客户端
 * @Date 2020/8/25
 */
public class RedisLettucePipeLineClient implements JDRedisClient {
    private String host;
    private Integer port;

    private Integer currentDbNum = 0;
    //批次数
    private Integer count = 1000;
    //上一次pipeline提交时间记录
    private Date date = new Date();
    //任务id
    private String taskId;

    private Lock commitLock = new ReentrantLock();
    private Lock compensatorLock = new ReentrantLock();
    private AtomicInteger commandNums = new AtomicInteger();
    /**
     * 被抛弃key阈值
     */


    private RedisClient client;


    //错误次数
    private long errorCount = 1;

    private boolean connectError = false;



    //补偿存储
    private KVPersistenceDataEntity kvPersistence = new KVPersistenceDataEntity();
    private CompensatorUtils compensatorUtils = new CompensatorUtils();
    //内存非幂等命令转幂等命令
    private Map<String, Integer> incrMap = new LruCache<>(1000);
    private Map<String, StringCompensatorEntity> appendMap = new LruCache<>(1000);
    private Map<String, Float> incrDoubleMap = new LruCache<>(1000);
    private CommandCompensatorUtils commandCompensatorUtils = new CommandCompensatorUtils();

    public RedisLettucePipeLineClient(String host, Integer port, String password, int count, long errorCount, String taskId) {

        this.host = host;
        this.port = port;
        this.taskId = taskId;
        if (count != 0) {
            this.count = count;
        }

        if (errorCount >= -1L) {
            this.errorCount = errorCount;
        }


        int timeout = 50000;
        RedisURI redisUri = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withPassword(password)
                .withTimeout(Duration.of(30, ChronoUnit.SECONDS))
                .build();

        client = RedisClient.create(redisUri);
//        client.

        //定时回收线程
//        threadPoolTaskExecutor.execute(new JDJedisPipeLineClient.PipelineSubmitThread(taskId));
    }



    @Override
    public String get(Long dbNum, byte[] key) {
        return null;
    }

    @Override
    public String get(Long dbNum, String key) {
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
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        return null;
    }

    @Override
    public Object send(byte[] cmd, byte[]... args) {
        return null;
    }

    @Override
    public void select(Integer dbNum) {

    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        return null;
    }
}
