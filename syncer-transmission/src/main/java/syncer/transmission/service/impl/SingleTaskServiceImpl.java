package syncer.transmission.service.impl;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import syncer.common.exception.TaskMsgException;
import syncer.common.util.ThreadPoolUtils;
import syncer.replica.entity.SyncType;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.util.SyncTypeUtils;
import syncer.transmission.entity.OffSetEntity;
import syncer.transmission.entity.StartTaskEntity;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.service.ISingleTaskService;
import syncer.transmission.strategy.taskcheck.RedisTaskStrategyGroupType;
import syncer.transmission.strategy.taskcheck.TaskCheckStrategyGroupSelecter;
import syncer.transmission.task.RedisDataCommandUpTransmissionTask;
import syncer.transmission.task.RedisDataSyncTransmissionTask;
import syncer.transmission.util.ExpandTaskUtils;
import syncer.transmission.util.lock.TaskRunUtils;
import syncer.transmission.util.redis.RedisReplIdCheck;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
@Service
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
            //获取offset和服务id
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
        //初始化ExpandTaskModel
        ExpandTaskUtils.loadingExpandTaskData(taskModel, dataEntity);
        SingleTaskDataManagerUtils.addMemThread(taskModel.getId(), dataEntity, true);
        //创建中
        SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(), taskModel.getOffset(), TaskStatusType.CREATING);
        try {
            //校验
            TaskCheckStrategyGroupSelecter.select(RedisTaskStrategyGroupType.NODISTINCT, null, taskModel).run(null, taskModel);

        } catch (Exception e) {
            SingleTaskDataManagerUtils.brokenTask(taskModel.getId());
            throw e;
        }

        ThreadPoolUtils.exec(new RedisDataSyncTransmissionTask(taskModel, true));
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

        dataEntity.getTaskModel().setStatus(TaskStatusType.CREATING.getCode());

        //初始化ExpandTaskModel
        ExpandTaskUtils.loadingExpandTaskData(taskModel, dataEntity);

        SingleTaskDataManagerUtils.addMemThread(taskModel.getId(), dataEntity, true);

        //创建中
        SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(), -1L, TaskStatusType.CREATING);

        try {

            //校验
            TaskCheckStrategyGroupSelecter.select(RedisTaskStrategyGroupType.NODISTINCT, null, taskModel).run(null, taskModel);

        } catch (Exception e) {
            SingleTaskDataManagerUtils.brokenTask(taskModel.getId());
            throw e;
        }


        //创建完成
        SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(), -1L, TaskStatusType.CREATED);

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
                Lock lock = TaskRunUtils.getTaskLock(taskModel.getId());
                lock.lock();
                try {
                    if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskModel.getId())) {
                        TaskDataEntity data = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId());
                        try {
                            try {
                                data.getReplication().close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            SingleTaskDataManagerUtils.changeThreadStatus(taskModel.getId(), data.getOffSetEntity().getReplOffset().get(), TaskStatusType.STOP);

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
                        if (Objects.isNull(taskModel)) {
                            StartTaskEntity startTaskEntity = StartTaskEntity
                                    .builder()
                                    .code("1001")
                                    .taskId(taskModel.getId())
                                    .msg("The current task is not running")
                                    .build();
                            result.add(startTaskEntity);
                        } else {
                            StartTaskEntity startTaskEntity = StartTaskEntity
                                    .builder()
                                    .code("1002")
                                    .taskId(taskModel.getId())
                                    .msg("The task does not exist. Please create the task first")
                                    .build();
                            result.add(startTaskEntity);
                        }
                    }
                } finally {
                    lock.unlock();
                }
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
        StartTaskEntity result = null;
        Lock lock = TaskRunUtils.getTaskLock(taskId);
        lock.lock();
        try {
            if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskId)) {
                TaskDataEntity data = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId);
                try {
                    data.getReplication().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SingleTaskDataManagerUtils.changeThreadStatus(taskId, data.getOffSetEntity().getReplOffset().get(), TaskStatusType.STOP);
                result = StartTaskEntity
                        .builder()
                        .code("2000")
                        .taskId(taskId)
                        .msg("Task stopped successfully")
                        .build();


            } else {
                TaskModel taskModel = SqlOPUtils.findTaskById(taskId);
                if (taskModel != null) {
                    result = StartTaskEntity
                            .builder()
                            .code("1001")
                            .taskId(taskId)
                            .msg("The current task is not running")
                            .build();
                } else {
                    result = StartTaskEntity
                            .builder()
                            .code("1002")
                            .taskId(taskId)
                            .msg("The task does not exist. Please create the task first")
                            .build();
                }
            }

        } catch (Exception e) {
            result = StartTaskEntity
                    .builder()
                    .code("1000")
                    .taskId(taskId)
                    .msg("Task stopped fail")
                    .build();
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return result;
    }

    @Override
    public StartTaskEntity startTaskByTaskId(String taskId, boolean afresh) {
        StartTaskEntity result = null;
        Lock lock = TaskRunUtils.getTaskLock(taskId);
        lock.lock();
        try {
            if (!SingleTaskDataManagerUtils.isTaskClose(taskId)) {
                result = StartTaskEntity
                        .builder()
                        .code("1001")
                        .taskId(taskId)
                        .msg("The task is running")
                        .build();
                return result;
            }
            TaskModel taskModel = SqlOPUtils.findTaskById(taskId);
            taskModel.setTaskMsg("");
            /**
             * todo offset更新
             */
            taskModel.setAfresh(afresh);

            if (Objects.isNull(taskModel)) {
                return StartTaskEntity
                        .builder()
                        .code("1002")
                        .taskId(taskId)
                        .msg("The task has not been created yet")
                        .build();
            }

            SqlOPUtils.updateAfreshsetById(taskId, afresh);
            String id = null;
            if (!SingleTaskDataManagerUtils.isTaskClose(taskId)) {
                return StartTaskEntity
                        .builder()
                        .code("1001")
                        .taskId(taskId)
                        .msg("The task is running")
                        .build();
            }
            if (taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())) {
                id = runSyncerCommandDumpUpTask(taskModel);
            } else {
                id = runSyncerTask(taskModel);
            }


            result = StartTaskEntity
                    .builder()
                    .code("2000")
                    .taskId(id)
                    .msg("OK")
                    .build();


        } catch (Exception e) {
            result = StartTaskEntity
                    .builder()
                    .code("1000")
                    .taskId(taskId)
                    .msg("Error_" + e.getMessage())
                    .build();
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return result;
    }

    @Override
    public List<StartTaskEntity> removeTaskByGroupId(String groupId) throws Exception {
        List<StartTaskEntity>result=Lists.newArrayList();
        List<TaskModel>taskModelList=SqlOPUtils.findTaskByGroupId(groupId);
        for (TaskModel taskModel:taskModelList){
            Lock lock=  TaskRunUtils.getTaskLock(taskModel.getId());
            lock.lock();
            try {
                if(SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskModel.getId())){
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1001")
                            .taskId(taskModel.getId())
                            .groupId(taskModel.getGroupId())
                            .msg("task is running,please stop the task first")
                            .build();
                    result.add(startTaskEntity);

                }else {
                    try {
                        SqlOPUtils.deleteTaskById(taskModel.getId());
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("2000")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("Delete successful")
                                .build();
                        result.add(startTaskEntity);

                    } catch (Exception e) {
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("1000")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("Delete failed")
                                .build();
                        result.add(startTaskEntity);
                    }
                }
            }finally {
                lock.unlock();
            }
        }
        return result;
    }

    @Override
    public StartTaskEntity removeTaskByTaskId(String taskId) throws Exception {
        StartTaskEntity result=null;
        if(SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskId)){
            result=StartTaskEntity
                    .builder()
                    .code("1001")
                    .taskId(taskId)
                    .msg("task is running,please stop the task first")
                    .build();
            return result;
        }

        try {
            if(SqlOPUtils.findTaskById(taskId)!=null){
                SqlOPUtils.deleteTaskById(taskId);
                result=StartTaskEntity
                        .builder()
                        .code("2000")
                        .taskId(taskId)
                        .msg("Delete successful")
                        .build();
            }else {
                result=StartTaskEntity
                        .builder()
                        .code("1002")
                        .taskId(taskId)
                        .msg("Task does not exist")
                        .build();
            }

        } catch (Exception e) {
            result=StartTaskEntity
                    .builder()
                    .code("1000")
                    .taskId(taskId)
                    .msg("Delete failed")
                    .build();
        }
        return result;
    }
}
