package syncer.syncerplusservice.util;

import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.dto.task.ListTaskMsgDto;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.entity.thread.ThreadReturnMsgEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerplusservice.service.IRedisReplicatorService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncTaskUtils {
    /**
     * 启动在非运行(RUN)中的任务线程
     * @param taskids
     * @return
     */
    public synchronized  static Map<String,String> startCreateThread(List<String> taskids, IRedisReplicatorService redisBatchedReplicatorService) throws TaskMsgException {
        Map<String,String>taskMap=new HashMap<>();


        for (String taskId:taskids
        ) {

            if(!TaskMsgUtils.getAliveThreadHashMap().containsKey(taskId)){
//                throw new TaskMsgException("taskid为【"+taskId+"】的任务还未创建");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUN_ERROR_CODE,"taskid为【"+taskId+"】的任务还未创建"));
            }

            if(StringUtils.isEmpty(taskId)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));
//                throw new TaskMsgException("taskids中不能存在空值");
            }
            ThreadMsgEntity entity=TaskMsgUtils.getThreadMsgEntity(taskId);
            if(entity.getStatus().equals(ThreadStatusEnum.RUN)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUNING_ERROR_CODE,"任务：【"+taskId+"】已经在运行中"));
//                throw new TaskMsgException("任务：【"+taskId+"】已经在运行中");
            }

        }

        for (String taskId:taskids
        ) {
            if(!StringUtils.isEmpty(taskId)){
                ThreadMsgEntity entity=TaskMsgUtils.getThreadMsgEntity(taskId);

                if(null!=entity){
//                    redisBatchedReplicatorService.batchedSync(entity.getRedisClusterDto(),taskId);
                    entity.setStatus(ThreadStatusEnum.RUN);
                    TaskMsgUtils.getAliveThreadHashMap().put(taskId,entity);
                    taskMap.put(taskId,"Task started successfully");
                }else {
                    taskMap.put(taskId,"The task does not exist. Please create the task first");
                }
            }
        }

        return taskMap;
    }

    public synchronized  static  Map<String,String> startCreateThread(String taskId, boolean afresh, IRedisReplicatorService redisBatchedReplicatorService) throws TaskMsgException {
        Map<String,String>taskMap=new HashMap<>();

        if(StringUtils.isEmpty(taskId)){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));
//                throw new TaskMsgException("taskids中不能存在空值");
        }

        if(!TaskMsgUtils.getAliveThreadHashMap().containsKey(taskId)){
//                throw new TaskMsgException("taskid为【"+taskId+"】的任务还未创建");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUN_ERROR_CODE,"taskid为【"+taskId+"】的任务还未创建"));
        }


        ThreadMsgEntity entity=TaskMsgUtils.getThreadMsgEntity(taskId);
        if(entity.getStatus().equals(ThreadStatusEnum.RUN)){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUNING_ERROR_CODE,"任务：【"+taskId+"】已经在运行中"));
//                throw new TaskMsgException("任务：【"+taskId+"】已经在运行中");
        }



        if(!StringUtils.isEmpty(taskId)){

            if(null!=entity){
                if(entity.getRedisClusterDto().getFileType().equals(FileType.ONLINERDB)||entity.getRedisClusterDto().getFileType().equals(FileType.ONLINEAOF)
                ||entity.getRedisClusterDto().getFileType().equals(FileType.AOF)||entity.getRedisClusterDto().getFileType().equals(FileType.RDB)){
                    redisBatchedReplicatorService.filebatchedSync(entity.getRedisClusterDto(),taskId);
                }else {
                    redisBatchedReplicatorService.batchedSync(entity.getRedisClusterDto(),taskId,afresh);
                }

                entity.setStatus(ThreadStatusEnum.RUN);
                TaskMsgUtils.getAliveThreadHashMap().put(taskId,entity);
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

            if(!TaskMsgUtils.getAliveThreadHashMap().containsKey(taskId)){
//                throw new TaskMsgException("taskid为【"+taskId+"】的任务还未创建");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_UNCREATE_ERROR_CODE,"taskid为【"+taskId+"】的任务还未创建"));
            }

            if(StringUtils.isEmpty(taskId)){
//                throw new TaskMsgException("taskids中不能存在空值");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_NULL_ERROR));
            }

            ThreadMsgEntity entity=TaskMsgUtils.getThreadMsgEntity(taskId);

            if(!entity.getStatus().equals(ThreadStatusEnum.RUN)){
//                throw new TaskMsgException("任务：【"+taskId+"】不在运行中");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_RUN_ERROR_CODE,"任务：【"+taskId+"】不在运行中"));

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
                        entity.setStatus(ThreadStatusEnum.STOP);
                        TaskMsgUtils.getAliveThreadHashMap().put(taskId,entity);
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


    public synchronized  static  Map<String,String> editTaskMsg(String taskId,String msg)  {
        try {
            Map<String,String>taskMap=new HashMap<>();


            if(StringUtils.isEmpty(taskId)||!TaskMsgUtils.getAliveThreadHashMap().containsKey(taskId))
                return null;

            ThreadMsgEntity entity=TaskMsgUtils.getThreadMsgEntity(taskId);

            if(null!=entity)
                //运行中
                entity.setTaskMsg(msg);

            return taskMap;
        }catch (Exception e){

        }
        return null;
    }



    /**
     * 异常停止在运行(RUN)中的任务线程
     * @param taskids
     * @return
     */

    public synchronized  static  Map<String,String> brokenCreateThread(List<String> taskids) throws TaskMsgException {
        return brokenCreateThread(taskids,"");
    }

    public synchronized  static  Map<String,String> brokenCreateThread(List<String> taskids,String msg) throws TaskMsgException {
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
                        if(StringUtils.isEmpty(msg)){
                            msg="";
                        }
                        if(msg.trim().indexOf("expect [$,:,*,+,-] but: R")>=0){
                            msg="数据文件格式(RDB/AOF/MIXED)与所填写的FileType不匹配";
                        }

                        entity.setTaskMsg(msg);
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
                    for(Map.Entry<String,ThreadMsgEntity>thre:TaskMsgUtils.getAliveThreadHashMap().entrySet()){
                        if(thre.getValue().getTaskName().equals(name)){
                            ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                            BeanUtils.copyProperties(thre.getValue(),msgEntity);
                            BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                            msgEntity.setTargetRedisVersion(thre.getValue().getRedisClusterDto().getTargetRedisVersion());
                            msgEntity.loading();
                            taskList.add(msgEntity);
                        }
                    }
                }


            }else {
//                throw new TaskMsgException("bynames 参数类型错误");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKNAME_TYPE_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKNAME_TYPE_ERROR));
            }
        }else if(listTaskMsgDto.getRegulation().trim().equals("all")){
            for(Map.Entry<String,ThreadMsgEntity>thre:TaskMsgUtils.getAliveThreadHashMap().entrySet()){
                ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                BeanUtils.copyProperties(thre.getValue(),msgEntity);
                BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                msgEntity.loading();
                msgEntity.setTargetRedisVersion(thre.getValue().getRedisClusterDto().getTargetRedisVersion());
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

                for(Map.Entry<String,ThreadMsgEntity>thre:TaskMsgUtils.getAliveThreadHashMap().entrySet()){
                    if(thre.getValue().getId().equals(id)){
                        ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                        BeanUtils.copyProperties(thre.getValue(),msgEntity);
                        BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                        msgEntity.loading();
                        msgEntity.setTargetRedisVersion(thre.getValue().getRedisClusterDto().getTargetRedisVersion());
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



            for(Map.Entry<String,ThreadMsgEntity>thre:TaskMsgUtils.getAliveThreadHashMap().entrySet()){
                if(thre.getValue().getStatus().equals(statusEnum)){
                    ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                    BeanUtils.copyProperties(thre.getValue(),msgEntity);
                    BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                    msgEntity.loading();
                    msgEntity.setTargetRedisVersion(thre.getValue().getRedisClusterDto().getTargetRedisVersion());
                    taskList.add(msgEntity);
                }
                if(statusEnum.equals(ThreadStatusEnum.CREATE)&&listTaskMsgDto.getTaskstatus().equals("stop")){
                    ThreadReturnMsgEntity msgEntity=ThreadReturnMsgEntity.builder().build();
                    BeanUtils.copyProperties(thre.getValue(),msgEntity);
                    BeanUtils.copyProperties(thre.getValue().getRedisClusterDto(),msgEntity);
                    msgEntity.loading();
                    msgEntity.setTargetRedisVersion(thre.getValue().getRedisClusterDto().getTargetRedisVersion());
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

            if(TaskMsgUtils.getAliveThreadHashMap().containsKey(taskId)){
                if(TaskMsgUtils.getAliveThreadHashMap().get(taskId).getStatus().equals(ThreadStatusEnum.RUN)){
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
                TaskMsgUtils.getAliveThreadHashMap().remove(taskId);
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

        ThreadMsgEntity entity=TaskMsgUtils.getAliveThreadHashMap().get(taskId);
        if(null==entity)
            return true;

        if(!entity.getStatus().equals(ThreadStatusEnum.RUN)){
            return true;
        }
        return false;
    }


}
