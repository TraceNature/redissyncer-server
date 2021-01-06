package syncer.transmission.service;

import syncer.common.exception.TaskMsgException;
import syncer.transmission.entity.StartTaskEntity;
import syncer.transmission.model.TaskModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 任务管理服务
 * @Date 2020/12/15
 */
public interface ISingleTaskService {
    /**
     * 启动任务
     * @param taskModel
     * @return taskId
     * @throws Exception
     */
    String runSyncerTask(TaskModel taskModel) throws Exception;

    /**
     * 开启增量数据任务
     * @param taskModel
     * @return
     * @throws Exception
     */
    String runSyncerCommandDumpUpTask(TaskModel taskModel) throws Exception;

    /**
     * 根据GroupId停止任务
     * @param groupId
     * @return
     * @throws TaskMsgException
     */
    List<StartTaskEntity> stopTaskListByGroupId(String groupId);

    /**
     * 根据taskId停止任务
     * @param taskId
     * @return
     * @throws TaskMsgException
     */
    StartTaskEntity stopTaskListByTaskId(String taskId);

    StartTaskEntity startTaskByTaskId(String taskId, boolean afresh);


    List<StartTaskEntity> removeTaskByGroupId(String groupId) throws Exception ;
    StartTaskEntity removeTaskByTaskId(String taskId)throws Exception;
}
