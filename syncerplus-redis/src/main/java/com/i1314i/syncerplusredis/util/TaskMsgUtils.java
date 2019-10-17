package com.i1314i.syncerplusredis.util;

import com.i1314i.syncerplusredis.constant.TaskMsgConstant;
import com.i1314i.syncerplusredis.constant.ThreadStatusEnum;
import com.i1314i.syncerplusredis.entity.dto.task.ListTaskMsgDto;
import com.i1314i.syncerplusredis.entity.thread.ThreadMsgEntity;
import com.i1314i.syncerplusredis.entity.thread.ThreadReturnMsgEntity;
import com.i1314i.syncerplusredis.exception.TaskMsgException;
import com.i1314i.syncerplusredis.replicator.Replicator;


import com.i1314i.syncerplusredis.util.code.CodeUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskMsgUtils {

    @Getter@Setter
    private static Map<String, ThreadMsgEntity> aliveThreadHashMap=new ConcurrentHashMap<String,ThreadMsgEntity>();

    /**
     * 停止但还活着的线程
     */
    @Getter
    private static Map<String,ThreadMsgEntity> deadThreadHashMap=new ConcurrentHashMap<String,ThreadMsgEntity>();


    public static Map<String, ThreadMsgEntity> getAliveThreadHashMap() {
        return aliveThreadHashMap;
    }

    public static Map<String, ThreadMsgEntity> getDeadThreadHashMap() {
        return deadThreadHashMap;
    }

    /**
     * 添加线程到aliveMap
     * @param threadId
     * @param threadMsgEntity
     */
    public synchronized static void addAliveThread(String threadId,ThreadMsgEntity threadMsgEntity) throws TaskMsgException {
        if(checkThreadMsg(threadMsgEntity)){
//            throw new TaskMsgException("相同配置任务已存在，请修改任务名");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR));
        }
        if(!aliveThreadHashMap.containsKey(threadId)){
            aliveThreadHashMap.put(threadId,threadMsgEntity);
        }

    }

    /**
     * 检查信息
     * @param threadMsgEntity
     * @return
     */
    public synchronized  static  boolean checkThreadMsg(ThreadMsgEntity threadMsgEntity){
        AtomicBoolean status= new AtomicBoolean(false);
        TaskMsgUtils.aliveThreadHashMap.entrySet().forEach(alive->{
            if(alive.getValue().getRedisClusterDto().equals(threadMsgEntity.getRedisClusterDto())){
                status.set(true);
                return;
            }
        });
        return status.get();
    }






    /**
     * 获取ThreadMsgEntity
     * @param taskId
     * @return
     */
    public synchronized static ThreadMsgEntity getThreadMsgEntity(String taskId){
        if(aliveThreadHashMap.containsKey(taskId)){
            return aliveThreadHashMap.get(taskId);
        }
        return null;
    }

    /**
     * 从aliveMap中将线程删除加入deadMap
     * @param threadName
     */
    public synchronized static void removeAliveThread(String threadName,ThreadMsgEntity thread){
        if(aliveThreadHashMap.containsKey(threadName)){
            deadThreadHashMap.put(threadName,thread);
            aliveThreadHashMap.remove(threadName);
        }

    }


    public synchronized static void removeAliveThread(String threadName){
        if(aliveThreadHashMap.containsKey(threadName)){
            deadThreadHashMap.put(threadName,aliveThreadHashMap.get(threadName));
            aliveThreadHashMap.remove(threadName);
        }

    }


    /**
     * 加入DeadMap 并将同名thread从中删除
     * @param threadName
     * @param thread
     */
    public synchronized static void addDeadThread(String threadName,ThreadMsgEntity thread){
        aliveThreadHashMap.remove(threadName);
        deadThreadHashMap.put(threadName,thread);
    }

    /**
     * 删除dead中的线程
     * @param threadName
     */
    public synchronized static void removeDeadThread(String threadName){
        deadThreadHashMap.remove(threadName);
    }




    public synchronized static void removeAliveAndDeadThread(String threadName){
        deadThreadHashMap.remove(threadName);
        aliveThreadHashMap.remove(threadName);
    }



    public static boolean containsKeyAliveMap(String key){
        return aliveThreadHashMap.containsKey(key);
    }


    public static boolean containsKeyDeadMap(String key){
        return deadThreadHashMap.containsKey(key);
    }



    /**
     * 异常停止在运行(RUN)中的任务线程
     * @param taskids
     * @return
     */
    public synchronized  static  Map<String,String> brokenCreateThread(List<String> taskids) throws TaskMsgException {
        Map<String,String>taskMap=new HashMap<>();

        for (String taskId:taskids
        ) {

            if(!TaskMsgUtils.getAliveThreadHashMap().containsKey(taskId)){
                continue;
            }


            if(StringUtils.isEmpty(taskId)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));
//                throw new TaskMsgException("taskids中不能存在空值");
            }
        }

        for (String taskId:taskids
        ) {
            if(!StringUtils.isEmpty(taskId)){
                ThreadMsgEntity entity=TaskMsgUtils.getThreadMsgEntity(taskId);
                if(null!=entity){
                    //运行中
                    if(entity.getStatus().equals(ThreadStatusEnum.RUN)){
                        if(null!=entity.getRList()&&entity.getRList().size()>0){
                            try {
                                for (Replicator r:entity.getRList()
                                ) {
                                    r.close();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        entity.setStatus(ThreadStatusEnum.BROKEN);
                        TaskMsgUtils.getAliveThreadHashMap().put(taskId,entity);
                        taskMap.put(taskId,"Task BROKEN successfully");
                    }else {
                        taskMap.put(taskId,"The current task is not running");
                    }
                }else {
                    taskMap.put(taskId,"The task does not exist. Please create the task first");
                }
            }
        }

        return taskMap;
    }

}
