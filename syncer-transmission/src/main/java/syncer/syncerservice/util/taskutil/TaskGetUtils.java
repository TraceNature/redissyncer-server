package syncer.syncerservice.util.taskutil;

import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;

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
            if(TaskDataManagerUtils.containsKey(taskId)){
                return TaskDataManagerUtils.get(taskId).getTaskModel().getGroupId();
            }else {
                TaskModel task= SqliteOPUtils.findTaskById(taskId);
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
