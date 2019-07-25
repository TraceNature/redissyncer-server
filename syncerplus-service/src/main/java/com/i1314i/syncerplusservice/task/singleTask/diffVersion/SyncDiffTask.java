package com.i1314i.syncerplusservice.task.singleTask.diffVersion;


import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.entity.SyncTaskEntity;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.task.CommitSendTask;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import com.moilioncircle.redis.replicator.*;

import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;

import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;

import com.moilioncircle.redis.replicator.rdb.datatype.*;
import lombok.Getter;
import lombok.Setter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;


import java.io.IOException;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * 同步不同版本之间的数据(多线程写入每一条数据)
 */
@Getter
@Setter
@Slf4j
public class SyncDiffTask implements Runnable {

    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;

    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }

    private String sourceUri;  //源redis地址
    private String targetUri;  //目标redis地址
    private int threadCount = 30;  //写线程数
    private boolean status = true;
    private String threadName; //线程名称
    private RedisSyncDataDto syncDataDto;
    private Date startTime = new Date();
    private boolean syncStatus = true;
    int commandNum = 0;
    Pipeline pipelined = null;
    private SyncTaskEntity taskEntity = new SyncTaskEntity();


    public SyncDiffTask(RedisSyncDataDto syncDataDto) {
        this.syncDataDto = syncDataDto;
        this.sourceUri = syncDataDto.getSourceUri();
        this.targetUri = syncDataDto.getTargetUri();
        this.threadName = syncDataDto.getThreadName();
        if (status) {
            this.status = false;
        }
    }


    @Override
    public void run() {


        //设线程名称
        Thread.currentThread().setName(threadName);
        TaskMonitorUtils.addAliveThread(Thread.currentThread().getName(), Thread.currentThread());

        RedisURI suri = null;
        try {
            suri = new RedisURI(sourceUri);

            RedisURI turi = new RedisURI(targetUri);
            ConnectionPool pools = RedisUrlUtils.getConnectionPool();
            final ConnectionPool pool = pools;

            /**
             * 初始化连接池
             */
            pool.init(syncDataDto.getMinPoolSize(), syncDataDto.getMaxPoolSize(), syncDataDto.getMaxWaitTime(), turi, syncDataDto.getTimeBetweenEvictionRunsMillis(), syncDataDto.getIdleTimeRunsMillis());
            AtomicInteger sendNum = new AtomicInteger(-1);
            final AtomicInteger dbnum = new AtomicInteger(-1);
            Replicator r = RedisMigrator.commandDress(new RedisReplicator(suri));

            TestJedisClient targetJedisClientPool = RedisUrlUtils.getJedisClient(syncDataDto, turi);

            /**
             * RDB复制
             */


            r.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {

                    if(event instanceof KeyStringValueString) {
                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueString kv = (KeyStringValueString) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        StringBuffer info = new StringBuffer();
                        int index;
                        Jedis targetJedisplus = null;
                        try {
                            targetJedisplus = targetJedisClientPool.getResource();
                        } catch (Exception e) {
                            log.warn("RDB复制：从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                            try {
                                targetJedisplus = targetJedisClientPool.getResource();
                            }catch (Exception ex){
                                log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                            }

                        }

                        if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
                            try {
                                targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                            } catch (Exception e) {
                                log.warn("RDB复制： 从池中获取链接 失败(重试): {}" , e.getMessage());
                                try {
                                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                                }catch (Exception exx){
                                    log.warn("RDB复制： 从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                                }
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }


                        threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.STRING));


                    } else if (event instanceof KeyStringValueList) {
                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueList kv = (KeyStringValueList) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        StringBuffer info = new StringBuffer();
                        int index;
                        Jedis targetJedisplus = null;
                        try {
                            targetJedisplus = targetJedisClientPool.getResource();
                        } catch (Exception e) {
                            log.warn("RDB复制：从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                            try {
                                targetJedisplus = targetJedisClientPool.getResource();
                            }catch (Exception ex){
                                log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                            }

                        }

                        if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
                            try {
                                targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                            } catch (Exception e) {
                                log.warn("RDB复制： 从池中获取链接 失败(重试): {}" , e.getMessage());
                                try {
                                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                                }catch (Exception exx){
                                    log.warn("RDB复制： 从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                                }
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }


                        threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.LIST));


                    } else if (event instanceof KeyStringValueSet) {
                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueSet kv = (KeyStringValueSet) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        StringBuffer info = new StringBuffer();
                        int index;
                        Jedis targetJedisplus = null;
                        try {
                            targetJedisplus = targetJedisClientPool.getResource();
                        } catch (Exception e) {
                            log.warn("RDB复制：从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                            try {
                                targetJedisplus = targetJedisClientPool.getResource();
                            }catch (Exception ex){
                                log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                            }

                        }


                        if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
                            try {
                                targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                            } catch (Exception e) {
                                log.warn("RDB复制： 从池中获取链接 失败(重试): {}" , e.getMessage());
                                try {
                                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                                }catch (Exception exx){
                                    log.warn("RDB复制： 从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                                }
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }


                        threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.SET));



                    }else if (event instanceof KeyStringValueZSet) {
                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueZSet kv = (KeyStringValueZSet) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        StringBuffer info = new StringBuffer();
                        int index;
                        Jedis targetJedisplus = null;
                        try {
                            targetJedisplus = targetJedisClientPool.getResource();
                        } catch (Exception e) {
                            log.warn("RDB复制：从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                            try {
                                targetJedisplus = targetJedisClientPool.getResource();
                            }catch (Exception ex){
                                log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                            }

                        }

                        if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {

                            try {
                                targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                            } catch (Exception e) {
                                log.warn("RDB复制： 从池中获取链接 失败(重试): {}" , e.getMessage());
                                try {
                                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                                }catch (Exception exx){
                                    log.warn("RDB复制： 从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                                }
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }


                        threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.ZSET));



                    }else if (event instanceof KeyStringValueHash) {

                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueHash kv = (KeyStringValueHash) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        StringBuffer info = new StringBuffer();
                        int index;
                        Jedis targetJedisplus = null;
                        try {
                            targetJedisplus = targetJedisClientPool.getResource();
                        } catch (Exception e) {
                            log.warn("RDB复制：从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                            try {
                                targetJedisplus = targetJedisClientPool.getResource();
                            }catch (Exception ex){
                                log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                            }

                        }


                        if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
                            try {
                                targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                            } catch (Exception e) {
                                log.warn("RDB复制： 从池中获取链接 失败(重试): {}" , e.getMessage());
                                try {
                                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                                }catch (Exception exx){
                                    log.warn("RDB复制： 从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                                }
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }


                        threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.HASH));




                    } else if (event instanceof KeyStringValueModule) {
                        log.warn("暂不支持Module");
                    } else if (event instanceof KeyStringValueStream) {
                        log.warn("暂不支持Stream");
                    }






                    /**
                     * 命令同步
                     */
                    if (event instanceof DefaultCommand) {
                        // Step3: sync aof command
                        RedisUrlUtils.doCommandCheckTask(r);
                        if (RedisUrlUtils.doThreadisCloseCheckTask()) {
                            return;
                        }


                        RedisClient redisClient = null;
                        try {
                            redisClient = pool.borrowResource();
                        } catch (Exception e) {
                            log.info("命令复制:从池中获取RedisClient失败:{}" , e.getMessage());

                        }
                        StringBuffer info = new StringBuffer();

//                        SuperCommand superCommand= (SuperCommand) event;
                        // Step3: sync aof command
                        DefaultCommand dc = (DefaultCommand) event;

                        threadPoolTaskExecutor.submit(new CommitSendTask(dc, redisClient, pool, info));
                    }


                }
            });


            r.addCloseListener(new CloseListener() {
                @Override
                public void handle(Replicator replicator) {
                    if (targetJedisClientPool != null) {
                        targetJedisClientPool.closePool();
                    }



                    if (pool != null) {
                        pool.close();
                    }

                }
            });
            r.open();

        } catch (URISyntaxException e) {
            log.info("redis address is error:{%s} ", e.getMessage());
        } catch (IOException e) {
            log.info("redis address is error:{%s} ", e.getMessage());
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


}
