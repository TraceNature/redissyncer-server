package syncer.syncerpluswebapp.controller.v2.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.entity.dto.task.EditRedisFileDataDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerpluswebapp.util.DtoCheckUtils;
import syncer.syncerservice.filter.redis_start_check_strategy.RedisTaskStrategyGroupSelecter;
import syncer.syncerservice.filter.strategy_type.RedisTaskStrategyGroupType;
import syncer.syncerservice.service.IRedisSyncerService;
import syncer.syncerservice.service.ISyncerService;
import syncer.syncerservice.service.ISyncerTaskService;
import syncer.syncerservice.util.DtoToTaskModelUtils;
import syncer.syncerservice.util.SyncTaskUtils;

import java.util.List;


@RestController
@RequestMapping(value = "/api/v2/file")
@Validated
public class RedisFileGroupReplicatorController {

    @Autowired
    RedisPoolProps redisPoolProps;
    @Autowired
    IRedisSyncerService redisBatchedSyncerService;
    @Autowired
    ISyncerService taskGroupService;
    @Autowired
    ISyncerTaskService syncerTaskService;
    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap creattask(@RequestBody @Validated  RedisFileDataDto redisFileDataDto) throws Exception {

        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(redisFileDataDto);
        for (TaskModel taskModel : taskModelList) {
            RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.SYNCGROUP,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);
        }
        return taskGroupService.createRedisToRedisTask(taskModelList);

    }







//    /**
//     * 根据taskId编辑非运行状态任务
//     * @param redisClusterDto
//     * @return
//     */
//    @RequestMapping(value = "/edittask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
//    public ResultMap editTask(@RequestBody @Validated EditRedisFileDataDto redisClusterDto) throws TaskMsgException {
//        DtoCheckUtils.loadingRedisClusterDto(redisClusterDto);
//        return  ResultMap.builder().code("2000").msg("The request is successful").data("编辑成功");
//    }



    //备份实时命令


    @RequestMapping(value = "/createCommandDumpUpTask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap creatCommandDumpUptask(@RequestBody @Validated FileCommandBackupDataDto redisFileDataDto) throws TaskMsgException {
            return syncerTaskService.creatCommandDumpUptask(redisFileDataDto);
    }



    @RequestMapping(value = "/stopAndDeleteAll",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap stopAll() throws TaskMsgException {
        return ResultMap.builder().code("200").data(SyncTaskUtils.stopAnddelAllCreateThread());
    }

}
