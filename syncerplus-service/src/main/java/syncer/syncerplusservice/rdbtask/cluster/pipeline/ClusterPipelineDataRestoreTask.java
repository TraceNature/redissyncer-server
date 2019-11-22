package syncer.syncerplusservice.rdbtask.cluster.pipeline;

import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.EventListener;
import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.extend.replicator.listener.ValueDumpIterableEventListener;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.entity.RedisInfo;
import syncer.syncerplusredis.entity.SyncTaskEntity;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusservice.pool.RedisMigrator;
import syncer.syncerplusservice.rdbtask.cluster.command.SendClusterRdbCommand1;

import syncer.syncerplusservice.service.command.SendRDBClusterDefaultCommand;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusservice.task.BatchedKeyValueTask.cluster.RdbClusterCommand;
import syncer.syncerplusservice.task.singleTask.pipe.cluster.LockPipeCluster;
import syncer.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import syncer.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import syncer.syncerplusservice.util.Jedis.cluster.pipelineCluster.JedisClusterPipeline;
import syncer.syncerplusservice.util.RedisUrlUtils;
import syncer.syncerplusredis.util.TaskMsgUtils;

import syncer.syncerplusservice.util.SyncTaskUtils;
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
    private JedisClusterPlus redisClient;
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
            SyncJedisClusterClient poolss= RedisUrlUtils.getConnectionClusterPool(syncDataDto);

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
