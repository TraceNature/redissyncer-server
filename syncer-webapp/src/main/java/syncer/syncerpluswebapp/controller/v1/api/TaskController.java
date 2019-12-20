package syncer.syncerpluswebapp.controller.v1.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisSyncNumCheckDto;
import syncer.syncerplusredis.entity.dto.task.EditRedisClusterDto;
import syncer.syncerplusredis.entity.dto.task.ListTaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.entity.thread.ThreadReturnMsgEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerpluswebapp.util.DtoCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerservice.service.IRedisSyncerService;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.SyncTaskUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(value = "/api/v1")
@Validated
public class TaskController {
//    @Autowired
//    IRedisReplicatorService redisBatchedReplicatorService;
    @Autowired
    RedisPoolProps redisPoolProps;
    @Autowired
    IRedisSyncerService redisBatchedSyncerService;

    /**
     * 创建同步任务
     * @param redisClusterDto
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap createTask(@RequestBody @Validated RedisClusterDto redisClusterDto) throws TaskMsgException {

        List<RedisClusterDto> redisClusterDtoList=DtoCheckUtils.loadingRedisClusterDto(redisClusterDto);
        List<String>taskids=new ArrayList<>();
        for (RedisClusterDto dto:redisClusterDtoList
             ) {
            DtoCheckUtils.checkRedisClusterDto(redisClusterDto);


            dto= (RedisClusterDto) DtoCheckUtils.ckeckRedisClusterDto(dto,redisPoolProps);

            String threadId= TemplateUtils.uuid();
            String threadName=redisClusterDto.getTaskName();
            if(StringUtils.isEmpty(threadName)){
                threadName=threadId;
                dto.setTaskName(threadId);
            }

            ThreadMsgEntity  msgEntity=ThreadMsgEntity.builder().id(threadId)
                    .status(ThreadStatusEnum.CREATE)
                    .taskName(threadName)
                    .redisClusterDto(dto)
                    .build();


            try {
                TaskMsgUtils.addAliveThread(threadId, msgEntity);

            }catch (TaskMsgException ex){
                throw ex;
            } catch (Exception e){
                return  ResultMap.builder().code("1000").msg("Failed to create task");
            }


            if(dto.isAutostart()){
                redisBatchedSyncerService.batchedSync(dto,threadId,dto.isAfresh());
                msgEntity.setStatus(ThreadStatusEnum.RUN);
            }else {
                msgEntity.getRedisClusterDto().setAfresh(true);
            }
            taskids.add(threadId);
        }


        HashMap msg=new HashMap(10);
        msg.put("taskids",taskids);
        return  ResultMap.builder().code("2000").msg("Task created successfully").data(msg);
    }



    /**
     * 根据taskId编辑非运行状态任务
     * @param redisClusterDto
     * @return
     */
    @RequestMapping(value = "/edittask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap editTask(@RequestBody @Validated EditRedisClusterDto redisClusterDto) throws TaskMsgException {
        redisClusterDto= (EditRedisClusterDto) DtoCheckUtils.loadingRedisClusterDto(redisClusterDto);

        return  ResultMap.builder().code("2000").msg("The request is successful").data("编辑成功");
    }


    @RequestMapping(value = "/data")
    public ResultMap getData(){
        return ResultMap.builder().code("2000").data( TaskMsgUtils.getAliveThreadHashMap());
    }


    //api/v1/starttask


    /**
     * 根据taskId启动任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/starttask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap startTask(@RequestBody @Validated TaskStartMsgDto taskMsgDto) throws TaskMsgException {
        Map<String,String> msg= SyncTaskUtils.startCreateThread(taskMsgDto.getTaskid(),taskMsgDto.isAfresh(),redisBatchedSyncerService);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(msg);
    }


    /**
     * 根据taskId停止任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/stoptask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap stopTask(@RequestBody @Validated TaskMsgDto taskMsgDto) throws TaskMsgException {
        Map<String,String> msg=SyncTaskUtils.stopCreateThread(taskMsgDto.getTaskids());
        return  ResultMap.builder().code("2000").msg("The request is successful").data(msg);
    }




    /**
     * 根据taskId停止任务
     * @param listTaskMsgDto
     * @return
     */
    @RequestMapping(value = "/listtasks",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap listTask(@RequestBody @Validated ListTaskMsgDto listTaskMsgDto) throws TaskMsgException {

        List<ThreadReturnMsgEntity> listCreateThread=SyncTaskUtils.listCreateThread(listTaskMsgDto);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(listCreateThread);
    }


    /**
     * 删除任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/removetask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap deleteTask(@RequestBody @Validated TaskMsgDto taskMsgDto) throws TaskMsgException {
        Map<String,String> msg=SyncTaskUtils.delCreateThread(taskMsgDto.getTaskids());
//        List<ThreadReturnMsgEntity> listCreateThread=TaskMsgUtils.listCreateThread(listTaskMsgDto);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(msg);
    }

//    @RequestMapping(value = "/test")
//    public String getMap(){
//        return JSON.toJSONString( TaskMsgUtils.getAliveThreadHashMap(), SerializerFeature.DisableCircularReferenceDetect);
//    }


    @RequestMapping(value = "/checktask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap checkTask(@RequestBody @Validated RedisSyncNumCheckDto redisSyncNumCheckDto) throws TaskMsgException {
        Map<String,Long>sourceNumMap=new ConcurrentHashMap<>();
        Map<String,Long>targetNumMap=new ConcurrentHashMap<>();
        Map<String,String>resMap=new ConcurrentHashMap<>();
        String sum="sum";
        System.out.println(JSON.toJSONString(redisSyncNumCheckDto));
        redisSyncNumCheckDto.getSourceRedisAddressSet().forEach(data->{
            try {
                if(StringUtils.isEmpty(data)){
                    return;
                }
                String[] hostAndPort=data.split(":");
                List<String>dbSource= RedisUrlCheckUtils.getRedisClientKeyNum(hostAndPort[0], Integer.valueOf(hostAndPort[1]),redisSyncNumCheckDto.getSourcePassword());
                for (int i=0;i<dbSource.size();i++){
                    if(sourceNumMap.containsKey(i)){
                        sourceNumMap.put(i+"",(sourceNumMap.get(i)+Long.valueOf(dbSource.get(i))));
                    }else {
                        sourceNumMap.put(i+"",Long.valueOf(dbSource.get(i)));
                    }
                    if(sourceNumMap.containsKey(sum)){
                        Long num=sourceNumMap.get(sum)+Long.valueOf(dbSource.get(i));
                        sourceNumMap.put(sum,num);
                    }else {
                        sourceNumMap.put(sum,Long.valueOf(dbSource.get(i)));
                    }
                }
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
        });

        redisSyncNumCheckDto.getTargetRedisAddressSet().forEach(data->{
            try {
                if(StringUtils.isEmpty(data)){
                    return;
                }
                String[] hostAndPort=data.split(":");
                List<String>targetDbSource= RedisUrlCheckUtils.getRedisClientKeyNum(hostAndPort[0], Integer.valueOf(hostAndPort[1]),redisSyncNumCheckDto.getTargetPassword());
                for (int i=0;i<targetDbSource.size();i++){
                    if(targetNumMap.containsKey(i)){
                        targetNumMap.put(i+"",(targetNumMap.get(i)+Long.valueOf(targetDbSource.get(i))));
                    }else {
                        targetNumMap.put(i+"",Long.valueOf(targetDbSource.get(i)));
                    }
                    if(targetNumMap.containsKey(sum)){
                        targetNumMap.put(sum,(targetNumMap.get(sum)+Long.valueOf(targetDbSource.get(i))));
                    }else {
                        targetNumMap.put(sum,Long.valueOf(targetDbSource.get(i)));
                    }
                }
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
        });


        resMap.put("源总key数量：", String.valueOf(sourceNumMap.get(sum)));
        resMap.put("目标key数量：", String.valueOf(targetNumMap.get(sum)));
        resMap.put("源总key数量：", String.valueOf(sourceNumMap.get(sum)));
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String num = df.format((float)targetNumMap.get(sum)/sourceNumMap.get(sum));//返回的是String类型

        resMap.put("目标/源key比例：", num);
        for (Map.Entry<String,Long>source:sourceNumMap.entrySet()
             ) {
            if(!source.getKey().equalsIgnoreCase(sum)){
                resMap.put("源Db"+source.getKey(), String.valueOf(source.getValue()));
            }
        }

        for (Map.Entry<String,Long>target:targetNumMap.entrySet()
        ) {
            if(!target.getKey().equalsIgnoreCase(sum)){
                resMap.put("目标Db"+target.getKey(), String.valueOf(target.getValue()));
            }
        }
//        List<ThreadReturnMsgEntity> listCreateThread=TaskMsgUtils.listCreateThread(listTaskMsgDto);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(resMap);
    }

    public static void main(String[] args) throws TaskMsgException {

        System.out.println(JSON.toJSONString( RedisUrlCheckUtils.getRedisClientKeyNum("114.67.100.238",6379,"redistest0102")));
    }
}
