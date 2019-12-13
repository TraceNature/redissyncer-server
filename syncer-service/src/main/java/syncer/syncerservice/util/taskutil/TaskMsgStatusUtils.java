package syncer.syncerservice.util.taskutil;

import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.util.TaskMsgUtils;

public class TaskMsgStatusUtils {
    /**
     * 检测任务是否不处于run状态
     * @return
     */
    public static synchronized boolean doThreadisCloseCheckTask(String taskId) {
        /**
         * 当aliveMap中不存在此线程时关闭
         */
        try {
            ThreadMsgEntity entity= TaskMsgUtils.getAliveThreadHashMap().get(taskId);
            if(null==entity)
                return true;

            if(!entity.getStatus().equals(ThreadStatusEnum.RUN)){
                return true;
            }
            return false;
        }catch (Exception e){
            return true;
        }

    }



}
