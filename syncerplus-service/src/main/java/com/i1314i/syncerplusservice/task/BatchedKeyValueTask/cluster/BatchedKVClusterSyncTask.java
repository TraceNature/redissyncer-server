package com.i1314i.syncerplusservice.task.BatchedKeyValueTask.cluster;

import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusservice.entity.SyncTaskEntity;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.service.command.SendClusterDefaultCommand;
import com.i1314i.syncerplusservice.service.command.SendDefaultCommand;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import com.moilioncircle.redis.replicator.CloseListener;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.rdb.iterable.ValueIterableEventListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

@Getter
@Setter
@Slf4j
public class BatchedKVClusterSyncTask implements Runnable {

    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;


    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }
    private SendClusterDefaultCommand sendDefaultCommand=new SendClusterDefaultCommand();
    private JedisClusterPlus redisClient;
    private String sourceUrl;  //源redis地址
    private int threadCount = 30;  //写线程数
    private boolean status = true;
    private String threadName; //线程名称
    private RedisClusterDto syncDataDto;
    private Date startTime = new Date();
    private boolean syncStatus = true;


    private RdbClusterCommand sendDumpKeyDiffVersionCommand=new RdbClusterCommand();
    public BatchedKVClusterSyncTask(RedisClusterDto syncDataDto,String sourceUrl) {
        this.syncDataDto = syncDataDto;
        this.sourceUrl=sourceUrl;
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
            suri = new RedisURI(sourceUrl);


            /**
             * 初始化连接池
             */

            Replicator r = RedisMigrator.bacthedCommandDress(new RedisReplicator(suri));
             SyncJedisClusterClient poolss=RedisUrlUtils.getConnectionClusterPool(syncDataDto);

            redisClient=poolss.jedisCluster();

            /**
             * RDB复制
             */
            r.addEventListener(new ValueIterableEventListener(1000,new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {
                    RedisUrlUtils.doCheckTask(r, Thread.currentThread());
                    if (RedisUrlUtils.doThreadisCloseCheckTask())
                        return;

                    /**
                     * 全量同步
                     */

                    sendDumpKeyDiffVersionCommand.sendRestoreDumpData(event,r,threadPoolTaskExecutor,redisClient,threadName,syncDataDto.getDbNum());



                    /**
                     * 命令同步
                     */
                    sendDefaultCommand.sendDefaultCommand(event,r,redisClient,threadPoolTaskExecutor);


                }
            }));


            r.addCloseListener(new CloseListener() {
                @Override
                public void handle(Replicator replicator) {
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
