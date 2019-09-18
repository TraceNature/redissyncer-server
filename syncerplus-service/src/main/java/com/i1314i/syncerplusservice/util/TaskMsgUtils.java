package com.i1314i.syncerplusservice.util;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusredis.replicator.Replicator;
import com.i1314i.syncerplusservice.constant.TaskMsgConstant;
import com.i1314i.syncerplusservice.constant.ThreadStatusEnum;
import com.i1314i.syncerplusservice.entity.dto.task.ListTaskMsgDto;
import com.i1314i.syncerplusservice.entity.thread.ThreadMsgEntity;
import com.i1314i.syncerplusservice.entity.thread.ThreadReturnMsgEntity;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.code.CodeUtils;

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
     * 启动在非运行(RUN)中的任务线程
     * @param taskids
     * @return
     */
    public synchronized  static  Map<String,String> startCreateThread(List<String> taskids, IRedisReplicatorService redisBatchedReplicatorService) throws TaskMsgException {
        Map<String,String>taskMap=new HashMap<>();


        for (String taskId:taskids
        ) {

            if(!aliveThreadHashMap.containsKey(taskId)){
//                throw new TaskMsgException("taskid为【"+taskId+"】的任务还未创建");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUN_ERROR_CODE,"taskid为【"+taskId+"】的任务还未创建"));
            }

            if(StringUtils.isEmpty(taskId)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));
//                throw new TaskMsgException("taskids中不能存在空值");
            }
            ThreadMsgEntity entity=getThreadMsgEntity(taskId);
            if(entity.getStatus().equals(ThreadStatusEnum.RUN)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUNING_ERROR_CODE,"任务：【"+taskId+"】已经在运行中"));
//                throw new TaskMsgException("任务：【"+taskId+"】已经在运行中");
            }

        }

        for (String taskId:taskids
        ) {
            if(!StringUtils.isEmpty(taskId)){
                ThreadMsgEntity entity=getThreadMsgEntity(taskId);

                if(null!=entity){
                    redisBatchedReplicatorService.batchedSync(entity.getRedisClusterDto(),taskId);
                    entity.setStatus(ThreadStatusEnum.RUN);
                    aliveThreadHashMap.put(taskId,entity);
                    taskMap.put(taskId,"Task started successfully");
                }else {
                    taskMap.put(taskId,"The task does not exist. Please create the task first");
                }
            }
        }

        return taskMap;
    }
    public synchronized  static  Map<String,String> startCreateThread(String taskId, IRedisReplicatorService redisBatchedReplicatorService) throws TaskMsgException {
        Map<String,String>taskMap=new HashMap<>();

        if(StringUtils.isEmpty(taskId)){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));
//                throw new TaskMsgException("taskids中不能存在空值");
        }

            if(!aliveThreadHashMap.containsKey(taskId)){
//                throw new TaskMsgException("taskid为【"+taskId+"】的任务还未创建");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUN_ERROR_CODE,"taskid为【"+taskId+"】的任务还未创建"));
            }


            ThreadMsgEntity entity=getThreadMsgEntity(taskId);
            if(entity.getStatus().equals(ThreadStatusEnum.RUN)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUNING_ERROR_CODE,"任务：【"+taskId+"】已经在运行中"));
//                throw new TaskMsgException("任务：【"+taskId+"】已经在运行中");
            }



            if(!StringUtils.isEmpty(taskId)){

                if(null!=entity){
                    redisBatchedReplicatorService.batchedSync(entity.getRedisClusterDto(),taskId);
                    entity.setStatus(ThreadStatusEnum.RUN);
                    aliveThreadHashMap.put(taskId,entity);
                    taskMap.put(taskId,"Task started successfully");
                }else {
                    taskMap.put(taskId,"The task does not exist. Please create the task first");
                }
            }

        return taskMap;
    }


    /**
     * 停止在运行(RUN)中的任务线程
     * @param taskids
     * @return
     */
    public synchronized  static  Map<String,String> stopCreateThread(List<String> taskids) throws TaskMsgException {
        Map<String,String>taskMap=new HashMap<>();


        for (String taskId:taskids
        ) {

            if(!aliveThreadHashMap.containsKey(taskId)){
//                throw new TaskMsgException("taskid为【"+taskId+"】的任务还未创建");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_UNCREATE_ERROR_CODE,"taskid为【"+taskId+"】的任务还未创建"));
            }

            if(StringUtils.isEmpty(taskId)){
//                throw new TaskMsgException("taskids中不能存在空值");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));
            }

            ThreadMsgEntity entity=getThreadMsgEntity(taskId);

            if(!entity.getStatus().equals(ThreadStatusEnum.RUN)){
//                throw new TaskMsgException("任务：【"+taskId+"】不在运行中");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUN_ERROR_CODE,"任务：【"+taskId+"】不在运行中"));

            }
        }

        for (String taskId:taskids
        ) {
            if(!StringUtils.isEmpty(taskId)){
                ThreadMsgEntity entity=getThreadMsgEntity(taskId);
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
                        entity.setStatus(ThreadStatusEnum.STOP);
                        aliveThreadHashMap.put(taskId,entity);
                        taskMap.put(taskId,"Task stopped successfully");
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


    /**
     * 异常停止在运行(RUN)中的任务线程
     * @param taskids
     * @return
     */
    public synchronized  static  Map<String,String> brokenCreateThread(List<String> taskids) throws TaskMsgException {
        Map<String,String>taskMap=new HashMap<>();

        for (String taskId:taskids
        ) {

            if(!aliveThreadHashMap.containsKey(taskId)){
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
                ThreadMsgEntity entity=getThreadMsgEntity(taskId);
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
                        aliveThreadHashMap.put(taskId,entity);
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

    /**
     * 返回各种类型的的任务线程
     * @param listTaskMsgDto
     * @return
     */
    public synchronized  static  List<ThreadReturnMsgEntity> listCreateThread(ListTaskMsgDto listTaskMsgDto) throws TaskMsgException {
        checklistTaskMsgDto(listTaskMsgDto);

        List<ThreadReturnMsgEntity>taskList=new ArrayList<>();


        if(listTaskMsgDto.getRegulation().trim().equals("bynames")){
            if(null!=listTaskMsgDto.getTasknames()&&listTaskMsgDto.getTasknames().size()>0){
                for (String name:listTaskMsgDto.getTasknames()
                ) {
                    if(StringUtils.isEmpty(name)){
                        throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKNAME_RUNING_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKNAME_RUNING_ERROR));
//                        throw new TaskMsgException("tasknames 不能有为空的参数");
                    }
                }

                for (String name:listTaskMsgDto.getTasknames()
                     ) {
                    for(Map.Entry<String,ThreadMsgEntity>thre:aliveThreadHashMap.entrySet()){
                        if(thre.getValue().getTaskName().equals(name)){
                            ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                            BeanUtils.copyProperties(thre.getValue(),msgEntity);
                            BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                            taskList.add(msgEntity);
                        }
                    }
                }


            }else {
//                throw new TaskMsgException("bynames 参数类型错误");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKNAME_TYPE_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKNAME_TYPE_ERROR));
            }
        }else if(listTaskMsgDto.getRegulation().trim().equals("all")){
            for(Map.Entry<String,ThreadMsgEntity>thre:aliveThreadHashMap.entrySet()){
                ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                BeanUtils.copyProperties(thre.getValue(),msgEntity);
                BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                taskList.add(msgEntity);
            }
        }else if(listTaskMsgDto.getRegulation().trim().equals("byids")){

            for (String id:listTaskMsgDto.getTaskids()
            ) {
                if(StringUtils.isEmpty(id)){
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));

//                    throw new TaskMsgException("taskids 不能有为空的参数");
                }
            }

            for (String id:listTaskMsgDto.getTaskids()
            ) {

                for(Map.Entry<String,ThreadMsgEntity>thre:aliveThreadHashMap.entrySet()){
                    if(thre.getValue().getId().equals(id)){
                        ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                        BeanUtils.copyProperties(thre.getValue(),msgEntity);
                        BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                        taskList.add(msgEntity);
                    }
                }
            }
        }else if(listTaskMsgDto.getRegulation().trim().equals("bystatus")){

            if(StringUtils.isEmpty(listTaskMsgDto.getTaskstatus())){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKSTATUS_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKSTATUS_NULL_ERROR));

//                throw new TaskMsgException("taskstatus 不能有为空");
            }
            ThreadStatusEnum statusEnum=null;
            if(listTaskMsgDto.getTaskstatus().equals("live")){
                statusEnum=ThreadStatusEnum.RUN;
            }else if(listTaskMsgDto.getTaskstatus().equals("stop")){
                statusEnum=ThreadStatusEnum.STOP;
            }else if(listTaskMsgDto.getTaskstatus().equals("broken")){
                statusEnum=ThreadStatusEnum.BROKEN;
            }



                for(Map.Entry<String,ThreadMsgEntity>thre:aliveThreadHashMap.entrySet()){
                    if(thre.getValue().getStatus().equals(statusEnum)){
                        ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                        BeanUtils.copyProperties(thre.getValue(),msgEntity);
                        BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                        taskList.add(msgEntity);
                    }
                    if(statusEnum.equals(ThreadStatusEnum.CREATE)&&listTaskMsgDto.getTaskstatus().equals("stop")){
                        ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                        BeanUtils.copyProperties(thre.getValue(),msgEntity);
                        BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                        taskList.add(msgEntity);
                    }
                }
        }
        return taskList;
    }


    /**
     * 检查TaskMsgDto
     * @param listTaskMsgDto
     */
    public static void checklistTaskMsgDto(ListTaskMsgDto listTaskMsgDto) throws TaskMsgException {
        //all、bynames、byids、bystatus
        if(!listTaskMsgDto.getRegulation().trim().equals("all")
                &&!listTaskMsgDto.getRegulation().trim().equals("bynames")
                && !listTaskMsgDto.getRegulation().trim().equals("byids")
                && !listTaskMsgDto.getRegulation().trim().equals("bystatus")){
//            throw new TaskMsgException("regulation 参数类型错误");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_REGULATION_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_REGULATION_ERROR));
        }
    }




    /**
     * 删除不在运行(RUN)中的任务线程
     * @param taskids
     * @return
     */
    public synchronized  static  Map<String,String> delCreateThread(List<String> taskids) throws TaskMsgException {
        Map<String,String>taskMap=new HashMap<>();

        for (String taskId:taskids
        ) {
            if(StringUtils.isEmpty(taskId)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));
//                throw new TaskMsgException("taskids中不能存在空值");
            }

            if(aliveThreadHashMap.containsKey(taskId)){
                if(aliveThreadHashMap.get(taskId).getStatus().equals(ThreadStatusEnum.RUN)){
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUN_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_RUN_ERROR));
                    //throw new TaskMsgException("请先停止taskids中处于运行状态的任务");
                }
            }else{
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_EXIST_ERROR_CODE,"不存在任务id为："+taskId+"的任务"));
//                throw new TaskMsgException("不存在任务id为："+taskId+"的任务");
            }
        }



        for (String taskId:taskids
        ) {

            if(!StringUtils.isEmpty(taskId)){
                aliveThreadHashMap.remove(taskId);
                taskMap.put(taskId,"success");
            }
        }

        return taskMap;
    }


    /**
     * 检测任务是否不处于run状态
     * @return
     */
    public static synchronized boolean doThreadisCloseCheckTask(String taskId) {
        /**
         * 当aliveMap中不存在此线程时关闭
         */

        ThreadMsgEntity entity=getAliveThreadHashMap().get(taskId);
        if(null==entity)
            return true;

        if(!entity.getStatus().equals(ThreadStatusEnum.RUN)){
            return true;
        }
        return false;
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
}
