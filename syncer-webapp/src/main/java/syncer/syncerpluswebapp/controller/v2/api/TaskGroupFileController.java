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
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.filter.redis_start_check_strategy.RedisTaskStrategyGroupSelecter;
import syncer.syncerservice.filter.strategy_type.RedisTaskStrategyGroupType;
import syncer.syncerservice.service.ISyncerService;
import syncer.syncerservice.util.DtoToTaskModelUtils;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 文件任务操作Controller
 * @Date 2020/7/16
 */
@RestController
@RequestMapping(value = "/api/v2/file")
@Validated
public class TaskGroupFileController {
    @Autowired
    RedisPoolProps redisPoolProps;

    @Autowired
    ISyncerService taskGroupService;


    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap createtask(@RequestBody @Validated RedisFileDataDto redisFileDataDto) throws Exception {
        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(redisFileDataDto,false);

        /**
         * 检查策略
         */
        for (TaskModel taskModel : taskModelList) {
            RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.FILEGROUP,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);
        }

        return taskGroupService.createRedisToRedisTask(taskModelList);

    }



    /**
     * 根据taskId编辑非运行状态任务
     * @param fileDataDto
     * @return
     */

    @RequestMapping(value = "/edittask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap editTaskByTaskId(@RequestBody @Validated RedisFileDataDto fileDataDto)throws Exception {
        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(fileDataDto,false);
        TaskModel taskModel=taskModelList.get(0);
        RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.FILEGROUP,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);
        return taskGroupService.editSyncerTaskByTaskId(taskModel);
    }


    /**
     * 备份实时命令
     * @param redisFileDataDto
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/createCommandDumpUpTask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap creatCommandDumpUptask(@RequestBody @Validated FileCommandBackupDataDto redisFileDataDto) throws Exception {
        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(redisFileDataDto,false);

        /**
         * 检查策略
         */
        for (TaskModel taskModel : taskModelList) {
            RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.COMMANDUPGROUP,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);
        }

        return taskGroupService.createCommandDumpUptask(taskModelList);
    }



//    @RequestMapping(value = "/stopAndDeleteAll",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
//    public ResultMap stopAll() throws TaskMsgException {
//        return ResultMap.builder().code("200").data(SyncTaskUtils.stopAnddelAllCreateThread());
//    }



    //备份实时命令


//    @RequestMapping(value = "/createCommandDumpUpTask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
//    public ResultMap creatCommandDumpUptask(@RequestBody @Validated FileCommandBackupDataDto redisFileDataDto) throws TaskMsgException {
//        return syncerTaskService.creatCommandDumpUptask(redisFileDataDto);
//    }



}
