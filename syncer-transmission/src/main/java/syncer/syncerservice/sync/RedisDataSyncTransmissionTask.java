package syncer.syncerservice.sync;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.RedisBranchTypeEnum;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.entity.thread.OffSetEntity;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.EventListener;
import syncer.syncerplusredis.event.PostRdbSyncEvent;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.extend.replicator.listener.ValueDumpIterableEventListener;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.SyncTypeUtils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TaskErrorUtils;
import syncer.syncerservice.compensator.ISyncerCompensator;
import syncer.syncerservice.compensator.ISyncerCompensatorFactory;
import syncer.syncerservice.filter.CommonFilter;
import syncer.syncerservice.filter.KeyValueRunFilterChain;
import syncer.syncerservice.filter.filter_factory.KeyValueFilterListSelector;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisClientFactory;
import syncer.syncerservice.util.JDRedisClient.RedisMigrator;
import syncer.syncerservice.util.KeyCountUtils;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.SyncTaskUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/16
 */
@Slf4j
@AllArgsConstructor
public class RedisDataSyncTransmissionTask implements Runnable{
    private TaskModel taskModel;
    /**
     * 目标Redis类型
  */

    private boolean status = true;
    @Override
    public void run() {

        //这一步应交给上层去做
        if(null==taskModel.getBatchSize()||taskModel.getBatchSize()==0){
            taskModel.setBatchSize(1500);
        }

        try {
            Replicator replicator = null;
            //replication
            if (taskModel.getSyncType().equals(SyncType.SYNC.getCode())) {

                RedisURI suri = new RedisURI(taskModel.getSourceUri());
                KeyCountUtils.updateKeyCount(taskModel.getId(),suri);
                replicator = new JDRedisReplicator(suri, taskModel.isAfresh());
            } else {
                //文件
                replicator = new JDRedisReplicator(null, SyncTypeUtils.getSyncType(taskModel.getSyncType()).getFileType(), taskModel.getFileAddress(), Configuration.defaultSetting(), taskModel.getId());
            }


            //注册增量命令解析器
            final Replicator replicationHandler = RedisMigrator.newBacthedCommandDress(replicator);
            //注册RDB全量解析器
            replicationHandler.setRdbVisitor(new ValueDumpIterableRdbVisitor(replicationHandler, taskModel.getRdbVersion()));

            OffSetEntity offset = TaskDataManagerUtils.get(taskModel.getId()).getOffSetEntity();
            if (offset == null) {
                offset = new OffSetEntity();
                TaskDataManagerUtils.get(taskModel.getId()).setOffSetEntity(offset);
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

            TaskDataManagerUtils.get(taskModel.getId()).setReplicator(replicationHandler);



            //只增量相关代码
            if (SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType().equals(TaskRunTypeEnum.INCREMENTONLY)) {
                String[] data = RedisUrlCheckUtils.selectSyncerBuffer(taskModel.getSourceUri(), SyncTypeUtils.getOffsetPlace(taskModel.getOffsetPlace()).getOffsetPlace());
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

            RedisBranchTypeEnum branchTypeEnum=SyncTypeUtils.getRedisBranchType(taskModel.getSourceRedisType()).getBranchTypeEnum();
            JDRedisClient client = JDRedisClientFactory.createJDRedisClient(branchTypeEnum, taskModel.getTargetHost(), taskModel.getTargetPort(), taskModel.getTargetPassword(), taskModel.getBatchSize(), taskModel.getId());
            //根据type生成相对节点List [List顺序即为filter节点执行顺序]
            List<CommonFilter> commonFilterList = KeyValueFilterListSelector.getStrategyList(SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType(),taskModel,client);
            ISyncerCompensator syncerCompensator= ISyncerCompensatorFactory.createJDRedisClient(branchTypeEnum,taskModel.getId(),client);
            SendCommandWithOutQueue sendCommandWithOutQueue=SendCommandWithOutQueue.builder()
                    .filterChain(KeyValueRunFilterChain.builder().commonFilterList(commonFilterList).build())
                    .r(replicationHandler)
                    .taskId(taskModel.getId())
                    .syncerCompensator(syncerCompensator)
                    .build();


            final  OffSetEntity baseoffset=offset;

            replicationHandler.addEventListener(new ValueDumpIterableEventListener(taskModel.getBatchSize(), new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {

                    if (TaskDataManagerUtils.isTaskClose(taskModel.getId())) {
                        //判断任务是否关闭
                        try {
                            replicationHandler.close();
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
                            .dbMapper(taskModel.getDbMapping())
                            .redisVersion(taskModel.getRedisVersion())
                            .baseOffSet(baseoffset)
                            .replId(replicationHandler.getConfiguration().getReplId())
                            .replOffset(replicationHandler.getConfiguration().getReplOffset())
                            //.configuration(r.getConfiguration())
                            .taskRunTypeEnum(SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType())
                            .fileType(SyncTypeUtils.getSyncType(taskModel.getSyncType()).getFileType())
                            .build();


//                    多队列接入
//                    try {
//                        multiQueueFilter.run(r,node);
//                    } catch (FilterNodeException e) {
//                        log.info("FilterNodeException:[{}]",e.getMessage());
//                    }

                    //更新offset
                    updateOffset(taskModel.getId(),replicationHandler,node);

                    sendCommandWithOutQueue.run(node);


                }
            }));




            replicationHandler.open(taskModel.getId());

            //任务运行
            TaskDataManagerUtils.changeThreadStatus(taskModel.getId(),taskModel.getOffset(), TaskStatusType.RUN);

//        }catch (IOException e){
//            //failover
//
        }catch (Exception e){
            TaskErrorUtils.brokenStatusAndLog(e,this.getClass(),taskModel.getId());
            e.printStackTrace();
        }
    }


    /**
     * 计算offset
     * @param taskId
     * @param replicationHandler
     * @param node
     */
    private void updateOffset(String taskId, Replicator replicationHandler,KeyValueEventEntity node){
        try {
            TaskDataEntity data=TaskDataManagerUtils.get(taskId);
            if(data.getOffSetEntity()==null){
                data.setOffSetEntity(OffSetEntity.builder()
                        .replId(replicationHandler.getConfiguration().getReplId())
                        .build());
            }
            Event event=node.getEvent();
            //全量同步结束
            if (event instanceof PostRdbSyncEvent||event instanceof DefaultCommand||event instanceof PreCommandSyncEvent) {
                data.getOffSetEntity().setReplId(replicationHandler.getConfiguration().getReplId());
                data.getOffSetEntity().getReplOffset().set(replicationHandler.getConfiguration().getReplOffset());

              if(node.getTaskRunTypeEnum().equals(TaskRunTypeEnum.STOCKONLY)||event instanceof PreCommandSyncEvent){
                  TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
                  taskMapper.updateOffsetAndReplId(taskId,replicationHandler.getConfiguration().getReplOffset(),replicationHandler.getConfiguration().getReplId());
              }
            }
        }catch (Exception e){
            log.info("[{}]update offset fail,replid[{}],offset[{}]",taskId,replicationHandler.getConfiguration().getReplId(),replicationHandler.getConfiguration().getReplOffset());
        }



    }
}
