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

package syncer.transmission.util.manger;

import syncer.replica.status.TaskStatus;
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

    public static void changeThreadStatus(String taskId, Long offset, TaskStatus taskType) throws Exception {
        taskStatusManger.changeThreadStatus(taskId,offset,taskType);
    }


    public static void updateThreadStatus(String taskId, TaskStatus taskStatusType) throws Exception {
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
