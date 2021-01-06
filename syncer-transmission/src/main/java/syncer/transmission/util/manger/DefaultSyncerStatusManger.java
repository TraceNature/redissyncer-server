package syncer.transmission.util.manger;

import syncer.replica.entity.TaskStatusType;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.manger.impl.MemoryAndSqliteTaskStatusManger;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/21
 */
public class DefaultSyncerStatusManger   {
    static ITaskStatusManger taskStatusManger=new MemoryAndSqliteTaskStatusManger();

    public static void addDbThread(String taskId, TaskModel taskModel) throws Exception {
        taskStatusManger.addDbThread(taskId,taskModel);
    }

    public static void addDbThread(String taskId, TaskDataEntity taskDataEntity) throws Exception {
        taskStatusManger.addDbThread(taskId,taskDataEntity);
    }


    public static void addMemoryDbThread(String taskId, TaskDataEntity taskDataEntity, boolean status) throws Exception {
        taskStatusManger.addMemoryDbThread(taskId,taskDataEntity,status);
    }


    public static void addMemoryDbThread(String threadId, TaskDataEntity taskDataEntity) throws Exception {
        taskStatusManger.addMemoryDbThread(threadId,taskDataEntity);
    }

    public static void changeThreadStatus(String taskId, Long offset, TaskStatusType taskType) throws Exception {
        taskStatusManger.changeThreadStatus(taskId,offset,taskType);
    }


    public static void updateThreadStatus(String taskId, TaskStatusType taskStatusType) throws Exception {
        taskStatusManger.updateThreadStatus(taskId,taskStatusType);
    }


    public static void brokenTask(String taskId) throws Exception {
        taskStatusManger.brokenTask(taskId);
    }


    public static void brokenStatusAndLog(Exception e, Class clazz, String taskId) {
        taskStatusManger.brokenStatusAndLog(e,clazz,taskId);
    }


    public static void brokenStatusAndLog(String exceptionMsg, Class clazz, String taskId) {
        taskStatusManger.brokenStatusAndLog(exceptionMsg,clazz,taskId);
    }

    public void updateBrokenResult(String taskId, String brokenResult) {
        taskStatusManger.updateBrokenResult(taskId,brokenResult);
    }


    public static void updateExpandTaskModel(String taskId) {
        taskStatusManger.updateExpandTaskModel(taskId);
    }


    public static boolean isTaskClose(String taskId) {
        return taskStatusManger.isTaskClose(taskId);
    }


}
