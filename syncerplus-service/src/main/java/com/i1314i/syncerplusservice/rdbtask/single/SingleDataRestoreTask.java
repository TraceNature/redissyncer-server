package com.i1314i.syncerplusservice.rdbtask.single;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.entity.RedisInfo;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.rdbtask.enums.RedisCommandType;
import com.i1314i.syncerplusservice.rdbtask.single.command.SendRdbCommand;
import com.i1314i.syncerplusservice.replicator.listener.ValueDumpIterableEventListener;
import com.i1314i.syncerplusservice.replicator.service.JDRedisReplicator;
import com.i1314i.syncerplusservice.replicator.visitor.ValueDumpIterableRdbVisitor;
import com.i1314i.syncerplusservice.service.command.SendDefaultCommand;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.moilioncircle.redis.replicator.rdb.iterable.datatype.BatchedKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public class SingleDataRestoreTask implements Runnable {
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
    private SendDefaultCommand sendDefaultCommand=new SendDefaultCommand();
    private double redisVersion;
    private RedisInfo info;
    public SingleDataRestoreTask(RedisSyncDataDto syncDataDto, RedisInfo info) {
        this.syncDataDto = syncDataDto;
        this.sourceUri = syncDataDto.getSourceUri();
        this.targetUri = syncDataDto.getTargetUri();
        this.threadName = syncDataDto.getThreadName();
        this.info=info;

    }



    @Override
    public void run() {

        //设线程名称
        Thread.currentThread().setName(threadName);
        TaskMonitorUtils.addAliveThread(Thread.currentThread().getName(), Thread.currentThread());


        try {
            RedisURI suri = new RedisURI(sourceUri);
            RedisURI turi = new RedisURI(targetUri);
            JDJedisClientPool targetJedisClientPool = RedisUrlUtils.getJDJedisClient(syncDataDto, turi);


            ConnectionPool pools = RedisUrlUtils.getConnectionPool();
            final ConnectionPool pool = pools;

            /**
             * 初始化连接池
             */
            pool.init(syncDataDto.getMinPoolSize(), syncDataDto.getMaxPoolSize(), syncDataDto.getMaxWaitTime(), turi, syncDataDto.getTimeBetweenEvictionRunsMillis(), syncDataDto.getIdleTimeRunsMillis());


            final Replicator r  = RedisMigrator.newBacthedCommandDress(new JDRedisReplicator(suri));
            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r,info.getRdbVersion()));
            r.addEventListener(new ValueDumpIterableEventListener(1000, new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {

                    if (RedisUrlUtils.doThreadisCloseCheckTask()){
                        try {
                            r.close();
                            if(status){
                                status= false;
                                System.out.println("线程正准备关闭...." + Thread.currentThread().getName());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    if (event instanceof PreRdbSyncEvent) {
                        log.info("{} :全量同步启动");
                    }


                    if (event instanceof PostRdbSyncEvent) {
                        log.info("{} :全量同步结束");
                    }

                    if (event instanceof BatchedKeyValuePair<?, ?>) {

                        BatchedKeyValuePair event1 = (BatchedKeyValuePair) event;
                        DB db=event1.getDb();
                        int dbbnum= (int) db.getDbNumber();
                        Long ms;
                        if(event1.getExpiredMs()==null){
                            ms =0L;
                        }else {
                            ms =event1.getExpiredMs()-System.currentTimeMillis();
                        }
                        if (event1.getValue() != null) {

                            if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                                if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                                    dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                                }else {
                                    return;
                                }
                            }

                            try {
                                threadPoolTaskExecutor.submit(new SendRdbCommand(ms, RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,RedisCommandType.getJDJedis(targetJedisClientPool,event,syncDataDto.getDbNum()),new String((byte[]) event1.getKey()),syncDataDto.getRedisVersion()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    if (event instanceof DumpKeyValuePair) {
                        DumpKeyValuePair valuePair = (DumpKeyValuePair) event;

                        if(valuePair.getValue()!=null){
                            Long ms;
                            if(valuePair.getExpiredMs()==null){
                                ms =0L;
                            }else {
                                ms =valuePair.getExpiredMs()-System.currentTimeMillis();
                                System.out.println(ms);
                            }

                            DB db=valuePair.getDb();
                            int dbbnum= (int) db.getDbNumber();

                            if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                                if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                                    dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                                }else {
                                    return;
                                }
                            }
                            try {
                                threadPoolTaskExecutor.submit(new SendRdbCommand(ms, RedisCommandTypeEnum.DUMP,event,RedisCommandType.getJDJedis(targetJedisClientPool,event,syncDataDto.getDbNum()),new String((byte[]) valuePair.getKey()),syncDataDto.getRedisVersion()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }


                    /**
                     * 命令同步
                     */
                    sendDefaultCommand.sendDefaultCommand(event,r,pool,threadPoolTaskExecutor,syncDataDto);


                }
            }));
            r.open();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
