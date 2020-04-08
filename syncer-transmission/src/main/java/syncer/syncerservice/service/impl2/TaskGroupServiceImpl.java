package syncer.syncerservice.service.impl2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerservice.service.IRedisSyncerService;
import syncer.syncerservice.service.IRedisTaskService;
import syncer.syncerservice.service.ISyncerService;

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
        Map<String,String> resultList=new HashMap<>();
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
                        resultList.put(taskModel.getId(),"Task created successfully and entered running state");
                    }else {
                        resultList.put(taskModel.getId(),"Task created successfully");
                        TaskDataManagerUtils.updateThreadStatus(taskModel.getId(), TaskStatusType.STOP);
                    }


                } catch (Exception e) {

                    resultList.put(taskModel.getId(),"Error_"+e.getMessage());
                }
            }
        }
        return ResultMap.builder().data(resultList);
    }

    @Override
    public ResultMap startSyncerTask(List<TaskStartMsgDto> taskStartMsgDtoList) throws Exception{

        Map<String,String> resultList=new HashMap<>();
        for (TaskStartMsgDto taskStartDto:
                taskStartMsgDtoList) {

            if(!TaskDataManagerUtils.isTaskClose(taskStartDto.getTaskid())){
                resultList.put(taskStartDto.getTaskid(),"The task is running");
                continue;
            }

            TaskModel taskModel=taskMapper.findTaskById(taskStartDto.getTaskid());
            taskModel.setAfresh(taskStartDto.isAfresh());
            taskMapper.updateAfreshsetById(taskStartDto.getTaskid(),taskStartDto.isAfresh());
            if(null==taskModel){
                resultList.put(taskModel.getId(),"The task has not been created yet");
                continue;
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
