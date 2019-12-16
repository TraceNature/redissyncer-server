package syncer.syncerpluswebapp.controller.v1.api;

import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.entity.dto.task.EditRedisFileDataDto;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerpluswebapp.util.DtoCheckUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerservice.service.IRedisSyncerService;

import java.util.HashMap;

@RestController
@RequestMapping(value = "/api/v1/file")
@Validated
public class RedisFileReplicatorController {

    @Autowired
    RedisPoolProps redisPoolProps;
    @Autowired
    IRedisSyncerService redisBatchedSyncerService;

    @RequestMapping(value = "/creattask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap creattask(@RequestBody @Validated  RedisFileDataDto redisFileDataDto) throws TaskMsgException {

        redisFileDataDto.setRedisFileDataDto(redisPoolProps.getMinPoolSize(),
                redisPoolProps.getMaxPoolSize(),
                redisPoolProps.getMaxWaitTime(),
                redisPoolProps.getTimeBetweenEvictionRunsMillis(),
                redisPoolProps.getIdleTimeRunsMillis());



        String type= String.valueOf(redisFileDataDto.getFileType()).toUpperCase();
        if(type.indexOf("RDB")>=0){
            if(redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
                redisFileDataDto.setFileType(FileType.ONLINERDB);
            }else {
                redisFileDataDto.setFileType(FileType.RDB);
            }
        }

        if(type.indexOf("AOF")>=0){
            if(redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
                redisFileDataDto.setFileType(FileType.ONLINEAOF);
            }else {
                redisFileDataDto.setFileType(FileType.AOF);
            }
        }

        if(type.indexOf("MIXED")>=0){
            if(redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
                redisFileDataDto.setFileType(FileType.ONLINEMIXED);
            }else {
                redisFileDataDto.setFileType(FileType.MIXED);
            }
        }

        RedisClusterDto redisClusterDto=new RedisClusterDto(redisPoolProps.getMinPoolSize(),
                redisPoolProps.getMaxPoolSize(),
                redisPoolProps.getMaxWaitTime(),
                redisPoolProps.getTimeBetweenEvictionRunsMillis(),
                redisPoolProps.getIdleTimeRunsMillis());
        redisClusterDto.setDbMapper(redisFileDataDto.getDbMapper());
        DtoCheckUtils.updateUri(redisFileDataDto);

        BeanUtils.copyProperties(redisFileDataDto,redisClusterDto);

        redisClusterDto.setSourceRedisAddress(redisFileDataDto.getFileAddress());
        redisClusterDto.setTargetUriData(redisFileDataDto.getTargetUriData());
        redisClusterDto.setTargetUris(redisFileDataDto.getTargetUris());



        String threadId= TemplateUtils.uuid();


        String threadName=redisFileDataDto.getTaskName();
        if(StringUtils.isEmpty(threadName)){
            threadName=threadId;
            redisFileDataDto.setTaskName(threadId);
        }

        ThreadMsgEntity msgEntity=ThreadMsgEntity.builder().id(threadId)
                .status(ThreadStatusEnum.CREATE)
                .taskName(threadName)
                .redisClusterDto(redisClusterDto)
                .build();
        try {
            TaskMsgUtils.addAliveThread(threadId, msgEntity);

        if(redisClusterDto.isAutostart()){
            redisBatchedSyncerService.batchedSync(redisClusterDto,threadId,redisFileDataDto.isAutostart());
            msgEntity.setStatus(ThreadStatusEnum.RUN);
        }else {
            msgEntity.getRedisClusterDto().setAfresh(true);
        }

        }catch (TaskMsgException ex){
            throw ex;
        }

//        catch (Exception e){
//            return  ResultMap.builder().code("1000").msg("Failed to create task");
//        }

        HashMap msg=new HashMap(10);
        msg.put("taskid",threadId);
        return  ResultMap.builder().code("2000").msg("Task created successfully").data(msg);
//        return ResultMap.getInstance().data(redisClusterDto);
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

}
