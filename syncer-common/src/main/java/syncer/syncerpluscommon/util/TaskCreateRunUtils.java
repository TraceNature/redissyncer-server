package syncer.syncerpluscommon.util;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/27
 */
public class TaskCreateRunUtils {
    static Map<String, Lock> map=new ConcurrentHashMap<>();
    static Map<String, Boolean> taskStartData=new ConcurrentHashMap<>();
    public static synchronized void  addTask(String taskId){
        map.put(taskId,new ReentrantLock());
    }

    public static synchronized void  removeTask(String taskId){
        map.remove(taskId);
    }

    public static synchronized boolean  containsTask(String taskId){

        return  map.containsKey(taskId);
    }


    public static synchronized Lock  getTaskLock(String taskId){
        if(!map.containsKey(taskId)){
            map.put(taskId,new ReentrantLock());
        }
        return map.get(taskId);
    }

    public static void putTaskStartData(String taskId){
        taskStartData.put(taskId,true);
    }

//    public static void putTaskStartData(String taskId){
//        taskStartData.put(taskId,true);
//    }
}
