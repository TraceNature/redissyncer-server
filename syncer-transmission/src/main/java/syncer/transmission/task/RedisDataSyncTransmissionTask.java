// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.task;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.jedis.HostAndPort;
import syncer.replica.config.RedisURI;
import syncer.replica.config.ReplicConfig;
import syncer.replica.constant.RedisType;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.event.*;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.listener.EventListener;
import syncer.replica.listener.TaskStatusListener;
import syncer.replica.listener.ValueDumpIterableEventListener;
import syncer.replica.parser.syncer.ValueDumpIterableRdbParser;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.register.DefaultCommandRegister;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.replica.status.TaskStatus;
import syncer.replica.type.SyncType;
import syncer.replica.util.SyncTypeUtils;
import syncer.replica.util.TaskRunTypeEnum;
import syncer.replica.util.strings.Strings;
import syncer.transmission.checkpoint.breakpoint.BreakPoint;
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
import java.nio.charset.StandardCharsets;
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
    BreakPoint breakPoint=new BreakPoint();
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
                RedisType redisType=null;
                List<HostAndPort>hostAndPorts= Lists.newArrayList();
                RedisURI suri = new RedisURI(taskModel.getSourceUri());
                if(RedisType.SENTINEL.getCode().equals(taskModel.getSourceRedisType())){
                    redisType=RedisType.SENTINEL;
                    String hosts=taskModel.getSourceHost();
                    String[]hosp=hosts.split(";");
                    for (int i = 0; i < hosp.length; i++) {
                        if(hosp[i].contains(":")){
                            String[]host=hosp[i].split(":");
                            hostAndPorts.add(new HostAndPort(host[0],Integer.valueOf(host[1])));
                        }
                    }
                }else{

                    KeyCountUtils.updateKeyCount(taskModel.getId(),suri);
                }

                replication = new RedisReplication(suri, taskModel.isAfresh(),redisType,hostAndPorts);
            } else {
                //文件
                replication = new RedisReplication(taskModel.getFileAddress(), SyncTypeUtils.getSyncType(taskModel.getSyncType()).getFileType(),  ReplicConfig.defaultConfig().setTaskId(taskModel.getTaskId()));
            }
            //注册增量命令解析器
            final Replication replicationHandler = DefaultCommandRegister.addCommandParser(replication);
            replicationHandler.getConfig().setTaskId(taskModel.getTaskId());
            //注册RDB全量解析器

            replicationHandler.setRdbParser(new ValueDumpIterableRdbParser(replicationHandler, taskModel.getRdbVersion()));

            OffSetEntity offset =null;


            offset=breakPoint.checkPointOffset(taskModel);

            /** old version

            TaskDataEntity taskDataEntity=SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId());
            if(Objects.nonNull(taskDataEntity)){
                offset = taskDataEntity.getOffSetEntity();
            }
           */

            if (offset == null) {
                offset = new OffSetEntity();
                SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId()).setOffSetEntity(offset);
            } else {
                if (StringUtils.isEmpty(offset.getReplId())) {
                    offset.setReplId(replicationHandler.getConfig().getReplId());
                } else if (offset.getReplOffset().get() > -1) {
                    if (!taskModel.isAfresh()) {
                        replicationHandler.getConfig().setReplOffset(offset.getReplOffset().get());
                        replicationHandler.getConfig().setReplId(offset.getReplId());
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
                    replicationHandler.getConfig().setReplOffset(offsetNum);
                    replicationHandler.getConfig().setReplId(data[1]);
                }
            }


            /**
            if(taskModel.getTargetUri().size()>1){
                taskModel.setTargetRedisType(2);
            }
            **/
            RedisType branchType=SyncTypeUtils.getRedisType(taskModel.getTargetRedisType());
            RedisClient client = RedisClientFactory.createRedisClient(branchType, taskModel.getTargetHost(), taskModel.getTargetPort(), taskModel.getTargetPassword(),taskModel.getSourceHost(), taskModel.getSourcePort(), taskModel.getBatchSize(),taskModel.getErrorCount(), taskModel.getId(),null,null);
            //根据type生成相对节点List [List顺序即为filter节点执行顺序]
            List<CommonProcessingStrategy> commonFilterList = ProcessingRunStrategyListSelecter.getStrategyList(SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType(),taskModel,client);
            ISyncerCompensator syncerCompensator= ISyncerCompensatorFactory.createRedisClient(branchType,taskModel.getId(),client);
            SendCommandWithOutQueue sendCommandWithOutQueue=SendCommandWithOutQueue.builder()
                    .filterChain(ProcessingRunStrategyChain.builder().commonFilterList(commonFilterList).build())
                    .replication(replicationHandler)
                    .taskId(taskModel.getTaskId())
                    .taskModel(taskModel)
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
                            .dbMapper(taskModel.loadDbMapping())
                            .redisVersion(taskModel.getRedisVersion())
                            .baseOffSet(baseoffset)
                            .replId(replicationHandler.getConfig().getReplId())
                            .replOffset(replicationHandler.getConfig().getReplOffset())
                            .taskRunTypeEnum(SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType())
                            .fileType(SyncTypeUtils.getSyncType(taskModel.getSyncType()).getFileType())
                            .build();


                    //更新offset
                    updateOffset(taskModel.getId(),replicationHandler,node);

                    sendCommandWithOutQueue.run(node);


                }

                @Override
                public String eventListenerName() {
                    return taskModel.getTaskId()+"_eventListenerName";
                }
            }));

            /**
             * 任务状态
             */
            replicationHandler.addTaskStatusListener(new TaskStatusListener() {
                @Override
                public void handler(Replication replication, SyncerTaskEvent event) {
                    String taskId=event.getTaskId();
                    try {
                        SingleTaskDataManagerUtils.changeThreadStatus(taskId,event.getOffset(),event.getEvent());
                        if(Objects.nonNull(event.getMsg())&&event.getEvent().equals(TaskStatus.BROKEN)){
                            SingleTaskDataManagerUtils.updateThreadMsg(taskId,event.getMsg());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public String eventListenerName() {
                    return taskModel.getTaskId()+"_TaskStatusListener";
                }
            });

            //任务运行
            SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(),taskModel.getOffset(), TaskStatus.STARTING);


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
                        .replId(replicationHandler.getConfig().getReplId())
                        .build());
            }
            Event event=node.getEvent();
            //全量同步结束
            if (event instanceof PostRdbSyncEvent ||event instanceof DefaultCommand ||event instanceof PreCommandSyncEvent) {
                data.getOffSetEntity().setReplId(replicationHandler.getConfig().getReplId());
                data.getOffSetEntity().getReplOffset().set(replicationHandler.getConfig().getReplOffset());

                if(node.getTaskRunTypeEnum().equals(TaskRunTypeEnum.STOCKONLY)||event instanceof PreCommandSyncEvent){
                    SqlOPUtils.updateOffsetAndReplId(taskId,replicationHandler.getConfig().getReplOffset(),replicationHandler.getConfig().getReplId());
                }
            }
        }catch (Exception e){
            log.info("[{}]update offset fail,replid[{}],offset[{}]",taskId,replicationHandler.getConfig().getReplId(),replicationHandler.getConfig().getReplOffset());
        }

    }
}
