package com.i1314i.syncerplusservice.filetask.cluster;

import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusredis.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.entity.FileType;
import com.i1314i.syncerplusredis.entity.RedisInfo;
import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusredis.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusredis.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusredis.entity.thread.OffSetEntity;
import com.i1314i.syncerplusredis.event.Event;
import com.i1314i.syncerplusredis.event.EventListener;
import com.i1314i.syncerplusredis.event.PostRdbSyncEvent;
import com.i1314i.syncerplusredis.event.PreRdbSyncEvent;
import com.i1314i.syncerplusredis.exception.IncrementException;
import com.i1314i.syncerplusredis.exception.TaskMsgException;
import com.i1314i.syncerplusredis.extend.replicator.listener.ValueDumpIterableEventListener;
import com.i1314i.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import com.i1314i.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import com.i1314i.syncerplusredis.rdb.datatype.DB;
import com.i1314i.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import com.i1314i.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import com.i1314i.syncerplusredis.replicator.Replicator;
import com.i1314i.syncerplusredis.util.TaskMsgUtils;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.rdbtask.cluster.command.SendClusterRdbCommand;
import com.i1314i.syncerplusservice.rdbtask.cluster.command.SendClusterRdbCommand1;
import com.i1314i.syncerplusservice.rdbtask.enums.RedisCommandType;
import com.i1314i.syncerplusservice.service.command.SendRDBClusterDefaultCommand;
import com.i1314i.syncerplusservice.task.BatchedKeyValueTask.cluster.RdbClusterCommand;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusservice.util.SyncTaskUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RdbFileClusterDataRestoreTask implements Runnable {
    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;

    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }
    private boolean afresh;
    private String sourceUri;  //源redis地址
    private String targetUri;  //目标redis地址
    private int threadCount = 30;  //写线程数
    private boolean status = true;
    private String threadName; //线程名称
    private Date time;
    private RedisClusterDto syncDataDto;
    private SendRDBClusterDefaultCommand sendDefaultCommand=new SendRDBClusterDefaultCommand();

    //判断增量是否可写
    private final AtomicBoolean commandDbStatus=new AtomicBoolean(true);

    private RdbClusterCommand sendDumpKeyDiffVersionCommand=new RdbClusterCommand();
    private  JedisClusterPlus redisClient;
    private double redisVersion;
    SendClusterRdbCommand1 clusterRdbCommand=new SendClusterRdbCommand1();
    private RedisInfo info;
    private String taskId;
    private int  batchSize;

    private FileType fileType;
    public RdbFileClusterDataRestoreTask(RedisClusterDto syncDataDto, RedisInfo info, String sourceUri, String taskId, int batchSize) {
        this.syncDataDto = syncDataDto;
        this.sourceUri=sourceUri;
        this.threadName = syncDataDto.getTaskName();
        this.info=info;
        this.taskId=taskId;
        this.afresh=syncDataDto.isAfresh();
        this.batchSize=batchSize;
    }

    public RdbFileClusterDataRestoreTask(RedisClusterDto syncDataDto, RedisInfo info, String sourceUri, String taskId, boolean afresh) {
        this.syncDataDto = syncDataDto;
        this.sourceUri=sourceUri;
        this.threadName = syncDataDto.getTaskName();
        this.info=info;
        this.taskId=taskId;
        this.afresh=syncDataDto.isAfresh();
        this.batchSize=syncDataDto.getBatchSize();
        this.afresh=afresh;
        this.fileType=syncDataDto.getFileType();
    }


    public RdbFileClusterDataRestoreTask(RedisClusterDto syncDataDto, RedisInfo info, String taskId, int batchSize) {
        this.syncDataDto = syncDataDto;
        this.threadName = syncDataDto.getTaskName();
        this.info = info;
        this.taskId = taskId;
        this.batchSize=batchSize;
        this.fileType=syncDataDto.getFileType();
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
            Replicator r = new JDRedisReplicator(null, fileType,syncDataDto.getFileAddress(), Configuration.defaultSetting(),taskId);

            TaskMsgUtils.getThreadMsgEntity(taskId).addReplicator(r);



            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r,info.getRdbVersion()));
            r.addEventListener(new ValueDumpIterableEventListener(batchSize, new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {


                    if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)){

                        try {
                          if(redisClient!=null){
                              redisClient.close();
                          }
                            r.close();



                            if(status){
                               Thread.currentThread().interrupt();
                                status= false;
                                SyncTaskUtils.stopCreateThread(Arrays.asList(taskId));
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
                        time=new Date();
                        log.warn("【{}】 :全量同步启动",taskId);
                    }


                    if (event instanceof PostRdbSyncEvent) {

                            try {
                                Map<String, String> msg = SyncTaskUtils.stopCreateThread(Arrays.asList(taskId));
                                if(redisClient!=null){
                                    redisClient.close();
                                }
                                r.close();
                            } catch (TaskMsgException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        long set=new Date().getTime()-time.getTime();
                        if(set/1000==0){
                            SyncTaskUtils.editTaskMsg(taskId,"全量同步结束 时间(ms)："+set);
                            log.warn("【{}】 :全量同步结束 时间：{}(ms)",taskId,set);
                        }else {
                            set=set/1000;
                            SyncTaskUtils.editTaskMsg(taskId,"全量同步结束 时间(s)："+set);
                            log.warn("【{}】 :全量同步结束 时间：{}(s)",taskId,set);
                        }
                            return;
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
                            if(ms<=0L){
                                return;
                            }
                        }
                        if (event1.getValue() != null&&event1.getKey()!=null) {
                            try {

//                                new SendClusterRdbCommand(ms, RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,redisClient,new String((byte[]) event1.getKey())).run();
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
                            if(valuePair.getExpiredMs()==null||valuePair.getExpiredMs()==0L){
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

                }
            }));
            r.open(taskId);
        }catch (URISyntaxException e) {
            e.printStackTrace();
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
                Map<String,String> msg=SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),et.getMessage());
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
