package com.i1314i.syncerplusservice.rdbtask.single.pipeline;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.config.ThreadPoolConfig;
import com.i1314i.syncerpluscommon.util.spring.SpringUtil;
import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.entity.EventEntity;
import com.i1314i.syncerplusservice.entity.RedisInfo;
import com.i1314i.syncerplusservice.entity.SyncTaskEntity;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.entity.thread.EventTypeEntity;
import com.i1314i.syncerplusservice.entity.thread.OffSetEntity;
import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.rdbtask.enums.RedisCommandType;
import com.i1314i.syncerplusservice.replicator.listener.ValueDumpIterableEventListener;
import com.i1314i.syncerplusservice.replicator.service.JDRedisReplicator;
import com.i1314i.syncerplusservice.replicator.visitor.ValueDumpIterableRdbVisitor;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.task.clusterTask.command.ClusterProtocolCommand;
import com.i1314i.syncerplusservice.task.singleTask.pipe.LockPipe;
import com.i1314i.syncerplusservice.task.singleTask.pipe.PipelinedSyncTask;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusservice.util.TaskMsgUtils;
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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class SingleDataPipelineRestoreTask implements Runnable {

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
    private SendPipelineRdbCommand sendDefaultCommand = new SendPipelineRdbCommand();
    private RedisInfo info;
    private String taskId;
    private boolean afresh;
    private boolean syncStatus = true;
    Pipeline pipelined = null;

    private Lock lock = new ReentrantLock();
    @Getter
    @Setter
    private String dbindex="-1";
    private final AtomicLong dbNum = new AtomicLong(0);
    private LockPipe lockPipe = new LockPipe();
    private SyncTaskEntity taskEntity = new SyncTaskEntity();

    public SingleDataPipelineRestoreTask(RedisSyncDataDto syncDataDto, RedisInfo info, String taskId) {
        this.syncDataDto = syncDataDto;
        this.sourceUri = syncDataDto.getSourceUri();
        this.targetUri = syncDataDto.getTargetUri();
        this.threadName = syncDataDto.getTaskName();
        this.info = info;
        this.taskId = taskId;
        this.afresh=syncDataDto.isAfresh();
    }


    @Override
    public void run() {

        //设线程名称
        Thread.currentThread().setName(threadName);

        try {
            RedisURI suri = new RedisURI(sourceUri);
            RedisURI turi = new RedisURI(targetUri);
            JDJedisClientPool targetJedisClientPool = RedisUrlUtils.getJDJedisClient(syncDataDto, turi);


            final JDJedis targetJedisplus = targetJedisClientPool.getResource();
            if (pipelined == null) {
                pipelined = targetJedisplus.pipelined();
            }
            PipelineLock pipelineLock=new PipelineLock(pipelined,taskEntity,taskId,targetJedisplus,targetJedisClientPool);


            final Replicator r = RedisMigrator.newBacthedCommandDress(new JDRedisReplicator(suri));

            TaskMsgUtils.getThreadMsgEntity(taskId).addReplicator(r);


            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r, info.getRdbVersion()));
//            1036363

            OffSetEntity offset= TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().get(sourceUri);
            if(offset==null){

                offset=new OffSetEntity();
                TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().put(sourceUri,offset);
            }else {

                if(StringUtils.isEmpty(offset.getReplId())){
                    offset.setReplId(r.getConfiguration().getReplId());
                }else if(offset.getReplOffset().get()>-1){
                    if(!afresh){
                        r.getConfiguration().setReplOffset(offset.getReplOffset().get());
                        r.getConfiguration().setReplId(offset.getReplId());
                    }

                }
            }

            final OffSetEntity baseOffSet= TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().get(sourceUri);
//            TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().put(taskId,offset);
//            r.getConfiguration().setReplOffset(14105376832);
//            r.getConfiguration().setReplId("e1399afce9f5b5c35c5315ae68e4807fe81e764f");
            r.addEventListener(new ValueDumpIterableEventListener(1000, new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {

//                    r.getConfiguration().getReplOffset()
//                    lockPipe.syncpipe(pipelined, taskEntity, 1000, true);
                    lockPipe.syncpipe(pipelineLock, taskEntity, 1000, true,suri,turi);
                    if (TaskMsgUtils.doThreadisCloseCheckTask(taskId)) {

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
                     * 管道的形式
                     */
                    if (syncStatus) {
//                        threadPoolTaskExecutor.submit(new PipelinedSyncTask(pipelined, taskEntity,lockPipe));
                        threadPoolTaskExecutor.submit(new PipelinedSyncTask(pipelineLock, taskEntity,lockPipe,taskId,suri,turi));
                        syncStatus = false;
                    }

                    if (event instanceof PreRdbSyncEvent) {
                        log.info("{} :全量同步启动");
                    }


                    if (event instanceof PostRdbSyncEvent) {

                        if(r.getConfiguration().getReplOffset()>=0){
                            baseOffSet.setReplId(r.getConfiguration().getReplId());
                            baseOffSet.getReplOffset().set(r.getConfiguration().getReplOffset());
                        }

//                        TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().put(taskId,baseOffSet);
                        log.info("{} :全量同步结束 ");
                    }

                    if (event instanceof BatchedKeyValuePair<?, ?>) {

                        BatchedKeyValuePair event1 = (BatchedKeyValuePair) event;


                        DB db = event1.getDb();
                        Long ms;
                        if (event1.getExpiredMs() == null) {
                            ms = 0L;
                        } else {
                            ms = event1.getExpiredMs() - System.currentTimeMillis();
                        }
                        if (event1.getValue() != null) {


                            int dbbnum = (int) db.getDbNumber();
                            if (null != syncDataDto.getDbNum() && syncDataDto.getDbNum().size() > 0) {
                                if (syncDataDto.getDbNum().containsKey((int) db.getDbNumber())) {

                                    dbbnum = syncDataDto.getDbNum().get((int) db.getDbNumber());
                                } else {
                                    return;
                                }
                            }

                            if (lockPipe.getDbNum() != dbbnum) {
                                pipelined.select(dbbnum);

                                EventEntity eventEntity=new EventEntity("SELECT".getBytes(),ms,null, EventTypeEntity.USE,RedisCommandTypeEnum.STRING);
                                taskEntity.addKey(eventEntity);

                                lockPipe.setDbNum(dbbnum);
                                taskEntity.add();

                            }


                            try {
                                sendDefaultCommand.sendSingleCommand(ms,RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,pipelineLock,new String((byte[]) event1.getKey()),syncDataDto.getRedisVersion(),taskEntity);

//                                sendDefaultCommand.sendSingleCommand(ms,RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,pipelined,new String((byte[]) event1.getKey()),syncDataDto.getRedisVersion(),taskEntity);
//                                threadPoolTaskExecutor.submit(new SendRdbCommand(ms, RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()), event, RedisCommandType.getJDJedis(targetJedisClientPool, event, syncDataDto.getDbNum()), new String((byte[]) event1.getKey()), syncDataDto.getRedisVersion()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    }
                    if (event instanceof DumpKeyValuePair) {
//                        System.out.println(r.getConfiguration().getReplId()+":"+r.getConfiguration().getReplOffset());

                        DumpKeyValuePair valuePair = (DumpKeyValuePair) event;
                        if (valuePair.getValue() != null) {
                            Long ms;
                            if (valuePair.getExpiredMs() == null) {
                                ms = 0L;
                            } else {
                                ms = valuePair.getExpiredMs() - System.currentTimeMillis();
                            }

                            DB db = valuePair.getDb();

                            int dbbnum = (int) db.getDbNumber();
                            if (null != syncDataDto.getDbNum() && syncDataDto.getDbNum().size() > 0) {
                                if (syncDataDto.getDbNum().containsKey((int) db.getDbNumber())) {

                                    dbbnum = syncDataDto.getDbNum().get((int) db.getDbNumber());
                                } else {
                                    return;
                                }
                            }

                            try {

                                if (lockPipe.getDbNum() != dbbnum) {
                                    pipelined.select(dbbnum);
                                    lockPipe.setDbNum(dbbnum);
                                    EventEntity eventEntity=new EventEntity("SELECT".getBytes(),ms,null, EventTypeEntity.USE,RedisCommandTypeEnum.STRING);
                                    taskEntity.addKey(eventEntity);
                                    taskEntity.add();
                                }

                                sendDefaultCommand.sendSingleCommand(ms,RedisCommandTypeEnum.DUMP,event,pipelineLock,new String((byte[]) valuePair.getKey()),syncDataDto.getRedisVersion(),taskEntity);


//                                threadPoolTaskExecutor.submit(new SendRdbCommand(ms, RedisCommandTypeEnum.DUMP, event, RedisCommandType.getJDJedis(targetJedisClientPool, event, syncDataDto.getDbNum()), new String((byte[]) valuePair.getKey()), syncDataDto.getRedisVersion()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }


                    /**
                     * 命令同步
                     */

                    if (event instanceof DefaultCommand) {
                        System.out.println(r.getConfiguration().getReplId()+":"+r.getConfiguration().getReplOffset());
                        baseOffSet.setReplId(r.getConfiguration().getReplId());
                        baseOffSet.getReplOffset().set(r.getConfiguration().getReplOffset());
//                        TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().put(taskId,baseOffSet);
                        DefaultCommand dc = (DefaultCommand) event;
                        if(dc.getCommand().equals("SELECT".getBytes())){
                            System.out.println(new String(dc.getArgs()[0]));
                        }
                        EventEntity eventEntity=new EventEntity(new DB(dbNum.get()), EventTypeEntity.USE,RedisCommandTypeEnum.COMMAND,dc);
                        taskEntity.addKey(eventEntity);

                        pipelineLock.sendCommand(new ClusterProtocolCommand(dc.getCommand()), dc.getArgs());
//                        pipelined.sendCommand(new ClusterProtocolCommand(dc.getCommand()), dc.getArgs());
                        taskEntity.add();
                    }
//                    sendDefaultCommand.sendDefaultCommand(event, r, pool, threadPoolTaskExecutor, syncDataDto);


                }
            }));
            r.open();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (EOFException ex) {
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ex.getMessage());
        } catch (NoRouteToHostException p) {
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, p.getMessage());
        } catch (ConnectException cx) {
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, cx.getMessage());
        }catch (AssertionError er){
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, er.getMessage());
        }catch (JedisConnectionException ty){
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ty.getMessage());
        }catch (SocketException ii){
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ii.getMessage());
        }

        catch (IOException et) {
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, et.getMessage());
        }
    }


}
