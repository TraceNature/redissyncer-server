package syncer.transmission.client.sentinel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.jedis.HostAndPort;
import syncer.jedis.Jedis;
import syncer.jedis.JedisPubSub;
import syncer.jedis.exceptions.JedisException;
import syncer.replica.sentinel.SentinelListener;
import syncer.replica.sentinel.SyncerRedisSentinel;
import syncer.replica.util.strings.Strings;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
    public RedisSentinel(String masterName, List<HostAndPort> hosts) {
        this.masterName = masterName;
        this.hosts = hosts;
    }


    public void open() throws IOException {

    }

    public void close() throws IOException {

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
