package syncer.syncerpluswebapp.controller.v2.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerpluscommon.bean.PageBean;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.StartTaskEntity;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.task.ListTaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.model.TaskModelResult;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerservice.filter.redis_start_check_strategy.RedisTaskStrategyGroupSelecter;
import syncer.syncerservice.filter.strategy_type.RedisTaskStrategyGroupType;
import syncer.syncerservice.service.ISyncerService;
import syncer.syncerservice.util.DtoToTaskModelUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/14
 */
@RestController
@RequestMapping(value = "/api/v2")
@Validated
public class TaskGroupController {
    @Autowired
    RedisPoolProps redisPoolProps;

    @Autowired
    ISyncerService taskGroupService;
    /**
     * 创建同步任务
     * @param redisClusterDto
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap createTask(@RequestBody @Validated RedisClusterDto redisClusterDto) throws Exception {

        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(redisClusterDto);

        for (TaskModel taskModel : taskModelList) {
            RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.SYNCGROUP,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);
        }
        return taskGroupService.createRedisToRedisTask(taskModelList);
    }


    /**
     * 根据taskId停止任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/stoptask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap stopTask(@RequestBody @Validated TaskMsgDto taskMsgDto) throws Exception {
        List<StartTaskEntity> msg=null;
        if(taskMsgDto.getTaskids()==null&&taskMsgDto.getGroupIds()==null){
            return  ResultMap.builder().code("4000").msg("taskids或GroupId不能为空");
        }
        if(taskMsgDto.getGroupIds()!=null&&taskMsgDto.getGroupIds().size()>0){
            msg= TaskDataManagerUtils.stopTaskListByGroupIds(taskMsgDto.getGroupIds());
        }else {
            msg= TaskDataManagerUtils.stopTaskList(taskMsgDto.getTaskids());
        }

        return  ResultMap.builder().code("2000").msg("The request is successful").data(msg);
    }

    /**
     * 根据taskId启动任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/starttask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap startTask(@RequestBody @Validated TaskStartMsgDto taskMsgDto) throws Exception {
        if(StringUtils.isEmpty(taskMsgDto.getTaskid())&&StringUtils.isEmpty(taskMsgDto.getGroupId())){
            return  ResultMap.builder().code("4000").msg("taskid或GroupId不能为空");
        }
        if(!StringUtils.isEmpty(taskMsgDto.getTaskid())){
            ResultMap resultMap=taskGroupService.startSyncerTask(Arrays.asList(taskMsgDto));
            return  resultMap.code("2000").msg("The request is successful");
        }else if(!StringUtils.isEmpty(taskMsgDto.getGroupId())){

            ResultMap resultMap=taskGroupService.startSyncerTaskByGroupId(taskMsgDto.getGroupId(),taskMsgDto.isAfresh());
            return  resultMap.code("2000").msg("The request is successful");
        }
        return  ResultMap.builder().code("4000").msg("参数错误");
    }

    /**
     * 根据taskId查询任务列表
     * @param listTaskMsgDto
     * @return
     */
    @RequestMapping(value = "/listtasks",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap listTask(@RequestBody @Validated ListTaskMsgDto listTaskMsgDto) throws Exception {

        List<TaskModelResult>  listCreateThread=TaskDataManagerUtils.listTaskList(listTaskMsgDto);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(listCreateThread);
    }



    /**
     * 根据taskId查询任务列表
     * @param listTaskMsgDto
     * @return
     */
    @RequestMapping(value = "/listtasksByPage",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap listTaskByPage(@RequestBody @Validated ListTaskMsgDto listTaskMsgDto) throws Exception {
        PageBean<TaskModelResult> listCreateThread=TaskDataManagerUtils.listTaskListByPages(listTaskMsgDto);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(listCreateThread);
    }


    /**
     * 删除任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/removetask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap deleteTask(@RequestBody @Validated TaskMsgDto taskMsgDto) throws Exception {
        List<StartTaskEntity> msg=null;
        if(taskMsgDto.getTaskids()==null&&taskMsgDto.getGroupIds()==null){
            return  ResultMap.builder().code("4000").msg("taskids或GroupId不能为空");
        }
        if(taskMsgDto.getGroupIds()!=null&&taskMsgDto.getGroupIds().size()>0){
            msg= TaskDataManagerUtils.removeTaskByGroupId(taskMsgDto.getGroupIds());
        }else {
            msg= TaskDataManagerUtils.removeTask(taskMsgDto.getTaskids());
        }


        return  ResultMap.builder().code("2000").msg("The request is successful").data(msg);
    }

}
