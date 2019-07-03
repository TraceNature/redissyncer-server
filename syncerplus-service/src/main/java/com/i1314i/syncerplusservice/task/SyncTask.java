package com.i1314i.syncerplusservice.task;

import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.Impl.ConnectionPoolImpl;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import com.moilioncircle.redis.replicator.*;
import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.cmd.CommandListener;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.rdb.RdbListener;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import static redis.clients.jedis.Protocol.Command.SELECT;
import static redis.clients.jedis.Protocol.toByteArray;


@Getter
@Setter
@Slf4j
public class SyncTask implements Runnable {

    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;

    static {
        threadPoolConfig= SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor=threadPoolConfig.threadPoolTaskExecutor();
    }

    private String sourceUri;  //源redis地址
    private String targetUri;  //目标redis地址
    private  int threadCount = 30;  //写线程数
    private boolean status=true;
    private String threadName; //线程名称

    public SyncTask(String sourceUri, String targetUri) {
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
        this.threadName=Thread.currentThread().getName();
        if(status){
            this.status=false;
        }
    }

    public SyncTask(String sourceUri, String targetUri,String threadName) {
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
        if(StringUtils.isEmpty(threadName)){
            this.threadName=Thread.currentThread().getName();
        }else {
            this.threadName=threadName;
        }
        if(status){
            this.status=false;
        }
    }

    public SyncTask(String sourceUri, String targetUri, int threadCount) {
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
        this.threadCount = threadCount;
        this.threadName=Thread.currentThread().getName();
        if(status){
            this.status=false;
        }
    }

    public SyncTask(String sourceUri, String targetUri, int threadCount,String threadName) {
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
        this.threadCount = threadCount;
        if(StringUtils.isEmpty(threadName)){
            this.threadName=Thread.currentThread().getName();
        }else {
            this.threadName=threadName;
        }

        if(status){
            this.status=false;
        }
    }

    @Override
    public void run() {


        //设线程名称
        Thread.currentThread().setName(threadName);
        TaskMonitorUtils.addAliveThread(Thread.currentThread().getName(),Thread.currentThread());


        RedisURI suri = null;
        try {
            suri = new RedisURI(sourceUri);

            RedisURI turi = new RedisURI(targetUri);

             final ConnectionPool pool = new ConnectionPoolImpl();

            /**
             * 初始化连接池
             */
            pool.init(10, 2000L,turi);

            Configuration tconfig = Configuration.valueOf(turi);

            final AtomicInteger dbnum = new AtomicInteger(-1);
            Replicator r = RedisMigrator.dress(new RedisReplicator(suri));


            /**
             * RDB复制
             */
            r.addRdbListener(new RdbListener.Adaptor() {

                @Override
                public void handle(Replicator replicator, KeyValuePair<?> kv) {

                    doCheckTask(r);

                    StringBuffer info = new StringBuffer();
                    if (!(kv instanceof DumpKeyValuePair)) return;
                    // Step1: select db
                    DB db = kv.getDb();
                    int index;
                    boolean status = false;
                    RedisClient redisClient = null;
                    try {
                         redisClient=pool.borrowResource();
                    } catch (Exception e) {
                        log.info("从池中获取RedisClient失败%s",e.getMessage());

                    }
                    if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
                        status = true;


                        try {
                            pool.borrowResource().send(SELECT, toByteArray(index));
                        } catch (Exception e) {
                            log.info("从池中获取链接失败");
                        }
                        dbnum.set(index);
                        info.append("SELECT:");
                        info.append(index);
                        log.info(info.toString());
                    }

                    info.setLength(0);
                    //threadPoolTaskExecutor.execute(new SyncTask(replicator,kv,target,dbnum));
                    // Step2: restore dump data
                    DumpKeyValuePair mkv = (DumpKeyValuePair) kv;
                    if (mkv.getExpiredMs() == null) {

                     //   Object r = redisClient.restore(mkv.getRawKey(), 0L, mkv.getValue(), true);
//                        Object r =threadPoolTaskExecutor.submit(new RdbRestoreTask(mkv, 0L,redisClient,pool ,true));
                          threadPoolTaskExecutor.submit(new RdbRestoreTask(mkv, 0L,redisClient,pool ,true,info));
//                        info.append(mkv.getKey());
//                        info.append("->");
//                        info.append(r.toString());
//                        log.info(info.toString());
                    } else {
                        long ms = mkv.getExpiredMs() - System.currentTimeMillis();
                        if (ms <= 0) return;

                   //     Object r = redisClient.restore(mkv.getRawKey(), ms, mkv.getValue(), true);
//                        Object r =threadPoolTaskExecutor.submit(new RdbRestoreTask(mkv, ms, redisClient,pool, true,info));
                      threadPoolTaskExecutor.submit(new RdbRestoreTask(mkv, ms, redisClient,pool, true,info));
//                        info.append(mkv.getKey());
//                        info.append("->");
//                        info.append(r.toString());
//                        log.info(info.toString());
                    }


                }
            });

            /**
             * 命令复制
             */
            r.addCommandListener(new CommandListener() {
                @Override
                public void handle(Replicator replicator, Command command) {
                    doCheckTask(r);
                    if (!(command instanceof DefaultCommand)) return;

                    RedisClient redisClient = null;
                    try {
                        redisClient=pool.borrowResource();
                    } catch (Exception e) {
                        log.info("从池中获取RedisClient失败%s",e.getMessage());

                    }
                    StringBuffer info = new StringBuffer();
                    // Step3: sync aof command
                    DefaultCommand dc = (DefaultCommand) command;
//                    Object r = threadPoolTaskExecutor.submit(new CommitSendTask(dc,redisClient,pool));
                     threadPoolTaskExecutor.submit(new CommitSendTask(dc,redisClient,pool,info));
                    //Object r = target.send(dc.getCommand(), dc.getArgs());

                    /**
                    info.append(new String(dc.getCommand()));
                    info.append(":");

                    for (byte[] arg : dc.getArgs()) {
                        info.append("[");
                        info.append(new String(arg));
                        info.append("]");
                    }

                    info.append("->");
                    info.append(r);
                    log.info(info.toString());

                     **/
                }
            });
            r.addCloseListener(new CloseListener() {
                @Override
                public void handle(Replicator replicator) {
//                    if(null!=pool)
//                    pool.close();
                }
            });
            r.open();

        } catch (URISyntaxException e) {
            log.info("redis address is error:%s ", e.getMessage());
        } catch (IOException e) {
            log.info("redis address is error:%s ", e.getMessage());
        }
    }


    public synchronized void doCheckTask(Replicator r){
        /**
         * 当aliveMap中不存在此线程时关闭
         */
        if(!TaskMonitorUtils.getAliveThreadHashMap().containsKey(Thread.currentThread().getName())){
            try {
                System.out.println("线程正准备关闭....");
                if(!Thread.currentThread().isInterrupted()){
                    Thread.currentThread().interrupt();
                }

                r.close();
                /**
                 * 清楚所有线程记录
                 */
                TaskMonitorUtils.removeAliveThread(Thread.currentThread().getName(),Thread.currentThread());
            } catch (IOException e) {
                TaskMonitorUtils.addDeadThread(Thread.currentThread().getName(),Thread.currentThread());
                log.info("数据同步关闭失败");
                e.printStackTrace();
            }
        }

    }
}
