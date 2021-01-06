package syncer.transmission.task;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.replica.cmd.impl.DefaultCommand;
import syncer.replica.constant.RedisBranchTypeEnum;
import syncer.replica.entity.*;
import syncer.replica.event.Event;
import syncer.replica.event.PostRdbSyncEvent;
import syncer.replica.event.PreCommandSyncEvent;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.listener.EventListener;
import syncer.replica.listener.TaskStatusListener;
import syncer.replica.listener.ValueDumpIterableEventListener;
import syncer.replica.rdb.sync.visitor.ValueDumpIterableRdbVisitor;
import syncer.replica.register.DefaultCommandRegister;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.replica.util.SyncTypeUtils;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.RedisClientFactory;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.compensator.ISyncerCompensatorFactory;
import syncer.transmission.entity.OffSetEntity;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.queue.SendCommandWithOutQueue;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.strategy.commandprocessing.ProcessingRunStrategyChain;
import syncer.transmission.strategy.commandprocessing.ProcessingRunStrategyListSelecter;
import syncer.transmission.util.redis.KeyCountUtils;
import syncer.transmission.util.redis.RedisReplIdCheck;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
@Slf4j
@AllArgsConstructor
public class RedisDataSyncTransmissionTask implements Runnable{
    private TaskModel taskModel;
    /**
     * 目标Redis类型
     */

    private boolean status = true;

    public RedisDataSyncTransmissionTask(TaskModel taskModel, boolean status) {
        this.taskModel = taskModel;
        this.status = status;
    }

    RedisReplIdCheck redisReplIdCheck=new RedisReplIdCheck();
    @Override
    public void run() {
//        Thread.currentThread().setName(taskModel.getId()+": "+Thread.currentThread().getName());

        if(Objects.isNull(taskModel.getBatchSize())||taskModel.getBatchSize()==0){
            taskModel.setBatchSize(500);
        }

        try{
            Replication replication=null;
            //replication
            if (taskModel.getSyncType().equals(SyncType.SYNC.getCode())) {

                RedisURI suri = new RedisURI(taskModel.getSourceUri());
                KeyCountUtils.updateKeyCount(taskModel.getId(),suri);
                replication = new RedisReplication(suri, taskModel.isAfresh());
            } else {
                //文件
                replication = new RedisReplication(taskModel.getFileAddress(), SyncTypeUtils.getSyncType(taskModel.getSyncType()).getFileType(),  Configuration.defaultSetting().setTaskId(taskModel.getTaskId()));
            }


            //注册增量命令解析器
            final Replication replicationHandler = DefaultCommandRegister.addCommandParser(replication);
            replicationHandler.getConfiguration().setTaskId(taskModel.getTaskId());
            //注册RDB全量解析器
            replicationHandler.setRdbVisitor(new ValueDumpIterableRdbVisitor(replicationHandler, taskModel.getRdbVersion()));

            OffSetEntity offset = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId()).getOffSetEntity();
            if (offset == null) {
                offset = new OffSetEntity();
                SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId()).setOffSetEntity(offset);
            } else {
                if (StringUtils.isEmpty(offset.getReplId())) {
                    offset.setReplId(replicationHandler.getConfiguration().getReplId());
                } else if (offset.getReplOffset().get() > -1) {
                    if (!taskModel.isAfresh()) {
                        replicationHandler.getConfiguration().setReplOffset(offset.getReplOffset().get());
                        replicationHandler.getConfiguration().setReplId(offset.getReplId());
                    }
                }
            }
            SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId()).setReplication(replicationHandler);

            //只增量相关代码  增量命令实时备份
            if (SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType().equals(TaskRunTypeEnum.INCREMENTONLY)) {
                String[] data = redisReplIdCheck.selectSyncerBuffer(taskModel.getSourceUri(), SyncTypeUtils.getOffsetPlace(taskModel.getOffsetPlace()).getOffsetPlace());
                long offsetNum = 0L;
                try {
                    offsetNum = Long.parseLong(data[0]);
                    offsetNum -= 1;
                    //offsetNum -= 1;
                } catch (Exception e) {
                }
                if (offsetNum != 0L && !StringUtils.isEmpty(data[1])) {
                    replicationHandler.getConfiguration().setReplOffset(offsetNum);
                    replicationHandler.getConfiguration().setReplId(data[1]);
                }
            }

            if(taskModel.getTargetUri().size()>1){
                taskModel.setTargetRedisType(2);
            }

            RedisBranchTypeEnum branchType=SyncTypeUtils.getRedisBranchType(taskModel.getTargetRedisType()).getBranchTypeEnum();
            RedisClient client = RedisClientFactory.createRedisClient(branchType, taskModel.getTargetHost(), taskModel.getTargetPort(), taskModel.getTargetPassword(), taskModel.getBatchSize(),taskModel.getErrorCount(), taskModel.getId(),null,null);
            //根据type生成相对节点List [List顺序即为filter节点执行顺序]
            List<CommonProcessingStrategy> commonFilterList = ProcessingRunStrategyListSelecter.getStrategyList(SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType(),taskModel,client);
            ISyncerCompensator syncerCompensator= ISyncerCompensatorFactory.createRedisClient(branchType,taskModel.getId(),client);
            SendCommandWithOutQueue sendCommandWithOutQueue=SendCommandWithOutQueue.builder()
                    .filterChain(ProcessingRunStrategyChain.builder().commonFilterList(commonFilterList).build())
                    .replication(replicationHandler)
                    .taskId(taskModel.getTaskId())
                    .syncerCompensator(syncerCompensator)
                    .build();

            final  OffSetEntity baseoffset=offset;

            replicationHandler.addEventListener(new ValueDumpIterableEventListener(taskModel.getBatchSize(), new EventListener() {
                @Override
                public void onEvent(Replication replicator, Event event) {

                    if (SingleTaskDataManagerUtils.isTaskClose(taskModel.getId())) {
                        //判断任务是否关闭
                        try {
                            replicationHandler.close();
                            if (status) {
                                Thread.currentThread().interrupt();
                                status = false;
                                log.info("[{}] 线程正准备关闭..." ,Thread.currentThread().getName());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }



                    KeyValueEventEntity node = KeyValueEventEntity.builder()
                            .event(event)
                            .dbMapper(taskModel.getDbMapping())
                            .redisVersion(taskModel.getRedisVersion())
                            .baseOffSet(baseoffset)
                            .replId(replicationHandler.getConfiguration().getReplId())
                            .replOffset(replicationHandler.getConfiguration().getReplOffset())
                            .taskRunTypeEnum(SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType())
                            .fileType(SyncTypeUtils.getSyncType(taskModel.getSyncType()).getFileType())
                            .build();
                    //更新offset
                    updateOffset(taskModel.getId(),replicationHandler,node);

                    sendCommandWithOutQueue.run(node);


                }
            }));

            /**
             * 任务状态
             */
            replicationHandler.addTaskStatusListener(new TaskStatusListener() {
                @Override
                public void handle(Replication replication, SyncerTaskEvent event) {
                    String taskId=event.getEvent().getTaskId();
                    try {
                        SingleTaskDataManagerUtils.changeThreadStatus(taskId,event.getOffset(),event.getTaskStatusType());
                        if(Objects.nonNull(event.getMsg())&&event.getTaskStatusType().equals(TaskStatusType.BROKEN)){
                            SingleTaskDataManagerUtils.updateThreadMsg(taskId,event.getMsg());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            //任务运行
            SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(),taskModel.getOffset(), TaskStatusType.RUN);


            replicationHandler.open();

        }catch (Exception e){
            SingleTaskDataManagerUtils.brokenStatusAndLog(e,this.getClass(),taskModel.getId());
            e.printStackTrace();
        }

    }


    /**
     * 计算offset
     * @param taskId
     * @param replicationHandler
     * @param node
     */
    private void updateOffset(String taskId, Replication replicationHandler, KeyValueEventEntity node){
        try {
            TaskDataEntity data=SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId);
            if(data.getOffSetEntity()==null){
                data.setOffSetEntity(OffSetEntity.builder()
                        .replId(replicationHandler.getConfiguration().getReplId())
                        .build());
            }
            Event event=node.getEvent();
            //全量同步结束
            if (event instanceof PostRdbSyncEvent ||event instanceof DefaultCommand ||event instanceof PreCommandSyncEvent) {
                data.getOffSetEntity().setReplId(replicationHandler.getConfiguration().getReplId());
                data.getOffSetEntity().getReplOffset().set(replicationHandler.getConfiguration().getReplOffset());

                if(node.getTaskRunTypeEnum().equals(TaskRunTypeEnum.STOCKONLY)||event instanceof PreCommandSyncEvent){
                    SqlOPUtils.updateOffsetAndReplId(taskId,replicationHandler.getConfiguration().getReplOffset(),replicationHandler.getConfiguration().getReplId());
                }
            }
        }catch (Exception e){
            log.info("[{}]update offset fail,replid[{}],offset[{}]",taskId,replicationHandler.getConfiguration().getReplId(),replicationHandler.getConfiguration().getReplOffset());
        }

    }
}
