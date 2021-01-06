package syncer.replica.sentinel.impl;

import syncer.jedis.HostAndPort;
import syncer.jedis.Jedis;
import syncer.jedis.JedisPubSub;
import syncer.jedis.exceptions.JedisException;
import syncer.replica.entity.Configuration;
import syncer.replica.sentinel.Sentinel;
import syncer.replica.sentinel.SentinelListener;
import syncer.replica.util.thread.ConcurrentUtils;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;

import static syncer.replica.util.objectutil.Strings.isEquals;
import static java.lang.Integer.parseInt;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/14
 */
@Slf4j
public class SyncerSentinel implements Sentinel {
    protected final String masterName;
    protected final List<HostAndPort> hosts;
    protected final Configuration configuration;
    protected final String channel = "+switch-master";
    protected final List<SentinelListener> listeners = new CopyOnWriteArrayList<>();
    protected final ScheduledExecutorService schedule = newSingleThreadScheduledExecutor();

    public SyncerSentinel(List<HostAndPort> hosts, String masterName, Configuration configuration) {
        this.hosts = hosts;
        this.masterName = masterName;
        this.configuration = configuration;
    }
    @Override
    public void open() throws IOException {
        schedule.scheduleWithFixedDelay(this::pulse, 0, 10, SECONDS);
    }

    @Override
    public void close() throws IOException {
        ConcurrentUtils.terminateQuietly(schedule, configuration.getConnectionTimeout(), MILLISECONDS);
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
        if (listeners.isEmpty()) {
            return;
        }
        for (SentinelListener listener : listeners) {
            listener.onClose(this);
        }
    }

    protected void doSwitchListener(HostAndPort host) {
        if (listeners.isEmpty()) {
            return;
        }
        for (SentinelListener listener : listeners) {
            listener.onSwitch(this, host);
        }
    }

    protected void pulse() {
        for (HostAndPort sentinel : hosts) {
            try (final Jedis jedis = new Jedis(sentinel)) {
                List<String> list = jedis.sentinelGetMasterAddrByName(masterName);
                if (list == null || list.size() != 2) {
                    throw new JedisException("host: " + list);
                }
                String host = list.get(0);
                int port = Integer.parseInt(list.get(1));
                doSwitchListener(new HostAndPort(host, port));

                log.info("subscribe sentinel {}", sentinel);
                jedis.subscribe(new PubSub(), this.channel);
            } catch (Throwable cause) {
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
                if (!isEquals(prev, next)) {
                    log.error("failed to match master, prev: {}, next: {}", prev, next);
                    return;
                }

                final String host = messages[3];
                final int port = parseInt(messages[4]);
                doSwitchListener(new HostAndPort(host, port));
            } catch (Throwable cause) {
                log.error("failed to subscribe: {}, cause: {}", response, cause);
            }
        }
    }
}
