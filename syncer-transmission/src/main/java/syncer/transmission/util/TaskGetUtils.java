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

package syncer.transmission.util;

import syncer.transmission.model.TaskModel;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/22
 */

public class TaskGetUtils {
    /**
     * 返回任务的GroupId
     * @param taskId
     * @return
     */

    public synchronized static String getRunningTaskGroupId(String taskId){
        try {
            if(SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskId)){
                return SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getTaskModel().getGroupId();
            }else {
                TaskModel task= SqlOPUtils.findTaskById(taskId);
                if(task!=null){
                    return task.getGroupId();
                }
            }
            return "";
        }catch (Exception e){
            return "";
        }
    }
}