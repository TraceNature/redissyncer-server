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

package syncer.transmission.util.manger.impl;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.common.exception.TaskMsgException;
import syncer.replica.status.Status;
import syncer.replica.status.TaskStatus;
import syncer.transmission.constants.TaskMsgConstant;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.lock.EtcdLockCommandRunner;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.code.CodeUtils;
import syncer.transmission.util.lock.TaskRunUtils;
import syncer.transmission.util.manger.ITaskStatusManger;
import syncer.transmission.util.sql.SqlOPUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhanenqiang
 * @Description 单项同步任务内存状态管理(内存+库)
 * @Date 2020/12/15
 */
@Slf4j
public class MemoryAndSqliteTaskStatusManger implements ITaskStatusManger {

    @Getter
    @Setter
    private static Map<String, TaskDataEntity> aliveThreadHashMap=new ConcurrentHashMap<String,TaskDataEntity>();
    @Getter
    public static final Map<String, Integer> rdbVersionMap=new ConcurrentHashMap<String,Integer>();


    @Override
    public void addDbThread(String taskId, TaskModel taskModel) throws Exception {
        if(checkTaskMsg(taskId)){
            boolean status= SqlOPUtils.insertTask(taskModel);
            if(!status){
                throw new Exception("任务基本信息持久化失败");
            }
        }
    }


    @Override
    public void addDbThread(String taskId, TaskDataEntity taskDataEntity) throws Exception {
        if(checkTaskMsg(taskId)){
            boolean status= SqlOPUtils.insertTask(taskDataEntity.getTaskModel());
            if(!status){
                throw new Exception("任务基本信息持久化失败");
            }
        }
    }

    /**
     * 只往内存中添加数据
     * @param taskId
     * @param taskDataEntity
     * @param status
     * @throws Exception
     */
    @Override
    public void addMemoryDbThread(String taskId, TaskDataEntity taskDataEntity, boolean status) throws Exception {
        if(checkTaskMsg(taskDataEntity)&&!status){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR));
        }

        if(status){
            aliveThreadHashMap.put(taskId,taskDataEntity);
        }else {
            if(!aliveThreadHashMap.containsKey(taskId)){
                aliveThreadHashMap.put(taskId,taskDataEntity);
            }
        }
    }

    @Override
    public void addMemoryDbThread(String taskId, TaskDataEntity taskDataEntity) throws Exception {
        if(checkTaskMsg(taskDataEntity)){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR));
        }
        if(!aliveThreadHashMap.containsKey(taskId)){
            aliveThreadHashMap.put(taskId,taskDataEntity);
        }
    }

    /**
     * 修改任务状态
     * @param taskId
     * @param offset
     * @param taskType
     * @throws Exception
     */
    @Override
    public void changeThreadStatus(String taskId, Long offset, TaskStatus taskType) throws Exception {
            if(aliveThreadHashMap.containsKey(taskId)){
                TaskRunUtils.getTaskLockE("changeStatus_" + taskId, new EtcdLockCommandRunner() {
                    @Override
                    public void run() throws Exception {
                        TaskDataEntity taskDataEntity=aliveThreadHashMap.get(taskId);
                        SqlOPUtils.updateTaskStatusById(taskId, taskType.getCode());
                        if(offset!=null&&offset>=-1L){
                            SqlOPUtils.updateTaskOffsetById(taskId,offset);
                        }

                        SqlOPUtils.updateKeyCountById(taskId,taskDataEntity.getRdbKeyCount().get(),taskDataEntity.getAllKeyCount().get(),taskDataEntity.getRealKeyCount().get());
                        updateExpandTaskModel(taskId);
                        if(taskType.getStatus().equals(Status.BROKEN)||taskType.getStatus().equals(Status.STOP)||taskType.getStatus().equals(Status.FINISH)){
                            aliveThreadHashMap.remove(taskId);
                            deleteTaskDataByTaskId(taskId);
                            taskDataEntity=null;
                        }else {
                            taskDataEntity.getTaskModel().setStatus(taskType.getCode());
                        }
                    }

                    @Override
                    public String lockName() {
                        return "changeStatus_" + taskId;
                    }

                    @Override
                    public int grant() {
                        return 30;
                    }
                });


            }
    }

    @Override
    public void updateThreadStatus(String taskId, TaskStatus taskStatusType) throws Exception {
        if(aliveThreadHashMap.containsKey(taskId)){

            TaskRunUtils.getTaskLockE("changeStatus_" + taskId, new EtcdLockCommandRunner() {
                @Override
                public void run() throws Exception {
                    TaskDataEntity data=aliveThreadHashMap.get(taskId);
                    SqlOPUtils.updateTaskStatusById(taskId, taskStatusType.getCode());
                    SqlOPUtils.updateKeyCountById(taskId,data.getRdbKeyCount().get(),data.getAllKeyCount().get(),data.getRealKeyCount().get());
                    updateExpandTaskModel(taskId);
                    data.getTaskModel().setStatus(taskStatusType.getCode());
                    if(taskStatusType.getStatus().equals(Status.BROKEN)
                            ||taskStatusType.getStatus().equals(Status.STOP)||taskStatusType.getStatus().equals(Status.FINISH)){
                        updateTaskOffset(taskId);
                        aliveThreadHashMap.remove(taskId);
                    }
                }

                @Override
                public String lockName() {
                    return "changeStatus_" + taskId;
                }

                @Override
                public int grant() {
                    return 30;
                }
            });


        }
    }

    /**
     * 宕掉任务
     * @param taskId
     * @throws Exception
     */

    @Override
    public void brokenTask(String taskId) throws Exception {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity data=aliveThreadHashMap.get(taskId);
            SqlOPUtils.updateKeyCountById(taskId,data.getRdbKeyCount().get(),data.getAllKeyCount().get(),data.getRealKeyCount().get());
            SqlOPUtils.updateTaskStatusById(taskId, TaskStatus.BROKEN.getCode());
            updateExpandTaskModel(taskId);
            if(data.getOffSetEntity().getReplOffset()!=null&&data.getOffSetEntity().getReplOffset().get()>-1){
                SqlOPUtils.updateTaskOffsetById(taskId,data.getOffSetEntity().getReplOffset().get());
            }
            aliveThreadHashMap.remove(taskId);
        }
    }

    @Override
    public void brokenStatusAndLog(Exception e, Class clazz, String taskId) {
        brokenStatusAndLog(e.getMessage(),clazz,taskId);
        e.printStackTrace();
    }

    @Override
    public void brokenStatusAndLog(String exceptionMsg, Class clazz, String taskId) {
        try {
            //清除内存信息并更新数据库状态
            updateBrokenResult(taskId,exceptionMsg);
            brokenTask(taskId);
        } catch (Exception ex) {
            log.warn("任务Id【{}】任务启动/运行异常停止 ，Class【{}】,异常原因【{}】", taskId, clazz.toString(),exceptionMsg);
            ex.printStackTrace();
        }
        log.warn("任务Id【{}】任务启动/运行异常停止 ，Class【{}】,异常原因【{}】", taskId, clazz.toString(),exceptionMsg);
    }

    /**
     * 更新BrokenResult
     * @param taskId
     * @param brokenResult
     */
    @Override
    public void updateBrokenResult(String taskId, String brokenResult) {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity dataEntity=aliveThreadHashMap.get(taskId);
            if(null!=dataEntity.getExpandTaskModel()){
                dataEntity.getTaskModel().setExpandJson(JSON.toJSONString(dataEntity.getExpandTaskModel()));
                dataEntity.getExpandTaskModel().setBrokenReason(brokenResult);
                updateExpandTaskModel(taskId);
            }
        }
    }

    /**
     * 将内存相关字段入库
     * @param taskId
     */
    @Override
    public void updateExpandTaskModel(String taskId) {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity dataEntity=aliveThreadHashMap.get(taskId);
            try {
                SqlOPUtils.updateExpandTaskModelById(taskId, JSON.toJSONString(dataEntity.getExpandTaskModel()));
            } catch (Exception e) {
                log.error("[{}]更新扩展字段失败{}",taskId,e.getMessage());
            }
        }
    }

    @Override
    public void updateThreadStatusAndMsg(String taskId, String msg, TaskStatus taskStatusType) throws Exception {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity data=aliveThreadHashMap.get(taskId);
            data.getTaskModel().setStatus(taskStatusType.getCode());
            data.getTaskModel().setTaskMsg(msg);
            SqlOPUtils.updateTaskMsgAndStatusById( taskStatusType.getCode(),msg,taskId);
            if(taskStatusType.equals(TaskStatus.BROKEN)||taskStatusType.equals(TaskStatus.STOP)||taskStatusType.equals(TaskStatus.FINISH)){
                updateTaskOffset(taskId);
                if(taskStatusType.equals(TaskStatus.BROKEN)){
                    updateBrokenResult(taskId,msg);
                }else {
                    data.getTaskModel().setTaskMsg("任务主动关闭或停止");
                    updateBrokenResult(taskId,"");
                }
                aliveThreadHashMap.remove(taskId);
            }
        }
    }

    @Override
    public void updateThreadMsg(String taskId, String msg) throws Exception {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity data=aliveThreadHashMap.get(taskId);
            data.getTaskModel().setTaskMsg(msg);
            SqlOPUtils.updateTaskMsgById(msg,taskId);
        }else {
            SqlOPUtils.updateTaskMsgById(msg,taskId);
        }
    }


    /**
     * 判断任务是否关闭
     * @param taskId
     * @return
     */
    @Override
    public boolean isTaskClose(String taskId) {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskModel taskModel=aliveThreadHashMap.get(taskId).getTaskModel();
            if(taskModel.getStatus().equals(TaskStatus.BROKEN.getCode())||taskModel.getStatus().equals(TaskStatus.STOP.getCode())||taskModel.getStatus().equals(TaskStatus.FINISH.getCode())){
                return true;
            }
            return false;
        }
        return true;
    }


    /**
     * 检查信息
     * @param threadMsgEntity
     * @return
     */
    public  boolean checkTaskMsg(TaskDataEntity threadMsgEntity){
        AtomicBoolean status= new AtomicBoolean(false);
        aliveThreadHashMap.entrySet().forEach(alive->{
            if(alive.getValue().getTaskModel().getId().equals(threadMsgEntity.getTaskModel().getId())){
                status.set(true);
                return;
            }
        });
        return status.get();
    }

    public boolean checkTaskMsg(String taskId){
        AtomicBoolean status= new AtomicBoolean(false);
        try {
            TaskModel taskModel= SqlOPUtils.findTaskById(taskId);
            if(taskModel==null){
                status.set(true);
            }
        } catch (Exception e) {
            status.set(true);
        }
        return status.get();
    }


    void deleteTaskDataByTaskId(String taskId){
        try {
            SqlOPUtils.deleteAbandonCommandModelByTaskId(taskId);
            SqlOPUtils.deleteBigKeyCommandModelByTaskId(taskId);
            SqlOPUtils.deleteBigKeyCommandModelByTaskId(taskId);
            SqlOPUtils.delOffsetEntityByTaskId(taskId);
            SqlOPUtils.deleteDataCompensationModelByTaskId(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 更新offset
     * @param taskId
     * @throws Exception
     */
    public void updateTaskOffset(String taskId)throws Exception{
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity data=aliveThreadHashMap.get(taskId);
            if(data.getOffSetEntity().getReplOffset()!=null&&data.getOffSetEntity().getReplOffset().get()>-1L){
                SqlOPUtils.updateTaskOffsetById(taskId,data.getOffSetEntity().getReplOffset().get());
            }
        }

    }
}
