package com.i1314i.syncerplusservice.task.singleTask.diffVersion.pipeVersion;


import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusservice.entity.SyncTaskEntity;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.task.CommitSendTask;
import com.i1314i.syncerplusservice.task.singleTask.pipe.LockPipe;
import com.i1314i.syncerplusservice.task.singleTask.pipe.PipelinedSumSyncTask;
import com.i1314i.syncerplusservice.task.singleTask.pipe.PipelinedSyncTask;
import com.i1314i.syncerplusservice.util.Jedis.ObjectUtils;
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
import redis.clients.jedis.params.SetParams;
import java.io.IOException;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 同步不同版本之间的数据（管道批量传输....正在修改暂不上线）
 */
@Getter
@Setter
@Slf4j
public class SyncPipeDiffTask implements Runnable {

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

    private LockPipe lockPipe=new LockPipe();
    private String dbindex="-1";
    private Lock lock = new ReentrantLock();
    public SyncPipeDiffTask(RedisSyncDataDto syncDataDto) {
        this.syncDataDto = syncDataDto;
        this.sourceUri = syncDataDto.getSourceUri();
        this.targetUri = syncDataDto.getTargetUri();
        this.threadName = syncDataDto.getThreadName();
        if (status) {
            this.status = false;
        }
    }
    void selectIndex(byte[]index){
        lock.lock();
        try {
            dbindex=new String(index);
        } catch (Exception e) {

        }finally {
            lock.unlock(); //释放锁
        }
    }

    String getIndex(){
        return dbindex;
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
            final AtomicInteger dbnum = new AtomicInteger(-1);

            Replicator r = RedisMigrator.commandDress(new RedisReplicator(suri));

            TestJedisClient targetJedisClientPool = RedisUrlUtils.getJedisClient(syncDataDto, turi);
            final Jedis targetJedisplus  = targetJedisClientPool.getResource();
            if (pipelined == null) {
                pipelined = targetJedisplus.pipelined();
            }



            /**
             * 管道的形式
             */
            if (syncStatus) {
                threadPoolTaskExecutor.submit(new PipelinedSyncTask(pipelined, taskEntity,lockPipe));
//                threadPoolTaskExecutor.execute(new PingTask(targetJedisplus));
//                threadPoolTaskExecutor.submit(new PipelinedSumSyncTask(pipelined, taskEntity));
                syncStatus = false;
            }

            /**
             * RDB复制
             */


            r.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {
                    lockPipe.syncpipe(pipelined,taskEntity,1000,true);

//                    System.out.println(JSON.toJSONString(event));




                    if(event instanceof KeyStringValueString) {
                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueString kv = (KeyStringValueString) event;

                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();

                        int dbbnum= (int) db.getDbNumber();

                        if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                            if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                                dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                            }else {
                                return;
                            }
                        }

                        StringBuffer info = new StringBuffer();
                        int index;
                        try {
//                         redisClient=pool.borrowResource();
//                            targetJedisplus = targetJedisClientPool.getResource();
//                        targetJedisplus=targetJedisClientPool.getResource();
//                            sourceJedisplus = sourceJedisClientPool.getResource();
                            if (pipelined == null) {
                                pipelined = targetJedisplus.pipelined();
                            }
                        } catch (Exception e) {
                            log.info("RDB复制：从池中获取RedisClient失败：{}", e.getMessage());
                        }



                        if (db != null && (index = dbbnum) != dbnum.get()) {
                            status = true;

                            try {
//                            redisClient.send(SELECT, toByteArray(index));
//                            targetJedisplus=targetJedisClientPool.selectDb(index,targetJedisplus);
//                                sourceJedisplus = sourceJedisClientPool.selectDb(index, sourceJedisplus);
                                pipelined.select(index);
                            } catch (Exception e) {
                                log.info("RDB复制： 从池中获取链接 失败: {}", e.getMessage());
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }

                        info.setLength(0);




                            taskEntity.add();
                            //                        commandNum++;
                            if (kv.getExpiredMs() == null) {

                                KeyStringValueString valueString = (KeyStringValueString) event;
                                try {
                                    pipelined.set(valueString.getKey(), valueString.getValue());
                                }catch (Exception e){
                                    log.info(e.getMessage()+new String(valueString.getKey())+":"+new String(valueString.getValue()));
                                }

                            } else {
                                long ms = kv.getExpiredMs() - System.currentTimeMillis();
                                if (ms <= 0) {
                                    log.warn("key: {}", new String(kv.getKey()));
                                } else {
                                        KeyStringValueString valueString = (KeyStringValueString) event;
                                        pipelined.set(valueString.getKey(), valueString.getValue(), new SetParams().px(ms));
                                }

                            }

                    } else if (event instanceof KeyStringValueList) {
                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueList kv = (KeyStringValueList) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        int dbbnum= (int) db.getDbNumber();

                        if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                            if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                                dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                            }else {
                                return;
                            }
                        }
                        StringBuffer info = new StringBuffer();
                        int index;
                        try {
                            if (pipelined == null) {
                                pipelined = targetJedisplus.pipelined();
                            }
                        } catch (Exception e) {
                            log.info("RDB复制： 从池中获取RedisClient失败：{}", e.getMessage());
                        }

                        if (db != null && (index = dbbnum) != dbnum.get()) {
                            status = true;
                            try {
                                pipelined.select(index);
                            } catch (Exception e) {
                                log.info("RDB复制 ： 从池中获取链接 失败: {}", e.getMessage());
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }

                        info.setLength(0);


                        taskEntity.add();
                        //                        commandNum++;
                        if (kv.getExpiredMs() == null) {
                            KeyStringValueList valueList = (KeyStringValueList) event;
                            List<byte[]> datas = valueList.getValue();
                            byte[][] array = new byte[datas.size()][];
                            datas.toArray(array);
                            pipelined.lpush(valueList.getKey(), array);
                        } else {
                            long ms = kv.getExpiredMs() - System.currentTimeMillis();
                            if (ms <= 0) {
                                log.warn("key: {}", new String(kv.getKey()));
                            } else {
                                    KeyStringValueList valueList = (KeyStringValueList) event;
                                    List<byte[]> datas = valueList.getValue();
                                    byte[][] array = new byte[datas.size()][];
                                    datas.toArray(array);
                                    pipelined.lpush(valueList.getKey(), array);
                                    pipelined.pexpire(valueList.getKey(), ms);
                            }

                        }
                    } else if (event instanceof KeyStringValueSet) {
                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueSet kv = (KeyStringValueSet) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        int dbbnum= (int) db.getDbNumber();

                        if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                            if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                                dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                            }else {
                                return;
                            }
                        }
                        StringBuffer info = new StringBuffer();
                        int index;
                        try {
                            if (pipelined == null) {
                                pipelined = targetJedisplus.pipelined();
                            }
                        } catch (Exception e) {
                            log.info("RDB复制：从池中获取RedisClient失败：{}", e.getMessage());
                        }

                        if (db != null && (index = dbbnum) != dbnum.get()) {
                            status = true;
                            try {
                                pipelined.select(index);
                            } catch (Exception e) {
                                log.info("RDB复制： 从池中获取链接 失败: {}", e.getMessage());
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }

                        info.setLength(0);
                        taskEntity.add();

                        if (kv.getExpiredMs() == null) {
                            KeyStringValueSet valueSet = (KeyStringValueSet) event;
                            Set<byte[]> datas = valueSet.getValue();
                            byte[][] array = new byte[datas.size()][];
                            datas.toArray(array);
                            pipelined.sadd(valueSet.getKey(), array);
                        } else {
                            long ms = kv.getExpiredMs() - System.currentTimeMillis();
                            if (ms <= 0) {
                                log.warn("key: {}", new String(kv.getKey()));
                            } else {
                                KeyStringValueSet valueSet = (KeyStringValueSet) event;
                                Set<byte[]> datas = valueSet.getValue();
                                byte[][] array = new byte[datas.size()][];
                                datas.toArray(array);
                                pipelined.sadd(valueSet.getKey(), array);
                                pipelined.pexpire(valueSet.getKey(), ms);
                            }

                        }
                    }else if (event instanceof KeyStringValueZSet) {
                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueZSet kv = (KeyStringValueZSet) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        int dbbnum= (int) db.getDbNumber();

                        if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                            if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                                dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                            }else {
                                return;
                            }
                        }
                        StringBuffer info = new StringBuffer();
                        int index;
                        try {
                            if (pipelined == null) {
                                pipelined = targetJedisplus.pipelined();
                            }
                        } catch (Exception e) {
                            log.info("RDB复制：从池中获取RedisClient失败：{}", e.getMessage());
                        }

                        if (db != null && (index =dbbnum) != dbnum.get()) {
                            status = true;
                            try {
                                pipelined.select(index);
                            } catch (Exception e) {
                                log.info("RDB复制： 从池中获取链接 失败: {}", e.getMessage());
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }

                        info.setLength(0);
                        taskEntity.add();
                        if (kv.getExpiredMs() == null) {
                            KeyStringValueZSet valueZSet = (KeyStringValueZSet) event;
                            Set<ZSetEntry> datas = valueZSet.getValue();
                            Map<byte[], Double> map = new HashMap<>();
                            datas.forEach(zset -> {
                                map.put(zset.getElement(), zset.getScore());
                            });
                            pipelined.zadd(valueZSet.getKey(), map);
                        } else {
                            long ms = kv.getExpiredMs() - System.currentTimeMillis();
                            if (ms <= 0) {
                                log.warn("key: {}", new String(kv.getKey()));
                            } else {
                                KeyStringValueZSet valueZSet = (KeyStringValueZSet) event;
                                Set<ZSetEntry> datas = valueZSet.getValue();
                                Map<byte[], Double> map = new HashMap<>();
                                datas.forEach(zset -> {
                                    map.put(zset.getElement(), zset.getScore());
                                });

                                pipelined.zadd(valueZSet.getKey(), map);
                                pipelined.pexpire(valueZSet.getKey(), ms);
                            }

                        }
                    }else if (event instanceof KeyStringValueHash) {

                        RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                        if (RedisUrlUtils.doThreadisCloseCheckTask())
                            return;
                        KeyStringValueHash kv = (KeyStringValueHash) event;
                        if (kv.getDb() == null)
                            return;
                        DB db = kv.getDb();
                        int dbbnum= (int) db.getDbNumber();

                        if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                            if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                                dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                            }else {
                                return;
                            }
                        }
                        StringBuffer info = new StringBuffer();
                        int index;
                        try {
                            if (pipelined == null) {
                                pipelined = targetJedisplus.pipelined();
                            }
                        } catch (Exception e) {
                            log.info("RDB复制：从池中获取RedisClient失败：{}", e.getMessage());
                        }

                        if (db != null && (index = dbbnum) != dbnum.get()) {
                            status = true;
                            try {
                                pipelined.select(index);
                            } catch (Exception e) {
                                log.info("RDB复制： 从池中获取链接 失败: {}", e.getMessage());
                            }
                            dbnum.set(index);
                            info.append("SELECT:");
                            info.append(index);
                            log.info(info.toString());
                        }

                        info.setLength(0);
                        taskEntity.add();
                        if (kv.getExpiredMs() == null) {
                            KeyStringValueHash valueHash = (KeyStringValueHash) event;
                            pipelined.hmset(valueHash.getKey(), valueHash.getValue());
                        } else {
                            long ms = kv.getExpiredMs() - System.currentTimeMillis();
                            if (ms <= 0) {
                                log.warn("key: {}", new String(kv.getKey()));
                            } else {
                                KeyStringValueHash valueHash = (KeyStringValueHash) event;
                                pipelined.hmset(valueHash.getKey(), valueHash.getValue());
                                pipelined.pexpire(valueHash.getKey(), ms);
                            }

                        }


                    } else if (event instanceof KeyStringValueModule) {
                        log.warn("暂不支持Module");
                    } else if (event instanceof KeyStringValueStream) {
                        log.warn("暂不支持Stream");
                    }






                        /**
                         * 多线程方式
                         */
//                    if (mkv.getExpiredMs() == null) {
//                          threadPoolTaskExecutor.submit(new RdbDiffVersionRestoreTask(mkv, 0L,redisClient,pool ,info,targetJedisplus,sourceJedisplus));
//                    } else {
//                        long ms = mkv.getExpiredMs() - System.currentTimeMillis();
//                        if (ms <= 0) return;
//                      threadPoolTaskExecutor.submit(new RdbDiffVersionRestoreTask(mkv, ms, redisClient,pool, info,targetJedisplus,sourceJedisplus));
//                    }

//                    targetJedisplus.pfadd()


                    /**
                     * 命令同步
                     */

//            new SyncerCommandListener(r,pool,threadPoolTaskExecutor).run();


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

                        if(new String(dc.getCommand()).trim().toUpperCase().equals("SELECT")){
                            Long dbnum=Long.parseLong(new String(dc.getArgs()[0]));
                            if(syncDataDto.getDbNum().containsKey(dbnum)){
                                int newdbNum=syncDataDto.getDbNum().get(dbnum);
                                selectIndex(ObjectUtils.getBytesKey(String.valueOf(newdbNum)));
                            }else {
                                selectIndex(dc.getArgs()[0]);
                            }
//                            selectIndex(dc.getArgs()[0]);
                        }else {

                            if(getDbindex().equals("-1")){
                                threadPoolTaskExecutor.submit(new CommitSendTask(dc, redisClient, pool, info,"0"));
                            }else {

                                threadPoolTaskExecutor.submit(new CommitSendTask(dc, redisClient, pool, info,getIndex()));
                            }

                        }
                    }

//                    else if(event instanceof PFAddCommand){
//                        PFAddCommand pfAddCommand= (PFAddCommand) event;
//                        pipelined.pfadd(pfAddCommand.getKey(),pfAddCommand.getElements());
//                        pipelined.sync();
//                        log.info("[{}]->[{}]",new String(pfAddCommand.getKey()));
//                    }
                }
            });


            r.addCloseListener(new CloseListener() {
                @Override
                public void handle(Replicator replicator) {
                    if (targetJedisClientPool != null) {
                        targetJedisClientPool.closePool();
                    }


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
