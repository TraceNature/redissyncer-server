package syncer.syncerservice.service.impl2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syncer.syncerpluscommon.constant.ResultCodeAndMessage;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.StartTaskEntity;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;
import syncer.syncerplusredis.entity.thread.OffSetEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.ExpandTaskModel;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.service.IRedisTaskService;
import syncer.syncerservice.service.ISyncerService;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerpluscommon.util.TaskCreateRunUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/14
 */
@Service("taskGroupService")
@Slf4j
public class TaskGroupServiceImpl implements ISyncerService {

    @Autowired
    RedisPoolProps redisPoolProps;



    /**
     * 单机Redis-->单机Redis数据服务
     */
    @Autowired
    IRedisTaskService singleRedisService;




    @Override
    public ResultMap createCommandDumpUptask(List<TaskModel> taskModelList) throws TaskMsgException {
        List<StartTaskEntity>resultList=new ArrayList<>();
        String groupId=null;
        if(taskModelList.size()==1){
            groupId = taskModelList.get(0).getId();
        }else {
            groupId = TemplateUtils.uuid();
        }

        if(taskModelList!=null&&taskModelList.size()>0){
            for (TaskModel taskModel : taskModelList) {
                Lock lock=  TaskCreateRunUtils.getTaskLock(taskModel.getId());
                lock.lock();
                try {
                    taskModel.setGroupId(groupId);
                    taskModel.setStatus(TaskStatusType.STOP.getCode());
                    TaskDataManagerUtils.addDbThread(taskModel.getId(),taskModel);
                    if(taskModel.isAutostart()){
                        TaskModel testTaskModel=new TaskModel();
                        BeanUtils.copyProperties(taskModel,testTaskModel);
                        testTaskModel.setStatus(TaskStatusType.CREATING.getCode());
                        TaskDataEntity  dataEntity=TaskDataEntity.builder()
                                .taskModel(testTaskModel)
                                .offSetEntity(OffSetEntity.builder().replId("").build())
                                .build();


                        TaskDataManagerUtils.addMemThread(taskModel.getId(),dataEntity);

                        String id=singleRedisService.createCommandSyncerTask(taskModel);

                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("2000")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("Task created successfully and entered running state")
                                .build();
                        resultList.add(startTaskEntity);
                    }else {
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("2000")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("Task created successfully")
                                .build();
                        resultList.add(startTaskEntity);
                        TaskDataManagerUtils.updateThreadStatus(taskModel.getId(), TaskStatusType.STOP);
                    }


                } catch (Exception ex) {

                    try {
                        TaskDataManagerUtils.removeThread(taskModel.getId());
                    } catch (Exception ep) {

                        ep.printStackTrace();
                    }
                    ex.printStackTrace();

                    log.error("taskId[{}],error[{}]",taskModel.getId(),ex.getMessage());
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1000")
                            .taskId(taskModel.getId())
                            .groupId(taskModel.getGroupId())
                            .msg("Error_"+ex.getMessage())
                            .build();
                    resultList.add(startTaskEntity);
                }finally {
                    lock.unlock();
                }
            }
        }
        return ResultMap.builder().code("2000").data(resultList).msg("The request is successful");
    }

    @Override
    public ResultMap createRedisToRedisTask(List<TaskModel> taskModelList) throws TaskMsgException {
        List<StartTaskEntity>resultList=new ArrayList<>();
        String groupId=null;
        if(taskModelList.size()==1){
            groupId = taskModelList.get(0).getId();
        }else {
            groupId = TemplateUtils.uuid();
        }

        if(taskModelList!=null&&taskModelList.size()>0){
            for (TaskModel taskModel : taskModelList) {
                Lock lock=  TaskCreateRunUtils.getTaskLock(taskModel.getId());
                lock.lock();
                try {
                    taskModel.setGroupId(groupId);
                    taskModel.setStatus(TaskStatusType.STOP.getCode());
                    TaskDataManagerUtils.addDbThread(taskModel.getId(),taskModel);

                    if(taskModel.isAutostart()){

                        TaskModel testTaskModel=new TaskModel();
                        BeanUtils.copyProperties(taskModel,testTaskModel);
                        testTaskModel.setStatus(TaskStatusType.CREATING.getCode());
                        TaskDataEntity  dataEntity=TaskDataEntity.builder()
                                .taskModel(testTaskModel)
                                .offSetEntity(OffSetEntity.builder().replId("").build())
                                .build();
                        dataEntity.getOffSetEntity().getReplOffset().set(-1L);
                        TaskDataManagerUtils.addMemThread(taskModel.getId(),dataEntity);


                        String id=singleRedisService.runSyncerTask(taskModel);


                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("2000")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("Task created successfully and entered running state")
                                .build();
                        resultList.add(startTaskEntity);

                    }else {
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("2000")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("Task created successfully")
                                .build();
                        resultList.add(startTaskEntity);
                        TaskDataManagerUtils.updateThreadStatus(taskModel.getId(), TaskStatusType.STOP);
                    }


                } catch (Exception e) {
                    try {
                        TaskDataManagerUtils.removeThread(taskModel.getId());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    log.error("taskId[{}],error[{}]",taskModel.getId(),e.getMessage());
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1000")
                            .taskId(taskModel.getId())
                            .groupId(taskModel.getGroupId())
                            .msg("Error_"+e.getMessage())
                            .build();
                    resultList.add(startTaskEntity);
                }finally {
                    lock.unlock();
                }
            }
        }
        return ResultMap.builder().code("2000").data(resultList).msg("The request is successful");
    }

    @Override
    public ResultMap startSyncerTask(List<TaskStartMsgDto> taskStartMsgDtoList) throws Exception{

        List<StartTaskEntity>resultList=new ArrayList<>();
        for (TaskStartMsgDto taskStartDto:
                taskStartMsgDtoList) {

            Lock lock=  TaskCreateRunUtils.getTaskLock(taskStartDto.getTaskid());
            lock.lock();
            try {
                if(!TaskDataManagerUtils.isTaskClose(taskStartDto.getTaskid())){
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1001")
                            .taskId(taskStartDto.getTaskid())
                            .msg("The task is running")
                            .build();
                    resultList.add(startTaskEntity);
                    continue;
                }

                TaskModel taskModel= SqliteOPUtils.findTaskById(taskStartDto.getTaskid());
                taskModel.setTaskMsg("");
                    /**
                     * todo offset更新
                     */
                taskModel.setAfresh(taskStartDto.isAfresh());

                if(null==taskModel){
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1002")
                            .taskId(taskStartDto.getTaskid())
                            .msg("The task has not been created yet")
                            .build();
                    resultList.add(startTaskEntity);
                    continue;
                }


                SqliteOPUtils.updateAfreshsetById(taskStartDto.getTaskid(),taskStartDto.isAfresh());
                String id=null;
                if(!TaskDataManagerUtils.isTaskClose(taskStartDto.getTaskid())){
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1001")
                            .taskId(taskStartDto.getTaskid())
                            .msg("The task is running")
                            .build();
                    resultList.add(startTaskEntity);
                    continue;
                }
                if(taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())){
                     id=singleRedisService.createCommandSyncerTask(taskModel);
                }else {
                     id=singleRedisService.runSyncerTask(taskModel);
                }


                StartTaskEntity startTaskEntity=StartTaskEntity
                        .builder()
                        .code("2000")
                        .taskId(id)
                        .msg("OK")
                        .build();
                resultList.add(startTaskEntity);
            } catch (Exception e) {
                StartTaskEntity startTaskEntity=StartTaskEntity
                        .builder()
                        .code("1000")
                        .taskId(taskStartDto.getTaskid())
                        .msg("Error_"+e.getMessage())
                        .build();
                resultList.add(startTaskEntity);
            }finally {
                lock.unlock();
            }

        }

        return ResultMap.builder().data(resultList);
    }

    @Override
    public ResultMap startSyncerTaskByGroupId(String groupId,boolean afresh) throws Exception {

        List<TaskModel>taskModelList=SqliteOPUtils.findTaskByGroupId(groupId);
        if(taskModelList==null){
            return ResultMap.builder().code("1004").msg("GroupId不存在");
        }
        List<StartTaskEntity>resultList=new ArrayList<>();
        for (TaskModel taskModel : taskModelList) {
            Lock lock=  TaskCreateRunUtils.getTaskLock(taskModel.getId());
            lock.lock();
            try {
                if(!TaskDataManagerUtils.isTaskClose(taskModel.getId())){
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1001")
                            .taskId(taskModel.getId())
                            .msg("The task is running")
                            .build();
                    resultList.add(startTaskEntity);
                    continue;
                }


                if(afresh!=taskModel.isAfresh()){
                    try {
                        SqliteOPUtils.updateAfreshsetById(taskModel.getId(),afresh);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())){
                    String id=singleRedisService.createCommandSyncerTask(taskModel);
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("2000")
                            .taskId(id)
                            .msg("OK")
                            .build();
                    resultList.add(startTaskEntity);

                }else {
                    String id=singleRedisService.runSyncerTask(taskModel);
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("2000")
                            .taskId(id)
                            .msg("OK")
                            .build();
                    resultList.add(startTaskEntity);
                }


            } catch (Exception e) {
                StartTaskEntity startTaskEntity=StartTaskEntity
                        .builder()
                        .code("1000")
                        .taskId(taskModel.getId())
                        .msg("Error_"+e.getMessage())
                        .build();
                resultList.add(startTaskEntity);
            }finally {
                lock.unlock();
            }

        }

        return ResultMap.builder().data(resultList);
    }




    @Override
    public ResultMap editSyncerTaskByTaskId(TaskModel taskModel) throws Exception {
        Lock lock=  TaskCreateRunUtils.getTaskLock(taskModel.getId());
        lock.lock();
        try {

            if(StringUtils.isEmpty(taskModel.getId())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASK_ID_ERROR.getCode(),ResultCodeAndMessage.TASK_MSG_TASK_ID_ERROR.getMsg()));
            }
            TaskDataEntity memTaskModel=TaskDataManagerUtils.get(taskModel.getId());
            if(memTaskModel!=null){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_EDIT_MSG_TASK_NOT_STOP_ERROR.getCode(),ResultCodeAndMessage.TASK_EDIT_MSG_TASK_NOT_STOP_ERROR.getMsg()));
            }

            TaskModel dbTaskModel=SqliteOPUtils.findTaskById(taskModel.getId());

            if(dbTaskModel==null){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASK_IS_NULL_ERROR.getCode(),ResultCodeAndMessage.TASK_MSG_TASK_IS_NULL_ERROR.getMsg()));
            }


            /**
             * TODO 修改任务信息时，将用户传入信息和原有信息进行数据合并
             */
            if(taskModel.getRedisVersion()!=0){
                dbTaskModel.setRedisVersion(taskModel.getRedisVersion());
            }

            if(!StringUtils.isEmpty(taskModel.getSourceRedisAddress())&&!dbTaskModel.getSourceRedisAddress().equals(taskModel.getSourceRedisAddress())){
                dbTaskModel.setSourceRedisAddress(taskModel.getSourceRedisAddress());
            }

            if(!StringUtils.isEmpty(taskModel.getTargetRedisAddress())&&!dbTaskModel.getTargetRedisAddress().equals(taskModel.getTargetRedisAddress())){
                dbTaskModel.setTargetRedisAddress(taskModel.getTargetRedisAddress());
            }

            if(!StringUtils.isEmpty(taskModel.getFileAddress())&&!dbTaskModel.getFileAddress().equals(taskModel.getFileAddress())){
                dbTaskModel.setFileAddress(taskModel.getFileAddress());
            }

            if(!StringUtils.isEmpty(taskModel.getDbMapper())&&!dbTaskModel.getDbMapper().equals(taskModel.getDbMapper())){
                dbTaskModel.setDbMapper(taskModel.getDbMapper());
            }

            if(!StringUtils.isEmpty(taskModel.getTaskName())&&!dbTaskModel.getTaskName().equals(taskModel.getTaskName())){
                dbTaskModel.setTaskName(taskModel.getTaskName());
            }

            if(!StringUtils.isEmpty(taskModel.getTargetUserName())&&!dbTaskModel.getTargetUserName().equals(taskModel.getTargetUserName())){
                dbTaskModel.setTargetUserName(taskModel.getTargetUserName());
            }
            if(!StringUtils.isEmpty(taskModel.getSourceUserName())&&!dbTaskModel.getSourceUserName().equals(taskModel.getSourceUserName())){
                dbTaskModel.setSourceUserName(taskModel.getSourceUserName());
            }

            if(!StringUtils.isEmpty(taskModel.getTargetPassword())&&!dbTaskModel.getTargetPassword().equals(taskModel.getTargetPassword())){
                dbTaskModel.setTargetPassword(taskModel.getTargetPassword());
            }

            if(!StringUtils.isEmpty(taskModel.getSourcePassword())&&!dbTaskModel.getSourcePassword().equals(taskModel.getSourcePassword())){
                dbTaskModel.setSourcePassword(taskModel.getSourcePassword());
            }

            if(taskModel.getBatchSize()!=0&&!dbTaskModel.getBatchSize().equals(taskModel.getBatchSize())){
                dbTaskModel.setBatchSize(taskModel.getBatchSize());
            }

            if(taskModel.getErrorCount()>=-1L&&!dbTaskModel.getErrorCount().equals(taskModel.getErrorCount())){
                dbTaskModel.setErrorCount(taskModel.getErrorCount());
            }
            dbTaskModel.setSyncType(taskModel.getSyncType());
            dbTaskModel.setAfresh(taskModel.isAfresh());
            dbTaskModel.setAutostart(taskModel.isAutostart());
            dbTaskModel.setTasktype(taskModel.getTasktype());
            dbTaskModel.setSourceAcl(taskModel.isSourceAcl());
            dbTaskModel.setTargetAcl(taskModel.isTargetAcl());



            if(SqliteOPUtils.updateTask(dbTaskModel)){
                return ResultMap.builder().code("2000").add("status","OK").msg("The request is successful");
            }
        }finally {
            lock.unlock();
        }


        return ResultMap.builder().code("1000").add("status","FAIL").msg("Operation failed");

    }



}
