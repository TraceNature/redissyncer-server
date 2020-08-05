package syncer.syncerplusredis.util;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.entity.TaskDataEntity;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/16
 */

@Slf4j
public class TaskErrorUtils {

    public  static void updateStatusAndLog(Exception e, Class clazz, String taskId, TaskDataEntity dataEntity){

        try {
            //清除内存信息并更新数据库状态
            TaskDataManagerUtils.removeThread(taskId,dataEntity.getOffSetEntity().getReplOffset().get());
        } catch (Exception ex) {
            log.warn("任务Id【{}】任务创建失败 ，Class【{}】,失败原因【{}】", taskId, clazz.toString(),e.getMessage());
            ex.printStackTrace();
        }
        log.warn("任务Id【{}】任务创建失败 ，Class【{}】,失败原因【{}】", taskId, clazz.toString(),e.getMessage());
    }



    public  static void brokenStatusAndLog(Exception e, Class clazz, String taskId){

        try {
            //清除内存信息并更新数据库状态
            TaskDataManagerUtils.removeThread(taskId);
        } catch (Exception ex) {
            log.warn("任务Id【{}】任务启动/运行异常停止 ，Class【{}】,异常原因【{}】", taskId, clazz.toString(),e.getMessage());
            ex.printStackTrace();
        }
        log.warn("任务Id【{}】任务启动/运行异常停止 ，Class【{}】,异常原因【{}】", taskId, clazz.toString(),e.getMessage());
      e.printStackTrace();
    }

    public  static void brokenStatusAndLog(String msg , Class clazz, String taskId){
        try {
            //清除内存信息并更新数据库状态
            TaskDataManagerUtils.removeThread(taskId);
        } catch (Exception ex) {
            log.warn("任务Id【{}】任务启动/运行异常停止 ，Class【{}】,异常原因【{}】", taskId, clazz.toString(),msg);
            ex.printStackTrace();
        }
        log.warn("任务Id【{}】任务启动/运行异常停止 ，Class【{}】,异常原因【{}】", taskId, clazz.toString(),msg);
    }
}
