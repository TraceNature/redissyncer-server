package syncer.transmission.util.manger;

import syncer.replica.entity.TaskStatusType;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.TaskModel;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
public interface ITaskStatusManger {
    void addDbThread(String taskId, TaskModel taskModel) throws Exception;
    void addDbThread(String taskId, TaskDataEntity taskDataEntity) throws Exception;
    void addMemoryDbThread(String taskId, TaskDataEntity taskDataEntity,boolean status) throws Exception;
    void addMemoryDbThread(String threadId, TaskDataEntity taskDataEntity) throws Exception;
    void changeThreadStatus(String taskId, Long offset, TaskStatusType taskType) throws Exception;
    void updateThreadStatus(String taskId,TaskStatusType taskStatusType) throws Exception;
    void brokenTask(String taskId) throws Exception;

    void brokenStatusAndLog(Exception e, Class clazz, String taskId);

    void brokenStatusAndLog(String exceptionMsg, Class clazz, String taskId);


    void updateBrokenResult(String taskId,String brokenResult);

    void updateExpandTaskModel(String taskId);
    void updateThreadStatusAndMsg(String taskId,String msg,TaskStatusType taskStatusType) throws Exception;
    void updateThreadMsg(String taskId,String msg) throws Exception;
    /**
     * 判断任务是否关闭
     * @param taskId
     * @return
     */
    boolean isTaskClose(String taskId);
}
