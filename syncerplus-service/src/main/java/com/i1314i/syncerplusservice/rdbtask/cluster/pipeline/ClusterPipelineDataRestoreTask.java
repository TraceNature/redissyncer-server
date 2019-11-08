package com.i1314i.syncerplusservice.rdbtask.cluster.pipeline;

import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusredis.event.Event;
import com.i1314i.syncerplusredis.event.EventListener;
import com.i1314i.syncerplusredis.exception.IncrementException;
import com.i1314i.syncerplusredis.extend.replicator.listener.ValueDumpIterableEventListener;
import com.i1314i.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import com.i1314i.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import com.i1314i.syncerplusredis.replicator.Replicator;
import com.i1314i.syncerplusredis.entity.RedisInfo;
import com.i1314i.syncerplusredis.entity.SyncTaskEntity;
import com.i1314i.syncerplusredis.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.rdbtask.cluster.command.SendClusterRdbCommand1;

import com.i1314i.syncerplusservice.service.command.SendRDBClusterDefaultCommand;
import com.i1314i.syncerplusredis.exception.TaskMsgException;
import com.i1314i.syncerplusservice.task.BatchedKeyValueTask.cluster.RdbClusterCommand;
import com.i1314i.syncerplusservice.task.singleTask.pipe.cluster.LockPipeCluster;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.i1314i.syncerplusservice.util.Jedis.cluster.pipelineCluster.JedisClusterPipeline;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusredis.util.TaskMsgUtils;

import com.i1314i.syncerplusservice.util.SyncTaskUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class ClusterPipelineDataRestoreTask implements Runnable {
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
    private RedisClusterDto syncDataDto;
    private SendRDBClusterDefaultCommand sendDefaultCommand=new SendRDBClusterDefaultCommand();
    private RdbClusterCommand sendDumpKeyDiffVersionCommand=new RdbClusterCommand();
    private  JedisClusterPlus redisClient;
    private double redisVersion;

    JedisClusterPipeline pipelined = null;
    private LockPipeCluster lockPipe=new LockPipeCluster();
    private SyncTaskEntity taskEntity = new SyncTaskEntity();

    SendClusterRdbCommand1 clusterRdbCommand=new SendClusterRdbCommand1();
    private RedisInfo info;
    private String taskId;
    public ClusterPipelineDataRestoreTask(RedisClusterDto syncDataDto, RedisInfo info, String sourceUri, String taskId) {
        this.syncDataDto = syncDataDto;
        this.sourceUri=sourceUri;
        this.threadName = syncDataDto.getTaskName();
        this.info=info;
        this.taskId=taskId;
    }



    @Override
    public void run() {

        //设线程名称
        Thread.currentThread().setName(threadName);



        try {
            RedisURI suri = new RedisURI(sourceUri);
            SyncJedisClusterClient poolss=RedisUrlUtils.getConnectionClusterPool(syncDataDto);

            SyncJedisClusterClient pool = RedisUrlUtils.getConnectionClusterPool(syncDataDto);

            redisClient = pool.jedisCluster();

            if (pipelined == null) {
                pipelined=new JedisClusterPipeline(redisClient);
            }

            final Replicator r  = RedisMigrator.newBacthedCommandDress(new JDRedisReplicator(suri));
            TaskMsgUtils.getThreadMsgEntity(taskId).addReplicator(r);

            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r,info.getRdbVersion()));
            r.addEventListener(new ValueDumpIterableEventListener(1000, new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {


//                    lockPipe.syncpipe(lockPipe, taskEntity, 1000, true,suri,turi);
                    if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)) {

                        try {
                            r.close();
                            if (status) {
                                Thread.currentThread().interrupt();
                                status = false;
                                System.out.println(" 线程正准备关闭..." + Thread.currentThread().getName());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }




                    /**
                     * 命令同步
                     */

                    /**
                     * 命令同步
                     */
//                    sendDefaultCommand.sendDefaultCommand(event,r,redisClient,threadPoolTaskExecutor,taskId);


                }
            }));
            r.open();
        }catch (URISyntaxException e) {
            try {
                Map<String,String> msg=SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),e.getMessage());
            } catch (TaskMsgException et) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,e.getMessage());
        } catch (EOFException ex ){
            try {
                Map<String,String> msg=SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),ex.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,ex.getMessage());
        }catch (NoRouteToHostException p){
            try {
                Map<String,String> msg=SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),p.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,p.getMessage());
        }catch (ConnectException cx){
            try {
                Map<String,String> msg=SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),cx.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,cx.getMessage());
        }
        catch (IOException et) {
            try {
                Map<String,String> msg= SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),et.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,et.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IncrementException et) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),et.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, et.getMessage());
        }catch (Exception e){
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),e.getMessage());
            } catch (TaskMsgException ep) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }

    }


}
