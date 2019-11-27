package syncerservice.syncerplusservice.task.clusterTask.pipelineVersion;

import syncerservice.syncerpluscommon.config.ThreadPoolConfig;
import syncerservice.syncerpluscommon.util.spring.SpringUtil;
import syncerservice.syncerplusredis.entity.RedisURI;
import syncerservice.syncerplusredis.event.Event;
import syncerservice.syncerplusredis.event.EventListener;
import syncerservice.syncerplusredis.event.PostRdbSyncEvent;
import syncerservice.syncerplusredis.event.PreRdbSyncEvent;
import syncerservice.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncerservice.syncerplusredis.replicator.CloseListener;
import syncerservice.syncerplusredis.replicator.RedisReplicator;
import syncerservice.syncerplusredis.replicator.Replicator;
import syncerservice.syncerplusredis.entity.SyncTaskEntity;
import syncerservice.syncerplusredis.entity.dto.RedisClusterDto;
import syncerservice.syncerplusservice.pool.RedisMigrator;
import syncerservice.syncerplusservice.service.command.SendClusterDefaultCommand;

import syncerservice.syncerplusservice.task.singleTask.pipe.cluster.LockPipeCluster;
import syncerservice.syncerplusservice.task.singleTask.pipe.cluster.PipelinedClusterSyncTask;
import syncerservice.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import syncerservice.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import syncerservice.syncerplusservice.util.Jedis.cluster.pipelineCluster.JedisClusterPipeline;
import syncerservice.syncerplusservice.util.RedisUrlUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;


/**
 * RedisCluster 数据迁移同步线程
 * 从原生Cluste集群往JDCloud集群迁移同步
 * redisCluster集群同步分两种情况,redisCluster由于时3.0之后推出的，所以无需考虑restore 无replace 问题
 * 由于JDCloud基于2.8版本，所以需要考虑restore replace相关问题
 * 只需考虑 跨版本和同版本迁移同步问题
 */
@Slf4j
public class ClusterRdbSameVersionJDCloudPipelineRestoreTask implements Callable<Integer> {

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
    private String sourceUrl;

    private boolean syncStatus = true;
    JedisClusterPipeline pipelined = null;

    private LockPipeCluster lockPipe=new LockPipeCluster();
    private SyncTaskEntity taskEntity = new SyncTaskEntity();

//    private SendClusterDumpKeySameVersionCommand sendDumpKeySameVersionCommand=new SendClusterDumpKeySameVersionCommand();
    private SendClusterDefaultCommand sendDefaultCommand=new SendClusterDefaultCommand();
    public ClusterRdbSameVersionJDCloudPipelineRestoreTask(RedisClusterDto syncDataDto, String sourceUrl) {
        this.syncDataDto = syncDataDto;
        this.threadName = syncDataDto.getTaskName();
        this.sourceUrl=sourceUrl;
        if (status) {
            this.status = false;
        }
    }


    @Override
    public Integer call() throws Exception {



        //设线程名称
        Thread.currentThread().setName(threadName);

        RedisURI suri = null;
        try {
            suri = new RedisURI(sourceUrl);

            SyncJedisClusterClient pool=RedisUrlUtils.getConnectionClusterPool(syncDataDto);


            redisClient=pool.jedisCluster();

            if (pipelined == null) {
                pipelined=new JedisClusterPipeline(redisClient);
//                pipelined.refreshCluster();
//                pipelined = redisClient.pool
            }



            /**
             * 管道的形式
             */
            if (syncStatus) {
                threadPoolTaskExecutor.submit(new PipelinedClusterSyncTask(pipelined, taskEntity,lockPipe));
//                threadPoolTaskExecutor.submit(new PipelinedClusterSumSyncTask(pipelined, taskEntity,lockPipe));

                syncStatus = false;
            }



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

                    if(event instanceof PreRdbSyncEvent){
                        log.info("{} :全量同步启动",threadName);
                    }

                    if(event instanceof PostRdbSyncEvent){
                        log.info("{} :全量同步结束 ",threadName);
                    }

                    if (event instanceof DumpKeyValuePair) {
                        DumpKeyValuePair kv = (DumpKeyValuePair) event;








                        taskEntity.add();


                        if (kv.getExpiredMs() == null) {
                            pipelined.restoreReplace(kv.getKey(),0,kv.getValue());

                        } else {
                            long ms = kv.getExpiredMs() - System.currentTimeMillis();

                            if (ms <= 0) return;

                            int ttl= (int) (ms/1000);
                            pipelined.restoreReplace(kv.getKey(),ttl,kv.getValue());

                        }

                        lockPipe.syncpipe(pipelined,taskEntity,1000,true);


                    }



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
