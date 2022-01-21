package syncer.transmission.client.sentinel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.common.constant.BreakpointContinuationType;
import syncer.jedis.HostAndPort;
import syncer.jedis.Jedis;
import syncer.jedis.JedisPubSub;
import syncer.jedis.exceptions.JedisException;
import syncer.replica.sentinel.Sentinel;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.impl.JedisMultiExecPipeLineClient;
import syncer.transmission.client.impl.JedisPipeLineClient;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * 哨兵监听
 */
@Slf4j
public class RedisSentinel {
    protected final String masterName;
    protected final List<HostAndPort> hosts;
    protected final String channel = "+switch-master";
    protected final ScheduledExecutorService schedule = newSingleThreadScheduledExecutor();
    protected final AtomicInteger hostsPulseSize = new AtomicInteger(0);
    protected String password=null;
    protected String sentinelPassword=null;
    private  Sentinel sentinel;
    private RedisClient client;
    //批次数
    protected Integer count = 1000;
    //错误次数
    private long errorCount = 1;

    /**
     * 用于计算检查点的名字
     */
    private String sourceHost;
    private Integer sourcePort;
    private String taskId;


    private BreakpointContinuationType breakpointContinuationType;
    public RedisSentinel(String masterName, List<HostAndPort> hosts) {
        this.masterName = masterName;
        this.hosts = hosts;
    }


    public void open() throws IOException {
        this.sentinel.open();
    }

    public void close() throws IOException {
        this.sentinel.close();
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


    void doSwitchListener(HostAndPort host) {
        if(Objects.nonNull(client)){

        }
        if(BreakpointContinuationType.v1.equals(breakpointContinuationType)){
            client = new JedisPipeLineClient(host.getHost(),host.getPort(),password,count,errorCount,taskId);
        }else {
            client = new JedisMultiExecPipeLineClient(host.getHost(),host.getPort(),password,sourceHost,sourcePort,count,errorCount,taskId);
        }
    }



    protected void pulse() {
        for (HostAndPort sentinel : hosts) {
            try {
                final Jedis jedis = new Jedis(sentinel);
                if (!StringUtils.isEmpty(sentinelPassword)) {
                    jedis.auth(sentinelPassword);
                }
                List<String> list = jedis.sentinelGetMasterAddrByName(masterName);
                if (list == null || list.size() != 2) {
                    int num = hostsPulseSize.incrementAndGet();
                    log.error("[hosts not find] 请检查[sentinel matser name ]");
                    close();
                    throw new JedisException("host: " + list);
                }
                String host = list.get(0);
                int port = Integer.parseInt(list.get(1));
                doSwitchListener(new HostAndPort(host, port));
                log.info("subscribe sentinel {}", sentinel);
                jedis.subscribe(new PubSub(), this.channel);
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("suspend sentinel {}, cause: {}", sentinel, e.getCause());
            }
        }
    }
}
