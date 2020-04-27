package syncer.syncerservice.service.impl2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.StartTaskEntity;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerservice.service.IRedisSyncerService;
import syncer.syncerservice.service.IRedisTaskService;
import syncer.syncerservice.service.ISyncerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/14
 */
@Service("taskGroupService")
public class TaskGroupServiceImpl implements ISyncerService {

    @Autowired
    RedisPoolProps redisPoolProps;
    @Autowired
    IRedisSyncerService redisBatchedSyncerService;


    /**
     * 单机Redis-->单机Redis数据服务
     */
    @Autowired
    IRedisTaskService singleRedisService;

    @Autowired
    TaskMapper taskMapper;



    @Override
    public ResultMap createCommandDumpUptask(List<TaskModel> taskModelList) throws TaskMsgException {
        return null;
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
                try {
                    taskModel.setGroupId(groupId);
                    TaskDataManagerUtils.addDbThread(taskModel.getId(),taskModel);
                    if(taskModel.isAutostart()){
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
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1000")
                            .taskId(taskModel.getId())
                            .groupId(taskModel.getGroupId())
                            .msg("Error_"+e.getMessage())
                            .build();
                    resultList.add(startTaskEntity);
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

            if(!TaskDataManagerUtils.isTaskClose(taskStartDto.getTaskid())){
                StartTaskEntity startTaskEntity=StartTaskEntity
                        .builder()
                        .code("1000")
                        .taskId(taskStartDto.getTaskid())
                        .msg("The task is running")
                        .build();
                resultList.add(startTaskEntity);
                continue;
            }

            TaskModel taskModel=taskMapper.findTaskById(taskStartDto.getTaskid());
            taskModel.setAfresh(taskStartDto.isAfresh());
            taskMapper.updateAfreshsetById(taskStartDto.getTaskid(),taskStartDto.isAfresh());
            if(null==taskModel){
                StartTaskEntity startTaskEntity=StartTaskEntity
                        .builder()
                        .code("1000")
                        .taskId(taskStartDto.getTaskid())
                        .msg("The task has not been created yet")
                        .build();
                resultList.add(startTaskEntity);
                continue;
            }

            try {
                String id=singleRedisService.runSyncerTask(taskModel);
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
                        .taskId(taskModel.getId())
                        .msg("Error_"+e.getMessage())
                        .build();
                resultList.add(startTaskEntity);
            }

        }

        return ResultMap.builder().data(resultList);
    }

    @Override
    public ResultMap startSyncerTaskByGroupId(String groupId,boolean afresh) throws Exception {
        List<TaskModel>taskModelList=taskMapper.findTaskByGroupId(groupId);
        if(taskModelList==null){
            return ResultMap.builder().msg("GroupId不存在");
        }
        Map<String,String> resultList=new HashMap<>();

        for (TaskModel taskModel : taskModelList) {
            if(!TaskDataManagerUtils.isTaskClose(taskModel.getId())){
                resultList.put(taskModel.getId(),"The task is running");
                continue;
            }
            if(afresh!=taskModel.isAfresh()){
                try {
                    taskMapper.updateAfreshsetById(taskModel.getId(),afresh);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                String id=singleRedisService.runSyncerTask(taskModel);
                resultList.put(id,"OK");
            } catch (Exception e) {
                resultList.put(taskModel.getId(),"Error_"+e.getMessage());
            }

        }

        return ResultMap.builder().data(resultList);
    }
}
