package com.i1314i.syncerpluswebapp.controller.v1.api;

import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerpluscommon.util.common.TemplateUtils;
import com.i1314i.syncerplusservice.constant.ThreadStatusEnum;
import com.i1314i.syncerplusservice.entity.RedisPoolProps;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.task.EditRedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.task.ListTaskMsgDto;
import com.i1314i.syncerplusservice.entity.dto.task.TaskMsgDto;
import com.i1314i.syncerplusservice.entity.thread.ThreadMsgEntity;
import com.i1314i.syncerplusservice.entity.thread.ThreadReturnMsgEntity;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMsgUtils;
import com.i1314i.syncerpluswebapp.util.DtoCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1")
@Validated
public class TaskController {
    @Autowired
    IRedisReplicatorService redisBatchedReplicatorService;
    @Autowired
    RedisPoolProps redisPoolProps;

    /**
     * 创建同步任务
     * @param redisClusterDto
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/creattask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap createTask(@RequestBody @Validated RedisClusterDto redisClusterDto) throws TaskMsgException {

        redisClusterDto= (RedisClusterDto) DtoCheckUtils.ckeckRedisClusterDto(redisClusterDto,redisPoolProps);
        String threadId= TemplateUtils.uuid();


            String threadName=redisClusterDto.getThreadName();
            if(StringUtils.isEmpty(threadName)){
                threadName=threadId;
                redisClusterDto.setThreadName(threadId);
            }

            ThreadMsgEntity  msgEntity=ThreadMsgEntity.builder().id(threadId)
                    .status(ThreadStatusEnum.CREATE)
                    .threadName(threadName)
                    .redisClusterDto(redisClusterDto)
                    .build();

            if(redisClusterDto.isAutostart()){
                redisBatchedReplicatorService.batchedSync(redisClusterDto,threadId);
                msgEntity.setStatus(ThreadStatusEnum.RUN);
            }
        try {
            TaskMsgUtils.addAliveThread(threadId, msgEntity);

        }catch (TaskMsgException ex){
            throw ex;
        } catch (Exception e){
            return  ResultMap.builder().code("100").msg("Failed to create task");
        }

        HashMap msg=new HashMap();
        msg.put("taskid",threadId);
        return  ResultMap.builder().code("200").msg("Task created successfully").data(msg);
    }

    @RequestMapping(value = "/data")
    public ResultMap getData(){
        return ResultMap.builder().code("200").data( TaskMsgUtils.getAliveThreadHashMap());
    }


    //api/v1/starttask


    /**
     * 根据taskId启动任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/starttask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap startTask(@RequestBody @Validated TaskMsgDto taskMsgDto) throws TaskMsgException {
        Map<String,String> msg=TaskMsgUtils.startCreateThread(taskMsgDto.getTaskids(),redisBatchedReplicatorService);
        return  ResultMap.builder().code("200").msg("The request is successful").data(msg);
    }


    /**
     * 根据taskId停止任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/stoptask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap stopTask(@RequestBody @Validated TaskMsgDto taskMsgDto) throws TaskMsgException {
        Map<String,String> msg=TaskMsgUtils.stopCreateThread(taskMsgDto.getTaskids());
        return  ResultMap.builder().code("200").msg("The request is successful").data(msg);
    }


    /**
     * 根据taskId编辑非运行状态任务
     * @param redisClusterDto
     * @return
     */
    @RequestMapping(value = "/edittask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap editTask(@RequestBody @Validated EditRedisClusterDto redisClusterDto) throws TaskMsgException {
        redisClusterDto= (EditRedisClusterDto) DtoCheckUtils.ckeckRedisClusterDto(redisClusterDto,redisPoolProps);
        TaskMsgUtils.getThreadMsgEntity(redisClusterDto.getTaskId());
//
//        ThreadMsgEntity  msgEntity=ThreadMsgEntity.builder().id(threadId)
//                .status(ThreadStatusEnum.CREATE)
//                .threadName(threadName)
//                .redisClusterDto(redisClusterDto)
//                .build();
//
//
        Map<String,String> msg=TaskMsgUtils.stopCreateThread(Arrays.asList(""));
        return  ResultMap.builder().code("200").msg("The request is successful").data(msg);
    }


    /**
     * 根据taskId停止任务
     * @param listTaskMsgDto
     * @return
     */
    @RequestMapping(value = "/listtasks",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap listTask(@RequestBody @Validated ListTaskMsgDto listTaskMsgDto) throws TaskMsgException {

        List<ThreadReturnMsgEntity> listCreateThread=TaskMsgUtils.listCreateThread(listTaskMsgDto);
        return  ResultMap.builder().code("200").msg("The request is successful").data(listCreateThread);
    }


    /**
     * 删除任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/deletetask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap deleteTask(@RequestBody @Validated TaskMsgDto taskMsgDto) throws TaskMsgException {
        Map<String,String> msg=TaskMsgUtils.delCreateThread(taskMsgDto.getTaskids());
//        List<ThreadReturnMsgEntity> listCreateThread=TaskMsgUtils.listCreateThread(listTaskMsgDto);
        return  ResultMap.builder().code("200").msg("The request is successful").data(msg);
    }

}
