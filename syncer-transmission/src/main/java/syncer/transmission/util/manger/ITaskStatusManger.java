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
    void changeThreadStatus(String taskId, Long offset, TaskStatus taskType) throws Exception;
    void updateThreadStatus(String taskId,TaskStatus taskStatusType) throws Exception;
    void brokenTask(String taskId) throws Exception;

    void brokenStatusAndLog(Exception e, Class clazz, String taskId);

    void brokenStatusAndLog(String exceptionMsg, Class clazz, String taskId);


    void updateBrokenResult(String taskId,String brokenResult);

    void updateExpandTaskModel(String taskId);
    void updateThreadStatusAndMsg(String taskId,String msg,TaskStatus taskStatusType) throws Exception;
    void updateThreadMsg(String taskId,String msg) throws Exception;
    /**
     * 判断任务是否关闭
     * @param taskId
     * @return
     */
    boolean isTaskClose(String taskId);
}
