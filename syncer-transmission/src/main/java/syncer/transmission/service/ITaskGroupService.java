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

package syncer.transmission.service;

import syncer.common.bean.PageBean;
import syncer.common.exception.TaskMsgException;
import syncer.transmission.entity.StartTaskEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.ListTaskParamDto;
import syncer.transmission.po.TaskModelResult;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/14
 */
public interface ITaskGroupService {
    /**
     * 创建实时命令备份任务
     * @param taskModelList
     * @return
     * @throws TaskMsgException
     */
//    ResponseResult createCommandDumpUpTask(List<TaskModel> taskModelList) throws TaskMsgException;


    /**
     * 创建redis数据同步/数据文件恢复
     * @param taskModelList
     * @return
     * @throws TaskMsgException
     */
    List<StartTaskEntity> createRedisToRedisTask(List<TaskModel> taskModelList) throws TaskMsgException;

    /**
     * 根据taskIdList停止任务
     * @param taskIdList
     * @return
     * @throws TaskMsgException
     */
    List<StartTaskEntity> batchStopTaskListByTaskIdList(List<String> taskIdList) throws TaskMsgException;

    /**
     *根据groupIdList停止任务
     * @param groupIdList
     * @return
     * @throws TaskMsgException
     */
    List<StartTaskEntity> batchStopTaskListByGroupIdList(List<String> groupIdList) throws TaskMsgException;

    /**
     * 根据groupIdList停止任务
     * @param groupId
     * @param afresh
     * @return
     * @throws TaskMsgException
     */
    List<StartTaskEntity> batchStartTaskListByGroupId(String groupId,boolean afresh) throws Exception;

    StartTaskEntity startTaskByTaskId(String taskId,boolean afresh) throws Exception;

    PageBean<TaskModelResult> listTaskListByPages(ListTaskParamDto param) throws Exception;

    List<TaskModelResult> listTaskList(ListTaskParamDto param) throws Exception;

    List<StartTaskEntity>removeTaskByGroupIdList(List<String> groupIdList) throws Exception ;

    List<StartTaskEntity> removeTaskByTaskIdList(List<String>taskIdList)throws Exception;
}
