package syncer.syncerplusredis.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.service.ITaskManageService;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/11
 */

@Slf4j
@Service("sqliteTaskManageService")
public class SqliteTaskManageServiceImpl implements ITaskManageService {

    @Autowired
    TaskMapper taskMapper;


    @Override
    public boolean createTaskService(TaskModel model) throws Exception {

        try {
            TaskModel dataModel=taskMapper.findTaskById(model.getId());
            if(dataModel==null){
                if(taskMapper.insertTask(model)){
                    return true;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            log.info("[{}]任务创建失败",model.getId());
        }

        return false;
    }

    @Override
    public boolean startTaskServiceById(String id, TaskStatusType statusType)throws Exception {
        TaskModel dataModel=taskMapper.findTaskById(id);
        //任务存在且未在运行中
        if(dataModel!=null
                && !dataModel.getStatus().equals(TaskStatusType.RUN.getCode())
        &&!dataModel.getStatus().equals(TaskStatusType.RDBRUNING.getCode())
        &&!dataModel.getStatus().equals(TaskStatusType.COMMANDRUNING.getCode())){
            taskMapper.updateTaskStatusById(id,TaskStatusType.RUN.getCode());

        }else {
//            throw new  Exception()
        }
        return false;
    }

    @Override
    public boolean startTaskServiceByGroupId(String groupId, TaskStatusType statusType)throws Exception {
        List<TaskModel> taskModelList = taskMapper.findTaskByGroupId(groupId);
        if (null != taskModelList) {
            for (TaskModel task : taskModelList) {
                if (null != task
                        && !task.getStatus().equals(TaskStatusType.RUN.getCode())
                        && !task.getStatus().equals(TaskStatusType.RDBRUNING.getCode())
                        && !task.getStatus().equals(TaskStatusType.COMMANDRUNING.getCode())) {
                    taskMapper.updateTaskStatusById(task.getId(), TaskStatusType.RUN.getCode());
                }
            }
            return true;
        }
        return  false;
    }

    @Override
    public boolean stopTaskServiceById(String id, TaskStatusType statusType) throws Exception {

        TaskModel dataModel=taskMapper.findTaskById(id);
        //任务存在且未在运行中
        if(dataModel!=null){
            if(dataModel.getStatus().equals(TaskStatusType.RUN.getCode())
                    &&dataModel.getStatus().equals(TaskStatusType.RDBRUNING.getCode())
                    &&dataModel.getStatus().equals(TaskStatusType.COMMANDRUNING.getCode())){

                taskMapper.updateTaskStatusById(id,TaskStatusType.STOP.getCode());
                //避免sqlite压力过大，同时更新内存状态
            }else {
                taskMapper.updateTaskStatusById(id,TaskStatusType.STOP.getCode());


            }
        }else {
            //throw new  Exception()

        }

        return false;

    }


    /**
     * 停止一组任务
     * @param groupId
     * @param statusType
     * @return
     * @throws Exception
     */
    @Override
    public boolean stopTaskServiceByGroupId(String groupId, TaskStatusType statusType) throws Exception {

        List<TaskModel> taskModelList = taskMapper.findTaskByGroupId(groupId);
        if (null != taskModelList) {
            for (TaskModel task : taskModelList) {

                if(null!=task){
                    if(task.getStatus().equals(TaskStatusType.RUN.getCode())
                            &&task.getStatus().equals(TaskStatusType.RDBRUNING.getCode())
                            &&task.getStatus().equals(TaskStatusType.COMMANDRUNING.getCode())){
                        taskMapper.updateTaskStatusById(task.getId(), TaskStatusType.RUN.getCode());
                    }
                }else{
                    continue;
                }

            }
            return true;
        }

        return false;
    }


    /**
     * 将任务异常退出
     * @param id
     * @param statusType
     * @return
     * @throws Exception
     */
    @Override
    public boolean brokenTaskServiceById(String id, TaskStatusType statusType) throws Exception {
        TaskModel dataModel=taskMapper.findTaskById(id);
        //任务存在且未在运行中

        if(null!=dataModel){
            return taskMapper.updateTaskStatusById(id,TaskStatusType.BROKEN.getCode());
        }


        return false;

    }


    /**
     * 将一组任务异常退出
     * @param groupId
     * @param statusType
     * @return
     * @throws Exception
     */
    @Override
    public boolean brokenTaskServiceByGroupId(String groupId, TaskStatusType statusType) throws Exception {
        List<TaskModel> taskModelList = taskMapper.findTaskByGroupId(groupId);
        if (null != taskModelList) {
            for (TaskModel task : taskModelList) {
                if(null!=task){
                     taskMapper.updateTaskStatusById(task.getId(),TaskStatusType.BROKEN.getCode());
                }
            }
            return true;
        }


        return false;
    }


}
