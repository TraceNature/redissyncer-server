package syncer.syncerpluswebapp.controller.v1.api;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.entity.dto.task.EditRedisFileDataDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerpluswebapp.util.DtoCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerservice.service.IRedisSyncerService;
import syncer.syncerservice.service.ISyncerTaskService;
import syncer.syncerservice.util.SyncTaskUtils;


//@RestController
//@RequestMapping(value = "/api/v1/file")
@Validated
public class RedisFileReplicatorController {

    @Autowired
    RedisPoolProps redisPoolProps;
    @Autowired
    IRedisSyncerService redisBatchedSyncerService;

    @Autowired
    ISyncerTaskService syncerTaskService;
    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap creattask(@RequestBody @Validated  RedisFileDataDto redisFileDataDto) throws TaskMsgException {
        return syncerTaskService.createFileToRedisTask(redisFileDataDto);
    }




    /**
     * 根据taskId编辑非运行状态任务
     * @param redisClusterDto
     * @return
     */
    @RequestMapping(value = "/edittask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap editTask(@RequestBody @Validated EditRedisFileDataDto redisClusterDto) throws TaskMsgException {
        DtoCheckUtils.loadingRedisClusterDto(redisClusterDto);
        return  ResultMap.builder().code("2000").msg("The request is successful").data("编辑成功");
    }



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
