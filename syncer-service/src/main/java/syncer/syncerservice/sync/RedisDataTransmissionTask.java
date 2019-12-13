package syncer.syncerservice.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import redis.clients.jedis.exceptions.JedisConnectionException;
import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.RedisBranchTypeEnum;
import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.RedisInfo;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.entity.dto.RedisSyncDataDto;
import syncer.syncerplusredis.entity.thread.OffSetEntity;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.EventListener;
import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.extend.replicator.listener.ValueDumpIterableEventListener;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerservice.filter.*;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.HashUtils;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisClientFactory;
import syncer.syncerservice.util.JDRedisClient.RedisMigrator;
import syncer.syncerservice.util.KVUtils;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.SyncTaskUtils;
import syncer.syncerservice.util.jedis.ObjectUtils;
import syncer.syncerservice.util.queue.LocalMemoryQueue;
import syncer.syncerservice.util.queue.SyncerQueue;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RedisDataTransmissionTask implements Runnable {

    private String sourceUri;  //源redis地址
    private String fileAddress;  //源redis地址
    private String targetUri;  //目标redis地址
    private String threadName; //线程名称
    private RedisSyncDataDto syncDataDto;
    private RedisInfo info;
    private String taskId;
    private boolean afresh;
    private int batchSize;
    private String offsetPlace;
    private String type;
    private RedisBranchTypeEnum branchTypeEnum;
    private boolean status = true;
    private FileType fileType;


    public RedisDataTransmissionTask(RedisSyncDataDto syncDataDto, RedisInfo info, String taskId, int batchSize, boolean afresh, RedisBranchTypeEnum branchTypeEnum) {

        this.syncDataDto = syncDataDto;
        this.branchTypeEnum = branchTypeEnum;
        this.sourceUri = syncDataDto.getSourceUri();
        this.targetUri = syncDataDto.getTargetUri();
        this.threadName = syncDataDto.getTaskName();
        this.info = info;
        this.taskId = taskId;
        this.afresh = syncDataDto.isAfresh();
        this.batchSize = batchSize;
        this.afresh = afresh;
        this.offsetPlace = syncDataDto.getOffsetPlace();
        this.type = syncDataDto.getTasktype();
        this.fileType = syncDataDto.getFileType();
        this.fileAddress = syncDataDto.getFileAddress();
    }


    @Override
    public void run() {
        if (batchSize == 0) {
            batchSize = 1000;
        }

        try {
            Replicator replicator = null;
            if (fileType.equals(FileType.SYNC)) {
                RedisURI suri = new RedisURI(sourceUri);
                replicator = new JDRedisReplicator(suri, afresh);
            } else {
                replicator = new JDRedisReplicator(null, fileType, fileAddress, Configuration.defaultSetting(), taskId);
            }

            final Replicator r = RedisMigrator.newBacthedCommandDress(replicator);
            TaskMsgUtils.getThreadMsgEntity(taskId).addReplicator(r);
            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r, info.getRdbVersion()));
            OffSetEntity offset = TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().get(sourceUri);
            if (offset == null) {
                offset = new OffSetEntity();
                TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().put(sourceUri, offset);
            } else {
                if (StringUtils.isEmpty(offset.getReplId())) {
                    offset.setReplId(r.getConfiguration().getReplId());
                } else if (offset.getReplOffset().get() > -1) {
                    if (!afresh) {
                        r.getConfiguration().setReplOffset(offset.getReplOffset().get());
                        r.getConfiguration().setReplId(offset.getReplId());
                    }

                }
            }

            //只增量相关代码
            if (type.trim().toUpperCase().equals(TaskRunTypeEnum.INCREMENTONLY)) {
                String[] data = RedisUrlCheckUtils.selectSyncerBuffer(sourceUri, offsetPlace);
                long offsetNum = 0L;
                try {
                    offsetNum = Long.parseLong(data[0]);
                    offsetNum -= 1;
                } catch (Exception e) {

                }
                if (offsetNum != 0L && !StringUtils.isEmpty(data[1])) {
                    r.getConfiguration().setReplOffset(offsetNum);
                    r.getConfiguration().setReplId(data[1]);
                }
            }

            final OffSetEntity baseOffSet = TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().get(sourceUri);

            MultiQueueFilter multiQueueFilter=new MultiQueueFilter(branchTypeEnum,syncDataDto,type,r,taskId,batchSize);

            r.addEventListener(new ValueDumpIterableEventListener(batchSize, new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {

                    if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)) {
                        //判断任务是否关闭
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


                    KeyValueEventEntity node = KeyValueEventEntity.builder()
                            .event(event)
                            .dbMapper(syncDataDto.getDbMapper())
                            .redisVersion(syncDataDto.getRedisVersion())
                            .baseOffSet(baseOffSet)
                            .replId(r.getConfiguration().getReplId())
                            .replOffset(r.getConfiguration().getReplOffset())
//                            .configuration(r.getConfiguration())
                            .taskRunTypeEnum(TaskRunTypeEnum.valueOf(type.trim().toUpperCase()))
                            .build();


                    multiQueueFilter.run(r,node);

                }
            }));

            r.open(taskId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (EOFException ex) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), ex.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ex.getMessage());
        } catch (NoRouteToHostException p) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), p.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, p.getMessage());
        } catch (ConnectException cx) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), cx.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, cx.getMessage());
        } catch (AssertionError er) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), er.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, er.getMessage());
        } catch (JedisConnectionException ty) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), ty.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ty.getMessage());
        } catch (SocketException ii) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), ii.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ii.getMessage());
        } catch (IOException et) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), et.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, et.getMessage());
        } catch (IncrementException et) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), et.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, et.getMessage());
        }
    }





}
