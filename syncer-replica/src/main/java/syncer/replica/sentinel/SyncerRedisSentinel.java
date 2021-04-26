package syncer.replica.sentinel;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.util.StringUtils;
import syncer.jedis.HostAndPort;
import syncer.jedis.Jedis;
import syncer.jedis.JedisPubSub;
import syncer.jedis.exceptions.JedisException;
import syncer.jedis.util.IOUtils;
import syncer.replica.config.ReplicConfig;
import syncer.replica.event.Event;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.listener.EventListener;
import syncer.replica.listener.TaskStatusListener;
import syncer.replica.replication.Replication;
import syncer.replica.replication.SentinelReplication;
import syncer.replica.util.strings.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class SyncerRedisSentinel implements Sentinel {
    protected final String masterName;
    protected final List<HostAndPort> hosts;
    protected final ReplicConfig config;
    protected final String channel = "+switch-master";
    protected final List<SentinelListener> listeners = new CopyOnWriteArrayList<>();
    protected final ScheduledExecutorService schedule = newSingleThreadScheduledExecutor();
    protected final AtomicInteger hostsPulseSize = new AtomicInteger(0);
    private SentinelReplication replication;

    public SyncerRedisSentinel(List<HostAndPort> hosts, String masterName, ReplicConfig config, SentinelReplication replication) {
        this.hosts = hosts;
        this.masterName = masterName;
        this.config = config;
        this.replication = replication;
    }


    @Override
    public void open() throws IOException {
        schedule.scheduleWithFixedDelay(this::pulse, 0, 10, SECONDS);
    }

    @Override
    public void close() throws IOException {
        IOUtils.terminateQuietly(schedule, config.getConnectionTimeout(), MILLISECONDS);
        doCloseListener();
    }


    @Override
    public boolean addSentinelListener(SentinelListener listener) {
        return this.listeners.add(listener);
    }

    @Override
    public boolean removeSentinelListener(SentinelListener listener) {
        return this.listeners.remove(listener);
    }


    protected void doCloseListener() {
        if (listeners.isEmpty()) return;
        for (SentinelListener listener : listeners) {
            listener.onClose(this);
        }
    }

    protected void doSwitchListener(HostAndPort host) {
        if (listeners.isEmpty()) return;
        for (SentinelListener listener : listeners) {
            listener.onSwitch(this, host);
        }
    }

    protected void pulse() {
        for (HostAndPort sentinel : hosts) {
            try {
                final Jedis jedis = new Jedis(sentinel);
                if (!StringUtils.isEmpty(config.getSentinelAuthPassword())) {
                    jedis.auth(config.getSentinelAuthPassword());
                }
                List<String> list = jedis.sentinelGetMasterAddrByName(masterName);
                if (list == null || list.size() != 2) {
                    int num = hostsPulseSize.incrementAndGet();
                    log.error("[hosts not find] 请检查[sentinel matser name ]");
                    replication.broken("[hosts not find] 请检查[sentinel matser name ]");
                    replication.onClose(this);
                    close();
                    //masterName null
                    throw new JedisException("host: " + list);
                }
                String host = list.get(0);
                int port = Integer.parseInt(list.get(1));
                doSwitchListener(new HostAndPort(host, port));

                log.info("subscribe sentinel {}", sentinel);
                jedis.subscribe(new PubSub(), this.channel);
            } catch (Throwable cause) {
                cause.printStackTrace();
                log.warn("suspend sentinel {}, cause: {}", sentinel, cause);
            }
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

    public static void main(String[] args) throws IOException {
        List<HostAndPort> hosts = new ArrayList<>();
        // sentinel hosts
        hosts.add(new HostAndPort("114.67.76.82", 26379));
        hosts.add(new HostAndPort("114.67.76.82", 26380));
        hosts.add(new HostAndPort("114.67.76.82", 26381));
        ReplicConfig config = ReplicConfig.defaultConfig();
//        config.setAuthPassword("123456");
        Replication replication = new SentinelReplication(hosts, "mymaster", config, true);
        replication.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replication replicator, Event event) {
                System.out.println(JSON.toJSONString(event));
            }

            @Override
            public String eventListenerName() {
                return null;
            }
        });

        replication.addTaskStatusListener(new TaskStatusListener() {


            @Override
            public void handler(Replication replication, SyncerTaskEvent event) {
                System.out.println(JSON.toJSONString(event));
            }

            @Override
            public String eventListenerName() {
                return "test";
            }
        });
        replication.open();

//        ReplicConfig config=ReplicConfig.defaultConfig();
//        config.setAuthPassword("123456");
//        SyncerRedisSentinel sentinel=new SyncerRedisSentinel(hosts,"local-master",config);
//        sentinel.addSentinelListener(new SentinelListener() {
//            @Override
//            public void onClose(Sentinel sentinel) {
//            }
//
//            @Override
//            public void onSwitch(Sentinel sentinel, HostAndPort host) {
//                System.out.println(JSON.toJSONString(host));
//            }
//        });
//
//        sentinel.pulse();
//        try {
//            sentinel.open();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
