package syncer.transmission.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.util.StringUtils;
import syncer.jedis.HostAndPort;
import syncer.jedis.Jedis;
import syncer.jedis.JedisPubSub;
import syncer.jedis.exceptions.JedisException;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.sentinel.RedisSentinel;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * 哨兵客户端
 *
 * 基于JedisMultiExecPipeLineClient实现
 */
@Slf4j
public class SentinelClient implements RedisClient {
    private JedisMultiExecPipeLineClient client=null;
    private RedisSentinel sentinel=null;
    protected final String masterName;
    protected final List<HostAndPort> hostAndPortList;
    protected final String channel = "+switch-master";
    protected final ScheduledExecutorService schedule = newSingleThreadScheduledExecutor();
    protected String password=null;
    protected String sentinelPassword=null;
    private AtomicInteger swtichCount=new AtomicInteger(0);
    private String taskId;
    public SentinelClient(List<HostAndPort> hostAndPortList, String masterName) {
        this.masterName = masterName;
        this.hostAndPortList = hostAndPortList;
    }


    @Override
    public String get(Long dbNum, byte[] key) {
        return client.get(dbNum,key);
    }

    @Override
    public String get(Long dbNum, String key) {
        return client.get(dbNum,key);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        return client.set(dbNum,key,value);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        return client.set(dbNum,key,value,ms);
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        return client.append(dbNum,key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        return client.lpush(dbNum,key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        return client.lpush(dbNum,key,ms,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        return client.lpush(dbNum,key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        return client.lpush(dbNum,key,ms,value);
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        return client.rpush(dbNum,key,value);
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        return client.rpush(dbNum,key,ms,value);
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, List<byte[]> value) {
        return client.rpush(dbNum,key,value);
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        return client.rpush(dbNum,key,ms,value);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        return client.sadd(dbNum,key,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        return client.sadd(dbNum,key,ms,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        return client.sadd(dbNum,key,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        return client.sadd(dbNum,key,ms,members);
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        return client.zadd(dbNum,key,value);
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        return client.zadd(dbNum,key,value,ms);
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        return client.hmset(dbNum,key,hash);
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        return client.hmset(dbNum,key,hash,ms);
    }

    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return client.restore(dbNum,key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return client.restoreReplace(dbNum,key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        return client.restoreReplace(dbNum,key,ttl,serializedValue,highVersion);
    }

    @Override
    public Object send(byte[] cmd, byte[]... args) {
        return client.send(cmd,args);
    }

    @Override
    public void select(Integer dbNum) {
        client.select(dbNum);
    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        return client.pexpire(dbNum,key,ms);
    }

    @Override
    public void updateLastReplidAndOffset(String replid, long offset) {
        client.updateLastReplidAndOffset(replid,offset);
    }

    @Override
    public void commitCheckPoint() {
        client.commitCheckPoint();
    }


    protected void pulse() {
        for (HostAndPort sentinel : hostAndPortList) {
            try {
                final Jedis jedis = new Jedis(sentinel);
                if (!StringUtils.isEmpty(sentinelPassword)) {
                    jedis.auth(sentinelPassword);
                }
                List<String> list = jedis.sentinelGetMasterAddrByName(masterName);
                swtichCount.incrementAndGet();
                if (list == null || list.size() != 2) {
                    log.error("[hosts not find] 请检查[sentinel matser name ]");
                    close();
                    throw new JedisException("host: " + list);
                }
                String host = list.get(0);
                int port = Integer.parseInt(list.get(1));
                doSwitchListener(new HostAndPort(host, port));
                log.info("subscribe sentinel {}", sentinel);
                jedis.subscribe(new PubSub(), this.channel);
                swtichCount.set(0);
            } catch (Exception e) {
                if(swtichCount.get()>=hostAndPortList.size()){
                    close();
                }
                log.warn("suspend sentinel {}, cause: {}", sentinel, e.getCause());

            }
        }
    }

    public void close() {
        try {
            log.error("[TASKID {}]任务异常关闭...",taskId);
            SingleTaskDataManagerUtils.brokenTask(taskId);
            if(!schedule.isShutdown()){
                schedule.shutdown();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected class PubSub extends JedisPubSub {
        @Override
        public void onMessage(String channel, String response) {
            try {
                final String[] messages = response.split(" ");
                if (messages.length <= 3) {
                    log.error("failed to handle, response: {}", response);
                    return;
                }
                String prev = masterName, next = messages[0];
                if (!Strings.isEquals(prev, next)) {
                    log.error("failed to match master, prev: {}, next: {}", prev, next);
                    return;
                }

                final String host = messages[3];
                final int port = parseInt(messages[4]);
                doSwitchListener(new HostAndPort(host, port));
            } catch (Exception e) {

                log.error("failed to subscribe: {}, cause: {}", response, e.getMessage());
            }
        }
    }


    //client切换操作
    void doSwitchListener(HostAndPort host) {
//        client=
    }

}


