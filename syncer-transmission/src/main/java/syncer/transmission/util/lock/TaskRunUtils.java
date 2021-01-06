package syncer.transmission.util.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/16
 */
public class TaskRunUtils {
    static Map<String, Lock> LOCK_MAP=new ConcurrentHashMap<>();
    static Map<String, Boolean> TASK_START_DATA=new ConcurrentHashMap<>();

    public static synchronized void  addTask(String taskId){
        LOCK_MAP.put(taskId,new ReentrantLock());
    }

    public static synchronized void  removeTask(String taskId){
        LOCK_MAP.remove(taskId);
    }

    public static synchronized boolean  containsTask(String taskId){
        return  LOCK_MAP.containsKey(taskId);
    }


    public static synchronized Lock  getTaskLock(String taskId){
        if(!LOCK_MAP.containsKey(taskId)){
            LOCK_MAP.put(taskId,new ReentrantLock());
        }
        return LOCK_MAP.get(taskId);
    }

    public static void putTaskStartData(String taskId){
        TASK_START_DATA.put(taskId,true);
    }

}
