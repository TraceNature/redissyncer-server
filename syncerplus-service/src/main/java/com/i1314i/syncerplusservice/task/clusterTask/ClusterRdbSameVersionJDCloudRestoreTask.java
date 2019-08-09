package com.i1314i.syncerplusservice.task.clusterTask;

import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.service.command.SendClusterDefaultCommand;
import com.i1314i.syncerplusservice.task.clusterTask.cluster.SendClusterDumpKeySameVersionCommand;
import com.i1314i.syncerplusservice.task.clusterTask.command.ClusterProtocolCommand;
import com.i1314i.syncerplusservice.util.Jedis.IJedisClient;
import com.i1314i.syncerplusservice.util.Jedis.ObjectUtils;
import com.i1314i.syncerplusservice.util.Jedis.StringUtils;
import com.i1314i.syncerplusservice.util.Jedis.cluster.JedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import com.moilioncircle.redis.replicator.CloseListener;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;

import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;

import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.datatype.Module;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * RedisCluster 数据迁移同步线程
 * 从原生Cluste集群往JDCloud集群迁移同步
 * redisCluster集群同步分两种情况,redisCluster由于时3.0之后推出的，所以无需考虑restore 无replace 问题
 * 由于JDCloud基于2.8版本，所以需要考虑restore replace相关问题
 * 只需考虑 跨版本和同版本迁移同步问题
 */
@Slf4j
public class ClusterRdbSameVersionJDCloudRestoreTask implements Callable<Integer> {

    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;

    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }

    private boolean status = true;
    private String threadName; //线程名称

    private RedisClusterDto syncDataDto;
    private JedisClusterPlus redisClient;
    private SendClusterDumpKeySameVersionCommand sendDumpKeySameVersionCommand=new SendClusterDumpKeySameVersionCommand();
    private SendClusterDefaultCommand sendDefaultCommand=new SendClusterDefaultCommand();
    public ClusterRdbSameVersionJDCloudRestoreTask(RedisClusterDto syncDataDto) {
        this.syncDataDto = syncDataDto;
        this.threadName = syncDataDto.getThreadName();
        if (status) {
            this.status = false;
        }
    }


    @Override
    public Integer call() throws Exception {



        //设线程名称
        Thread.currentThread().setName(threadName);
        TaskMonitorUtils.addAliveThread(Thread.currentThread().getName(), Thread.currentThread());
        RedisURI suri = null;
        try {
            suri = new RedisURI(String.valueOf(syncDataDto.getSourceUris().toArray()[0]));

            System.out.println(syncDataDto.getTargetRedisAddress());
            SyncJedisClusterClient pool=RedisUrlUtils.getConnectionClusterPool(syncDataDto);

            redisClient=pool.jedisCluster();







            /**
             * 初始化连接池
             */
            Replicator r = RedisMigrator.dress(new RedisReplicator(suri));

            /**
             * RDB复制
             */
            r.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {
                    /**
                     * 全量同步
                     */

                    sendDumpKeySameVersionCommand.sendRestoreDumpData(event,r,redisClient,threadPoolTaskExecutor,threadName);

                    /**
                     * 命令同步
                     */
                    sendDefaultCommand.sendDefaultCommand(event,r,redisClient,threadPoolTaskExecutor);
                }
            });


            r.addCloseListener(new CloseListener() {
                @Override
                public void handle(Replicator replicator) {


                }
            });


            try {
                r.open();
            }catch (Exception e){
                System.out.println("---------------异常"+e.getMessage());
            }




        } catch (URISyntaxException e) {
            log.info("redis address is error:{%s} ", e.getMessage());
        } catch (IOException e) {
            log.info("redis address is error:{%s} ", e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("-------------------异常"+e.getMessage());
        }

        return null;

    }





}
