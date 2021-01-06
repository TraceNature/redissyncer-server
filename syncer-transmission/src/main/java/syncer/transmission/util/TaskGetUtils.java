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