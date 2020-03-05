package syncer.syncerservice.sync;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import syncer.syncerjedis.exceptions.JedisConnectionException;
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
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.extend.replicator.listener.ValueDumpIterableEventListener;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerservice.compensator.ISyncerCompensator;
import syncer.syncerservice.compensator.ISyncerCompensatorFactory;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.filter.*;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisClientFactory;
import syncer.syncerservice.util.JDRedisClient.RedisMigrator;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.SyncTaskUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private int bigKeySize;

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
        this.bigKeySize=syncDataDto.getBigKeySize();
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


            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r, info.getRdbVersion(),bigKeySize));

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
            if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.INCREMENTONLY)) {

                String[] data = RedisUrlCheckUtils.selectSyncerBuffer(sourceUri, offsetPlace);
                System.out.println("offsetAllNum"+Long.parseLong(data[0]));
                System.out.println(JSON.toJSONString(data));

                long offsetNum = 0L;
                try {
                    offsetNum = Long.parseLong(data[0]);
                    offsetNum -= 1;
                    //offsetNum -= 1;

                } catch (Exception e) {

                }
                if (offsetNum != 0L && !StringUtils.isEmpty(data[1])) {
                    r.getConfiguration().setReplOffset(offsetNum);
                    r.getConfiguration().setReplId(data[1]);
                }
            }

            final OffSetEntity baseOffSet = TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().get(sourceUri);

//            MultiQueueFilter multiQueueFilter=new MultiQueueFilter(branchTypeEnum,syncDataDto,type,r,taskId,batchSize);



            List<CommonFilter> commonFilterList = new ArrayList<>();
            JDRedisClient client = JDRedisClientFactory.createJDRedisClient(branchTypeEnum, syncDataDto.getTargetHost(), syncDataDto.getTargetPort(), syncDataDto.getTargetPassword(), batchSize, taskId);

            //根据type生成相对节点List [List顺序即为filter节点执行顺序]
            assemble_the_list(commonFilterList, type, taskId, syncDataDto, client);
            ISyncerCompensator syncerCompensator= ISyncerCompensatorFactory.createJDRedisClient(branchTypeEnum,taskId,client);

            SendCommandWithOutQueue sendCommandWithOutQueue=SendCommandWithOutQueue.builder()
                    .filterChain(KeyValueRunFilterChain.builder().commonFilterList(commonFilterList).build())
                    .r(r)
                    .taskId(taskId)
                    .syncerCompensator(syncerCompensator)
                    .build();

            r.addEventListener(new ValueDumpIterableEventListener(batchSize, new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {
//                    System.out.println(JSON.toJSONString(event));
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
                            //.configuration(r.getConfiguration())
                            .taskRunTypeEnum(TaskRunTypeEnum.valueOf(type.trim().toUpperCase()))
                            .fileType(fileType)
                            .build();


                    //多队列接入
//                    try {
//                        multiQueueFilter.run(r,node);
//                    } catch (FilterNodeException e) {
//
//                    }

                    sendCommandWithOutQueue.run(node);

                }
            }));

            r.open(taskId);
        } catch (Exception e) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), e.getMessage());
            } catch (TaskMsgException ex) {
                log.warn("任务Id【{}】异常结束任务失败 ，失败原因【{}】", taskId, e.getMessage());
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }
    }



    /**
     * 按照Type组装List节点
     *
     * @param commonFilterList
     * @param type
     * @param taskId
     * @param syncDataDto
     * @param client
     */
    public void assemble_the_list(List<CommonFilter> commonFilterList, String type, String taskId, RedisSyncDataDto syncDataDto, JDRedisClient client) {
        //全量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.STOCKONLY)) {

            commonFilterList.add(KeyValueTimeCalculationFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueDataAnalysisFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueEventDBMappingFilter.builder().taskId(taskId).client(client).build());

//            commonFilterList.add(KeyValueSizeCalulationFilter.builder().taskId(taskId).client(client).build());

            commonFilterList.add(KeyValueRdbSyncEventFilter.builder().taskId(taskId).client(client).redisVersion(syncDataDto.getRedisVersion()).build());
        }

        //增量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.INCREMENTONLY)) {

            commonFilterList.add(KeyValueEventDBMappingFilter.builder().taskId(taskId).client(client).build());

//            commonFilterList.add(KeyValueSizeCalulationFilter.builder().taskId(taskId).client(client).build());

            commonFilterList.add(KeyValueCommandSyncEventFilter.builder().taskId(taskId).client(client).build());
        }


        //全量+增量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.TOTAL)) {
            commonFilterList.add(KeyValueTimeCalculationFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueDataAnalysisFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueEventDBMappingFilter.builder().taskId(taskId).client(client).build());

//            commonFilterList.add(KeyValueSizeCalulationFilter.builder().taskId(taskId).client(client).build());

            commonFilterList.add(KeyValueRdbSyncEventFilter.builder().taskId(taskId).client(client).redisVersion(syncDataDto.getRedisVersion()).build());
            commonFilterList.add(KeyValueCommandSyncEventFilter.builder().taskId(taskId).client(client).build());
        }


    }


}
