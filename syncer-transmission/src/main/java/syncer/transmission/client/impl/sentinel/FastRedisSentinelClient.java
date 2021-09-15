package syncer.transmission.client.impl.sentinel;

import lombok.extern.slf4j.Slf4j;
import syncer.common.util.ThreadPoolUtils;
import syncer.jedis.HostAndPort;
import syncer.jedis.Jedis;
import syncer.transmission.client.impl.ConnectErrorRetry;
import syncer.transmission.client.impl.JedisPipeLineClient;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class FastRedisSentinelClient extends JedisPipeLineClient {
    FastRedisSentinelClient.CheckFailover pool;
    private String masterName;
    private String currentMaster;
    private AtomicBoolean status;
    private String sentileHost;
    public FastRedisSentinelClient(String sentileHost, String password, int count, long errorCount,String masterName, String taskId) {
        this.sentileHost = sentileHost;
        this.taskId = taskId;
        this.password=password;
        this.masterName=masterName;
        if (count != 0) {
            this.count = count;
        }

        if (errorCount >= -1L) {
            this.errorCount = errorCount;
        }
        pool=new FastRedisSentinelClient.CheckFailover(masterName, hostAndportSet(), password,10000, 10000 );
        pool.initClient(pool.getCurrentHostMaster());
        retry=new ConnectErrorRetry(taskId);
        //定时回收线程
        ThreadPoolUtils.exec(new FastRedisSentinelClient.PipelineSubmitThread(taskId));
    }

    protected Set<String> hostAndportSet(){
        return Arrays.stream(sentileHost.split(";")).filter(hs->{
            return Objects.nonNull(hs);
        }).distinct().collect(Collectors.toSet());
    }

    class CheckFailover extends SentinelFailOverListener{
        public CheckFailover(String masterName, Set<String> sentinels, String password, int sentinelConnectionTimeout, int sentinelSoTimeout) {
            super(masterName, sentinels, password, sentinelConnectionTimeout, sentinelSoTimeout);
        }

        public CheckFailover(String masterName, Set<String> sentinels, String password) {
            super(masterName, sentinels, password);
        }



        @Override
        protected void initClient(HostAndPort master) {
            synchronized(initPoolLock){
                if (!master.equals(currentHostMaster)) {
                    currentHostMaster = master;
                    log.info("Created Sentinel to master at " + master);
                    if(Objects.isNull(pipelined)){
                        targetClient=createJedis(master.getHost(), master.getPort(),password);
                        pipelined = targetClient.pipelined();
                        log.warn("[{}] connected to [{}:{}]",taskId,master.getHost(),master.getPort());
                        currentMaster=master.getHost()+":"+master.getPort();
                        host=master.getHost();
                        port=master.getPort();
                    }else {
                        commitLock.lock();
                        try {
                            if (null!=currentMaster&&!currentMaster.equals(master.getHost()+":"+master.getPort())) {
                                submitCommandNumNow();
                                pipelined.close();
                                targetClient.close();
                                targetClient=createJedis(master.getHost(), master.getPort(),password);
                                pipelined = targetClient.pipelined();
                                host=master.getHost();
                                port=master.getPort();
                                log.warn("[{}] failover from [{}] to [{}:{}]",taskId,currentMaster,master.getHost(),master.getPort());
                                currentMaster=master.getHost()+":"+master.getPort();
                            }

                        }finally {
                            commitLock.unlock();
                        }
                    }
                }
            }
        }

        protected Jedis getClient(){
            if(Objects.isNull(targetClient)){
                initClient(getCurrentHostMaster());
            }
            return targetClient;
        }

    }

    /**
     * 超时自动提交线程
     */
    //死锁
    class PipelineSubmitThread implements Runnable {
        String taskId;
        private boolean status = true;
        private boolean startStatus = true;
        public PipelineSubmitThread(String taskId) {
            this.taskId = taskId;
        }

        @Override
        public void run() {
            Thread.currentThread().setName(taskId + ": " + Thread.currentThread().getName());
            while (true) {
                compensatorLock.lock();
                try {
                    submitCommandNum();
                    if (SingleTaskDataManagerUtils.isTaskClose(taskId) && taskId != null) {
                        log.warn("task[{}]数据传输模块进入关闭保护状态,不再接收新数据", taskId);
                        Date time = new Date(date.getTime());
                        if (status) {
                            while (System.currentTimeMillis() - time.getTime() < 1000 * 5) {
                                submitCommandNum();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Thread.currentThread().interrupt();
                            status = false;
                            addCommandNum();
                            log.warn("task[{}]数据传输模保护状态退出,任务停止", taskId);
                            try {
                                pool.destroy();
                                targetClient.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }

                }finally {
                    compensatorLock.unlock();
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {

                }
            }

        }
    }
}
