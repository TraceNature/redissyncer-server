package com.i1314i.syncerplusservice.rdbtask.cluster;

import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusredis.event.Event;
import com.i1314i.syncerplusredis.event.EventListener;
import com.i1314i.syncerplusredis.event.PostRdbSyncEvent;
import com.i1314i.syncerplusredis.event.PreRdbSyncEvent;
import com.i1314i.syncerplusredis.rdb.datatype.DB;
import com.i1314i.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import com.i1314i.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import com.i1314i.syncerplusredis.replicator.Replicator;
import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.entity.RedisInfo;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.rdbtask.cluster.command.SendClusterRdbCommand;
import com.i1314i.syncerplusservice.rdbtask.cluster.command.SendClusterRdbCommand1;
import com.i1314i.syncerplusservice.rdbtask.enums.RedisCommandType;
import com.i1314i.syncerplusservice.replicator.listener.ValueDumpIterableEventListener;
import com.i1314i.syncerplusservice.replicator.service.JDRedisReplicator;
import com.i1314i.syncerplusservice.replicator.visitor.ValueDumpIterableRdbVisitor;
import com.i1314i.syncerplusservice.service.command.SendRDBClusterDefaultCommand;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.task.BatchedKeyValueTask.cluster.RdbClusterCommand;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusservice.util.TaskMsgUtils;

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
public class ClusterDataRestoreTask implements Runnable {
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
    SendClusterRdbCommand1 clusterRdbCommand=new SendClusterRdbCommand1();
    private RedisInfo info;
    private String taskId;
    private int  batchSize;
    public ClusterDataRestoreTask(RedisClusterDto syncDataDto, RedisInfo info,String sourceUri,String taskId,int batchSize) {
        this.syncDataDto = syncDataDto;
        this.sourceUri=sourceUri;
        this.threadName = syncDataDto.getTaskName();
        this.info=info;
        this.taskId=taskId;
        this.batchSize=batchSize;
    }

    public ClusterDataRestoreTask(RedisClusterDto syncDataDto, RedisInfo info,String sourceUri,String taskId) {
        this.syncDataDto = syncDataDto;
        this.sourceUri=sourceUri;
        this.threadName = syncDataDto.getTaskName();
        this.info=info;
        this.taskId=taskId;
        this.batchSize=syncDataDto.getBatchSize();
    }

    @Override
    public void run() {

        //设线程名称
        Thread.currentThread().setName(threadName);

        if(batchSize==0){
            batchSize=1000;
        }

        try {
            RedisURI suri = new RedisURI(sourceUri);
            SyncJedisClusterClient poolss=RedisUrlUtils.getConnectionClusterPool(syncDataDto);
            redisClient=poolss.jedisCluster();
            final Replicator r  = RedisMigrator.newBacthedCommandDress(new JDRedisReplicator(suri));
            TaskMsgUtils.getThreadMsgEntity(taskId).addReplicator(r);

            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r,info.getRdbVersion()));
            r.addEventListener(new ValueDumpIterableEventListener(batchSize, new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {


                    if (TaskMsgUtils.doThreadisCloseCheckTask(taskId)){

                        try {
                          if(redisClient!=null){
                              redisClient.close();
                          }
                            r.close();



                            if(status){
                               Thread.currentThread().interrupt();
                                status= false;
                                TaskMsgUtils.stopCreateThread(Arrays.asList(taskId));
                                System.out.println(" 线程正准备关闭...." + Thread.currentThread().getName());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (TaskMsgException e) {
                            e.printStackTrace();
                        }
                        return;
                    }


                    if (event instanceof PreRdbSyncEvent) {
                        log.info("{} :全量同步启动 ");
                    }


                    if (event instanceof PostRdbSyncEvent) {
                        log.info("{} :全量同步结束 ");
                    }

                    if (event instanceof BatchedKeyValuePair<?, ?>) {



                        BatchedKeyValuePair event1 = (BatchedKeyValuePair) event;
                        if (event1.getDb() == null)
                            return;

                        DB db=event1.getDb();
                        if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                            if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                            }else {
                                return;
                            }
                        }

                        Long ms;
                        if(event1.getExpiredMs()==null){
                            ms =0L;
                        }else {
                            ms =event1.getExpiredMs()-System.currentTimeMillis();
                            if(ms<0L){
                                return;
                            }
                        }
                        if (event1.getValue() != null) {
                            try {
//                                clusterRdbCommand.sendCommand(ms, RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,redisClient,new String((byte[]) event1.getKey()));
                                threadPoolTaskExecutor.submit(new SendClusterRdbCommand(ms, RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,redisClient,new String((byte[]) event1.getKey())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    if (event instanceof DumpKeyValuePair) {

                        /**
                         * 若存在Map映射则只同步映射关系中的数据
                         */
                        DumpKeyValuePair valuePair = (DumpKeyValuePair) event;
                        if (valuePair.getDb() == null)
                            return;

                        DB db=valuePair.getDb();
                        if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                            if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                            }else {
                                return;
                            }
                        }

                        if(valuePair.getValue()!=null){
                            Long ms;
                            if(valuePair.getExpiredMs()==null){
                                ms =0L;
                            }else {
                                ms =valuePair.getExpiredMs()-System.currentTimeMillis();
                                if(ms<0L){
                                    return;
                                }
                            }


                            try {
//                                clusterRdbCommand.sendCommand(ms, RedisCommandTypeEnum.DUMP,event,redisClient,new String((byte[]) valuePair.getKey()));
                                threadPoolTaskExecutor.submit(new SendClusterRdbCommand(ms, RedisCommandTypeEnum.DUMP,event,redisClient,new String((byte[]) valuePair.getKey())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }


                    /**
                     * 命令同步
                     */

                    /**
                     * 命令同步
                     */
                    sendDefaultCommand.sendDefaultCommand(event,r,redisClient,threadPoolTaskExecutor,taskId);


                }
            }));
            r.open();
        }catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (EOFException ex ){
            try {
                Map<String,String> msg=TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,ex.getMessage());
        }catch (NoRouteToHostException p){
            try {
                Map<String,String> msg=TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,p.getMessage());
        }catch (ConnectException cx){
            try {
                Map<String,String> msg=TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,cx.getMessage());
        }
        catch (IOException et) {
            try {
                Map<String,String> msg=TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】",taskId,et.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}
