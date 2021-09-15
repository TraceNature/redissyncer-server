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

package syncer.transmission.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import syncer.common.util.ThreadPoolUtils;
import syncer.replica.constant.RedisType;
import syncer.replica.status.TaskStatus;
import syncer.replica.type.SyncType;
import syncer.replica.util.SyncTypeUtils;
import syncer.transmission.entity.OffSetEntity;
import syncer.transmission.entity.StartTaskEntity;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.lock.EtcdLockCommandRunner;
import syncer.transmission.model.ExpandTaskModel;
import syncer.transmission.model.TaskModel;
import syncer.transmission.service.ISingleTaskService;
import syncer.transmission.strategy.taskcheck.RedisTaskStrategyGroupType;
import syncer.transmission.strategy.taskcheck.TaskCheckStrategyGroupSelecter;
import syncer.transmission.task.RedisDataCommandUpTransmissionTask;
import syncer.transmission.task.RedisDataSyncTransmission2KafkaTask;
import syncer.transmission.task.RedisDataSyncTransmissionTask;
import syncer.transmission.task.RedisSyncFilterByAuxKeyTransmissionTask;
import syncer.transmission.util.ExpandTaskUtils;
import syncer.transmission.util.lock.TaskRunUtils;
import syncer.transmission.util.redis.RedisReplIdCheck;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
@Service
@Slf4j
public class SingleTaskServiceImpl implements ISingleTaskService {
    RedisReplIdCheck redisReplIdCheck = new RedisReplIdCheck();

    /**
     * 运行之前已经创建过任务
     *
     * @param taskModel
     * @return
     * @throws Exception
     */
    @Override
    public String runSyncerTask(TaskModel taskModel) throws Exception {
        if (StringUtils.isEmpty(taskModel.getTaskName())) {
            StringBuilder stringBuilder = new StringBuilder();

            if (taskModel.getSourceRedisType().equals(3)) {
                stringBuilder.append(taskModel.getId())
                        .append("【")
                        .append(taskModel.getFileAddress())
                        .append("数据文件】");
                taskModel.setTaskName(stringBuilder.toString());
            } else {
                stringBuilder.append(taskModel.getId())
                        .append("【")
                        .append(taskModel.getSourceRedisAddress())
                        .append("节点】");
                taskModel.setTaskName(stringBuilder.toString());
            }
        }
        TaskDataEntity dataEntity = null;
        if (taskModel.getSyncType().equals(SyncType.SYNC.getCode()) || taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())) {

            if (RedisType.SENTINEL.getCode().equals(taskModel.getSourceRedisType())) {
                dataEntity = TaskDataEntity.builder()
                        .taskModel(taskModel)
                        .offSetEntity(OffSetEntity.builder().replId("").build())
                        .build();
            } else {
                //获取offset和服务id
                String[] data = redisReplIdCheck.selectSyncerBuffer(taskModel.getSourceUri(), SyncTypeUtils.getOffsetPlace(taskModel.getOffsetPlace()).getOffsetPlace());
                dataEntity = TaskDataEntity.builder()
                        .taskModel(taskModel)
                        .offSetEntity(OffSetEntity.builder().replId(data[1]).build())
                        .build();
                dataEntity.getOffSetEntity().getReplOffset().set(taskModel.getOffset());

            }
        } else {
            dataEntity = TaskDataEntity.builder()
                    .taskModel(taskModel)
                    .offSetEntity(OffSetEntity.builder().replId("").build())
                    .build();
        }
        //初始化ExpandTaskModel
        ExpandTaskUtils.loadingExpandTaskData(taskModel, dataEntity);
        SingleTaskDataManagerUtils.addMemThread(taskModel.getId(), dataEntity, true);
        //创建中
        SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(), taskModel.getOffset(), TaskStatus.CREATING);
        try {
            //校验
            TaskCheckStrategyGroupSelecter.select(RedisTaskStrategyGroupType.NODISTINCT, null, taskModel).run(null, taskModel);

        } catch (Exception e) {
            SingleTaskDataManagerUtils.brokenTask(taskModel.getId());
            throw e;
        }

        if (RedisType.KAFKA.getCode().equals(taskModel.getTargetRedisType())) {
            ThreadPoolUtils.exec(new RedisDataSyncTransmission2KafkaTask(taskModel, true));
        }  else {
            if (taskModel.isCircleReplication()) {
                ThreadPoolUtils.exec(new RedisSyncFilterByAuxKeyTransmissionTask(taskModel));
              } else {
                ThreadPoolUtils.exec(new RedisDataSyncTransmissionTask(taskModel, true));
              }
        }
        return taskModel.getId();
    }

    @Override
    public String runSyncerCommandDumpUpTask(TaskModel taskModel) throws Exception {
        if (StringUtils.isEmpty(taskModel.getTaskName())) {
            StringBuilder stringBuilder = new StringBuilder(taskModel.getId())
                    .append("【")
                    .append(taskModel.getSourceRedisAddress())
                    .append("节点】");
            taskModel.setTaskName(stringBuilder.toString());
        }

        TaskDataEntity dataEntity = null;

        if (taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())) {
            String[] data = redisReplIdCheck.selectSyncerBuffer(taskModel.getSourceUri(), SyncTypeUtils.getOffsetPlace(taskModel.getOffsetPlace()).getOffsetPlace());

            dataEntity = TaskDataEntity.builder()
                    .taskModel(taskModel)
                    .offSetEntity(OffSetEntity.builder().replId(data[1]).build())
                    .build();
            dataEntity.getOffSetEntity().getReplOffset().set(taskModel.getOffset());
        } else {
            dataEntity = TaskDataEntity.builder()
                    .taskModel(taskModel)
                    .offSetEntity(OffSetEntity.builder().replId("").build())
                    .build();
        }

        dataEntity.getTaskModel().setStatus(TaskStatus.CREATING.getCode());

        //初始化ExpandTaskModel
        ExpandTaskUtils.loadingExpandTaskData(taskModel, dataEntity);

        SingleTaskDataManagerUtils.addMemThread(taskModel.getId(), dataEntity, true);

        //创建中
        SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(), -1L, TaskStatus.CREATING);

        try {
            //校验
            TaskCheckStrategyGroupSelecter.select(RedisTaskStrategyGroupType.NODISTINCT, null, taskModel).run(null, taskModel);

        } catch (Exception e) {
            SingleTaskDataManagerUtils.brokenTask(taskModel.getId());
            throw e;
        }

        //创建完成
        SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(), -1L, TaskStatus.CREATED);

        try {
            ThreadPoolUtils.exec(new RedisDataCommandUpTransmissionTask(taskModel));
        } catch (Exception e) {
            SingleTaskDataManagerUtils.brokenStatusAndLog(e, this.getClass(), taskModel.getId());
        }

        return taskModel.getId();
    }


    @Override
    public List<StartTaskEntity> stopTaskListByGroupId(String groupId) {
        List<StartTaskEntity> result = Lists.newArrayList();
        try {
            List<TaskModel> taskModelList = SqlOPUtils.findTaskByGroupId(groupId);
            taskModelList.forEach(taskModel -> {
                TaskRunUtils.getTaskLock(taskModel.getTaskId(), new EtcdLockCommandRunner() {
                    @Override
                    public void run() {
                        if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskModel.getId())) {
                            TaskDataEntity data = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId());
                            try {
                                SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(), data.getOffSetEntity().getReplOffset().get(), TaskStatus.STOP);
                                try {
                                    data.getReplication().close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                StartTaskEntity startTaskEntity = StartTaskEntity
                                        .builder()
                                        .code("2000")
                                        .taskId(taskModel.getId())
                                        .msg("Task stopped successfully")
                                        .build();
                                result.add(startTaskEntity);
                            } catch (Exception e) {
                                StartTaskEntity startTaskEntity = StartTaskEntity
                                        .builder()
                                        .code("1000")
                                        .taskId(taskModel.getId())
                                        .msg("Task stopped fail")
                                        .build();
                                result.add(startTaskEntity);
                            }
                        } else {
                            StartTaskEntity startTaskEntity = StartTaskEntity
                                    .builder()
                                    .code("1001")
                                    .taskId(taskModel.getId())
                                    .msg("The current task is not running")
                                    .build();
                            result.add(startTaskEntity); 
                                result.add(startTaskEntity);
                            result.add(startTaskEntity); 
                                result.add(startTaskEntity);
                            result.add(startTaskEntity); 
                                result.add(startTaskEntity);
                            result.add(startTaskEntity); 
                                result.add(startTaskEntity);
                            result.add(startTaskEntity); 
                        }
                    }

                    @Override
                    public String lockName() {
                        return "taskRunLock" + taskModel.getTaskId();
                    }

                    @Override
                    public int grant() {
                        return 30;
                    }
                });

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据TaslId停止任务
     *
     * @param taskId
     * @return
     */
    @Override
    public StartTaskEntity stopTaskListByTaskId(String taskId) {
        final StartTaskEntity result = StartTaskEntity.builder().build();
        TaskModel taskModel = SqlOPUtils.findTaskById(taskId);
        if (taskModel == null) {
            result.setCode("1002");
            result.setTaskId(taskId);
            result.setMsg("The task does not exist. Please create the task first");
            return result;
        }
        TaskRunUtils.getTaskLock(taskId, new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskId)) {
                        TaskDataEntity data = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId);
                        try {
                            data.getReplication().close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        SingleTaskDataManagerUtils.changeThreadStatus(taskId, data.getOffSetEntity().getReplOffset().get(), TaskStatus.STOP);
                        result.setCode("2000");
                        result.setTaskId(taskId);
                        result.setMsg("Task stopped successfully");
                    } else {
                        result.setCode("1001");
                        result.setTaskId(taskId);
                        result.setMsg("The current task is not running");
                    }
                } catch (Exception e) {
                    result.setCode("1000");
                    result.setTaskId(taskId);
                    result.setMsg("Task stopped fail");
                    log.error("task {} stop fail", taskId);
                }
            }

            @Override
            public String lockName() {
                return "taskRunLock" + taskId;
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        return result;
    }

    @Override
    public StartTaskEntity startTaskByTaskId(String taskId, boolean afresh) {
        final StartTaskEntity result = StartTaskEntity.builder().build();
        TaskRunUtils.getTaskLock(taskId, new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    if (!SingleTaskDataManagerUtils.isTaskClose(taskId)) {
                        result.setCode("1001");
                        result.setTaskId(taskId);
                        result.setMsg("The task is running");
                        return;
                    }
                    TaskModel taskModel = SqlOPUtils.findTaskById(taskId);
                    if (Objects.isNull(taskModel)) {
                        result.setCode("1002");
                        result.setTaskId(taskId);
                        result.setMsg("The task has not been created yet");
                        return;
                    }
                    ExpandTaskModel expandTaskModel = taskModel.getExpandTaskJson();
                    expandTaskModel.fileSize.set(0L);
                    expandTaskModel.readFileSize.set(0L);
                    taskModel.updateExpandJson(expandTaskModel);
                    taskModel.setTaskMsg("");
                    /**
                     * todo offset更新
                     */
                    taskModel.setAfresh(afresh);
                    SqlOPUtils.updateAfreshsetById(taskId, afresh);
                    String id = null;
                    if (!SingleTaskDataManagerUtils.isTaskClose(taskId)) {
                        result.setCode("1001");
                        result.setTaskId(taskId);
                        result.setMsg("The task is running");
                        return;

                    }
                    if (taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())) {
                        id = runSyncerCommandDumpUpTask(taskModel);
                    } else {
                        id = runSyncerTask(taskModel);
                    }
                    result.setCode("2000");
                    result.setTaskId(id);
                    result.setMsg("OK");
                    return;
                } catch (Exception e) {
                    result.setCode("1000");
                    result.setTaskId(taskId);
                    result.setMsg("Error_" + e.getMessage());
                    log.error("startTaskByTaskId {} fail ", taskId);
                    SingleTaskDataManagerUtils.brokenStatusAndLog(e, this.getClass(), taskId);
                    e.printStackTrace();
                    return;
                }
            }

            @Override
            public String lockName() {
                return "taskRunLock" + taskId;
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return result;
    }

    @Override
    public List<StartTaskEntity> removeTaskByGroupId(String groupId) throws Exception {
        List<StartTaskEntity> result = Lists.newArrayList();
        List<TaskModel> taskModelList = SqlOPUtils.findTaskByGroupId(groupId);
        for (TaskModel taskModel : taskModelList) {
            TaskRunUtils.getTaskLock(taskModel.getTaskId(), new EtcdLockCommandRunner() {
                @Override
                public void run() {
                    if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskModel.getId())) {
                        StartTaskEntity startTaskEntity = StartTaskEntity
                                .builder()
                                .code("1001")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("task is running,please stop the task first")
                                .build();
                        result.add(startTaskEntity);
                    } else {
                        try {
                            SqlOPUtils.deleteTaskById(taskModel.getId());
                            StartTaskEntity startTaskEntity = StartTaskEntity
                                    .builder()
                                    .code("2000")
                                    .taskId(taskModel.getId())
                                    .groupId(taskModel.getGroupId())
                                    .msg("Delete successful")
                                    .build();
                            result.add(startTaskEntity);

                        } catch (Exception e) {
                            StartTaskEntity startTaskEntity = StartTaskEntity
                                    .builder()
                                    .code("1000")
                                    .taskId(taskModel.getId())
                                    .groupId(taskModel.getGroupId())
                                    .msg("Delete failed")
                                    .build();
                            result.add(startTaskEntity);
                        }
                    }
                }

                @Override
                public String lockName() {
                    return "taskRunLock" + taskModel.getTaskId();
                }

                @Override
                public int grant() {
                    return 30;
                }
            });
        }
        return result;
    }

    @Override
    public StartTaskEntity removeTaskByTaskId(String taskId) throws Exception {
        StartTaskEntity result = null;
        if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskId)) {
            result = StartTaskEntity
                    .builder()
                    .code("1001")
                    .taskId(taskId)
                    .msg("task is running,please stop the task first")
                    .build();
            return result;
        }
        try {
            if (SqlOPUtils.findTaskById(taskId) != null) {
                SqlOPUtils.deleteTaskById(taskId);
                result = StartTaskEntity
                        .builder()
                        .code("2000")
                        .taskId(taskId)
                        .msg("Delete successful")
                        .build();
            } else {
                result = StartTaskEntity
                        .builder()
                        .code("1002")
                        .taskId(taskId)
                        .msg("Task does not exist")
                        .build();
            }

        } catch (Exception e) {
            result = StartTaskEntity
                    .builder()
                    .code("1000")
                    .taskId(taskId)
                    .msg("Delete failed")
                    .build();
        }
        return result;
    }
}
