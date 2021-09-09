package syncer.transmission.client.impl.sentinel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import syncer.jedis.*;
import syncer.jedis.exceptions.JedisConnectionException;
import syncer.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SentinelFailOverListener {
    protected String password;


    protected volatile HostAndPort currentHostMaster;
    protected final Object initPoolLock = new Object();
    protected int sentinelConnectionTimeout;
    protected int sentinelSoTimeout;

    protected Set<MasterListener> masterListeners = new HashSet<MasterListener>();

    public SentinelFailOverListener(String masterName, Set<String> sentinels, final String password,
                             final int sentinelConnectionTimeout, final int sentinelSoTimeout){
        this.password = password;
        this.sentinelConnectionTimeout = sentinelConnectionTimeout;
        this.sentinelSoTimeout = sentinelSoTimeout;
        HostAndPort master = initSentinels(sentinels, masterName);
        initSentinels(sentinels,masterName);
        initClient(master);
    }

    public SentinelFailOverListener(String masterName, Set<String> sentinels, String password) {
        this(masterName, sentinels,password,10000,10000);
    }



    protected void initClient(HostAndPort master) {
        synchronized(initPoolLock){
            if (!master.equals(currentHostMaster)) {
                currentHostMaster = master;
                log.info("Created JedisPool to master at " + master);
            }
        }
    }

    private HostAndPort initSentinels(Set<String> sentinels, String masterName) {
        HostAndPort master = null;
        boolean sentinelAvailable = false;
        log.info("Trying to find master from available Sentinels...");
        for (String sentinel : sentinels) {
            final HostAndPort hap = HostAndPort.parseString(sentinel);

            log.debug("Connecting to Sentinel {}", hap);

            Jedis jedis = null;
            try {
                jedis = new Jedis(hap.getHost(), hap.getPort(), sentinelConnectionTimeout, sentinelSoTimeout);
                if (password != null) {
                    jedis.auth(password);
                }


                List<String> masterAddr = jedis.sentinelGetMasterAddrByName(masterName);

                // connected to sentinel...
                sentinelAvailable = true;

                if (masterAddr == null || masterAddr.size() != 2) {
                    log.warn("Can not get master addr, master name: {}. Sentinel: {}", masterName, hap);
                    continue;
                }

                master = toHostAndPort(masterAddr);
                log.debug("Found Redis master at {}", master);
                break;
            } catch (JedisException e) {
                // resolves #1036, it should handle JedisException there's another chance
                // of raising JedisDataException
                log.warn(
                        "Cannot get master address from sentinel running @ {}. Reason: {}. Trying next one.", hap,
                        e.toString());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        if (master == null) {
            if (sentinelAvailable) {
                // can connect to sentinel, but master name seems to not
                // monitored
                throw new JedisException("Can connect to sentinel, but " + masterName
                        + " seems to be not monitored...");
            } else {
                throw new JedisConnectionException("All sentinels down, cannot determine where is "
                        + masterName + " master is running...");
            }
        }

        log.info("Redis master running at " + master + ", starting Sentinel listeners...");

        for (String sentinel : sentinels) {
            final HostAndPort hap = HostAndPort.parseString(sentinel);
            MasterListener masterListener = new MasterListener(masterName, hap.getHost(), hap.getPort());
            // whether MasterListener threads are alive or not, process can be stopped
            masterListener.setDaemon(true);
            masterListeners.add(masterListener);
            masterListeners.add(masterListener);
            masterListener.start();
        }

        return master;
    }


    public HostAndPort getCurrentHostMaster() {
        return currentHostMaster;
    }



    private HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
        String host = getMasterAddrByNameResult.get(0);
        int port = Integer.parseInt(getMasterAddrByNameResult.get(1));
        return new HostAndPort(host, port);
    }

    protected class MasterListener extends Thread {
        protected String masterName;
        protected String host;
        protected int port;
        protected long subscribeRetryWaitTimeMillis = 5000;
        protected volatile Jedis j;
        protected AtomicBoolean running = new AtomicBoolean(false);
        protected MasterListener() {
        }

        public MasterListener(String masterName, String host, int port) {
            super(String.format("MasterListener-%s-[%s:%d]", masterName, host, port));
            this.masterName = masterName;
            this.host = host;
            this.port = port;
        }

        public MasterListener(String masterName, String host, int port,
                              long subscribeRetryWaitTimeMillis) {
            this(masterName, host, port);
            this.subscribeRetryWaitTimeMillis = subscribeRetryWaitTimeMillis;
        }

        @Override
        public void run() {
            running.set(true);
            while (running.get()) {
                j = new Jedis(host, port);
                try {
                    // double check that it is not being shutdown
                    if (!running.get()) {
                        break;
                    }

                    // code for active refresh
                    List<String> masterAddr = j.sentinelGetMasterAddrByName(masterName);
                    if (masterAddr == null || masterAddr.size() != 2) {
                        log.warn("Can not get master addr, master name: {}. Sentinel: {}:{}.", masterName, host, port);
                    } else {
                        initClient(toHostAndPort(masterAddr));
                    }

                    j.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            log.debug("Sentinel {}:{} published: {}.", host, port, message);

                            String[] switchMasterMsg = message.split(" ");

                            if (switchMasterMsg.length > 3) {

                                if (masterName.equals(switchMasterMsg[0])) {
                                    initClient(toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4])));
                                } else {
                                    log.debug(
                                            "Ignoring message on +switch-master for master name {}, our master name is {}",
                                            switchMasterMsg[0], masterName);
                                }

                            } else {
                                log.error(
                                        "Invalid message received on Sentinel {}:{} on channel +switch-master: {}", host,
                                        port, message);
                            }
                        }
                    }, "+switch-master");

                } catch (JedisException e) {

                    if (running.get()) {
                        log.error("Lost connection to Sentinel at {}:{}. Sleeping 5000ms and retrying.", host,
                                port, e);
                        try {
                            Thread.sleep(subscribeRetryWaitTimeMillis);
                        } catch (InterruptedException e1) {
                            log.error("Sleep interrupted: ", e1);
                        }
                    } else {
                        log.debug("Unsubscribing from Sentinel at {}:{}", host, port);
                    }
                } finally {
                    j.close();
                }
            }
        }

        public void shutdown() {
            try {
                log.debug("Shutting down listener on {}:{}", host, port);
                running.set(false);
                // This isn't good, the Jedis object is not thread safe
                if (j != null) {
                    j.disconnect();
                }
            } catch (Exception e) {
                log.error("Caught exception while shutting down: ", e);
            }
        }
    }

    public void destroy() {
        for (MasterListener m : masterListeners) {
            m.shutdown();
        }
    }
}
