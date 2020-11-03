package syncer.syncerpluswebapp.controller.v2.api;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
//import springfox.documentation.annotations.ApiIgnore;
import syncer.syncerpluscommon.bean.PageBean;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.StartTaskEntity;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisSyncNumCheckDto;
import syncer.syncerplusredis.entity.dto.task.ListTaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.model.TaskModelResult;
import syncer.syncerplusredis.util.TaskDataManagerUtils;

import syncer.syncerpluswebapp.config.submit.Resubmit;
//import syncer.syncerpluswebapp.config.swagger.CommonData;
//import syncer.syncerpluswebapp.config.swagger.model.ApiJsonObject;
//import syncer.syncerpluswebapp.config.swagger.model.ApiJsonProperty;
//import syncer.syncerpluswebapp.config.swagger.model.ApiJsonResult;
import syncer.syncerservice.filter.redis_start_check_strategy.RedisTaskStrategyGroupSelecter;
import syncer.syncerservice.filter.strategy_type.RedisTaskStrategyGroupType;
import syncer.syncerservice.service.ISyncerService;
import syncer.syncerservice.util.DtoToTaskModelUtils;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.common.Montitor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import static syncer.syncerpluswebapp.config.swagger.model.GlobalString.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/14
 */
@Api("/api/v2")
@RestController
@RequestMapping(value = "/api/v2")
@Validated
@Slf4j
public class TaskGroupController {

    @Autowired
    RedisPoolProps redisPoolProps;

    @Autowired
    ISyncerService taskGroupService;

    @Autowired
    Montitor montitor;
    /**
     * 创建同步任务
     * @param redisClusterDto
     * @return
     * @throws TaskMsgException
     */
    @ApiOperation(value = "创建实时同步任务接口", notes = "用于在线(SYNC)任务创建")

    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
//    @ApiJsonObject(name = "manager-checkManager", value = {
//            @ApiJsonProperty(name = JSON_SOURCE_REDIS_ADDRESS,required = true),
//            @ApiJsonProperty(name = JSON_SOURCE_REDIS_PASSWORD),
//            @ApiJsonProperty(name = JSON_TARGET_REDIS_ADDRESS,required = true),
//            @ApiJsonProperty(name = JSON_TARGET_REDIS_PASSWORD),
//            @ApiJsonProperty(name = JSON_TARGET_REDIS_VERION,required = true),
//            @ApiJsonProperty(name = JSON_TASKNAME,required = true),
//            @ApiJsonProperty(name = JSON_AUTO_START),
//            @ApiJsonProperty(name = JSON_BATCHSIZE),
//            @ApiJsonProperty(name = JSON_DBMAPPER),
//    },
//            result = @ApiJsonResult(type = CommonData.RESULT_TYPE_NORMAL_FINAL,name = "data",value = {
//                    JSON_RESULT_CODE,
//                    JSON_RESULT_MSG,
//                    JSON_RESULT_DATA
//            }))
    @ApiImplicitParam(name = "params", required = true, dataType = "manager-checkManager")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", reference = "manager-checkManager")})

//    @ApiResponses({
//            @ApiResponse(code=2000,message="任务启动请求后台已处理"),
//            @ApiResponse(code=400,message="信息传入错误(请检查JSON格式是否正确) 错误请求...."),
//            @ApiResponse(code = 500,message = "服务器开小差了..."),
//            @ApiResponse(code = 4000,message = "目标redis连接失败"),
//            @ApiResponse(code = 4001,message = "任务URI信息有误，请检查"),
//            @ApiResponse(code = 4002,message = "相同配置任务已存在，请修改任务名"),
//            @ApiResponse(code = 4024,message = "targetRedisVersion can not be empty /targetRedisVersion error"),
//            @ApiResponse(code = 4027,message = "incrementtype参数错误 只能为（beginbuffer/endbuffer）")
//    })
    @Resubmit(delaySeconds = 10)
    public ResultMap createTask( @RequestBody @Validated RedisClusterDto params) throws Exception {
        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(params,false);

        if(null==taskModelList||taskModelList.size()==0){
            return ResultMap.builder().code("1000").msg("任务列表为空，请检查填入任务信息");
        }

        for (TaskModel taskModel : taskModelList) {
            RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.SYNCGROUP,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);
        }


        double montitors = new BigDecimal((float)montitor.jvmMemoryUsed()/montitor.jvmMemoryMax()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if(montitors>=0.80){
            return ResultMap.builder().code("1005").msg("当前系统已处于高负载状态,已开启任务数量限制，请稍后再创建任务");
        }

//        if(montitors>=0.80){
//            return ResultMap.builder().code("1000").msg("当前系统已处于高负载状态,已开启任务数量限制，请稍后再创建任务");
//        }


        return taskGroupService.createRedisToRedisTask(taskModelList);
    }




    /**
     * 根据taskId停止任务
     * @param taskMsgDto
     * @return
     */



//    @ApiJsonObject(name = "manager-stopTask", value = {
//            @ApiJsonProperty(name = JSON_TASKIDS,required = false),
//            @ApiJsonProperty(name = JSON_TGROUPIDS,required = false)
//    }, result = @ApiJsonResult(value = {
//                    JSON_RESULT_CODE,
//                    JSON_RESULT_MSG,
//                    JSON_STOPTASK_RESULT_DATA
//            }))
//    @ApiImplicitParam(name = "params", required = true, dataType = "manager-stopTask")
//    @ApiResponses({@ApiResponse(response=String.class,code = 200, message = "OK", reference = "manager-stopTask")})


//    @ApiResponses({
//            @ApiResponse(code=2000,message="Task stopped successfully"),
//            @ApiResponse(code=400,message="信息传入错误(请检查JSON格式是否正确) 错误请求...."),
//            @ApiResponse(code = 500,message = "服务器开小差了..."),
//            @ApiResponse(code = 1000,message = "Task stopped fail"),
//            @ApiResponse(code = 1001,message = "The current task is not running"),
//            @ApiResponse(code = 1002,message = "The task does not exist. Please create the task first"),
//            @ApiResponse(code = 4000,message = "taskids或GroupId不能为空  【外层code】")
//    })
    @ApiOperation("停止任务")
    @RequestMapping(value = "/stoptask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    @Resubmit(delaySeconds = 10)
    public ResultMap stopTask( @RequestBody @Validated TaskMsgDto params) throws Exception {
        List<StartTaskEntity> msg=null;
        if(params.getTaskids()==null&&params.getGroupIds()==null){
            return  ResultMap.builder().code("4000").msg("taskids或GroupId不能为空");
        }
        if(params.getGroupIds()!=null&&params.getGroupIds().size()>0){
            msg= TaskDataManagerUtils.stopTaskListByGroupIds(params.getGroupIds());
        }else {
            msg= TaskDataManagerUtils.stopTaskList(params.getTaskids());
        }
        return  ResultMap.builder().code("2000").msg("The request is successful").data(msg);
    }

    /**
     * 根据taskId启动任务
     * @param taskMsgDto
     * @return
     */
    @RequestMapping(value = "/starttask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")

//
//    @ApiJsonObject(name = "manager-starttask", value = {
//            @ApiJsonProperty(name = JSON_TASKIDS),
//            @ApiJsonProperty(name = JSON_TGROUPIDS),
//            @ApiJsonProperty(name = JSON_AUTO_AFRESH)
//
//    },
//            result = @ApiJsonResult(type = CommonData.RESULT_TYPE_NORMAL_FINAL,name = "data",value = {
//                    JSON_RESULT_CODE,
//                    JSON_RESULT_MSG,
//                    JSON_STARTASK_RESULT_DATA
//            }))
    @ApiImplicitParam(name = "params", required = true, dataType = "manager-starttask")
    @ApiResponses({@ApiResponse(response=String.class,code = 200, message = "OK", reference = "manager-starttask")})


//    @ApiResponses({
//            @ApiResponse(code=400,message="信息传入错误(请检查JSON格式是否正确) 错误请求...."),
//            @ApiResponse(code = 500,message = "服务器开小差了..."),
//            @ApiResponse(code = 1000,message = "Error_msg"),
//            @ApiResponse(code = 1001,message = "The task is running"),
//            @ApiResponse(code = 1002,message = "The task has not been created yet"),
//            @ApiResponse(code = 1004,message = "GroupId不存在"),
//            @ApiResponse(code=2000,message="OK")
//    })

    @Resubmit(delaySeconds = 10)
    public ResultMap startTask( @RequestBody @Validated TaskStartMsgDto params) throws Exception {
        if(StringUtils.isEmpty(params.getTaskid())&&StringUtils.isEmpty(params.getGroupId())){
            return  ResultMap.builder().code("4000").msg("taskid或GroupId不能为空");
        }

//        double montitors = new BigDecimal((float)montitor.jvmMemoryUsed()/montitor.jvmMemoryMax()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//        if(montitors>=0.80){
//            return ResultMap.builder().code("1005").msg("当前系统已处于高负载状态,已开启任务数量限制，请稍后再创建任务");
//        }

        if(!StringUtils.isEmpty(params.getTaskid())){
            ResultMap resultMap=taskGroupService.startSyncerTask(Arrays.asList(params));
            return  resultMap.code("2000").msg("The request is successful");
        }else if(!StringUtils.isEmpty(params.getGroupId())){
            ResultMap resultMap=taskGroupService.startSyncerTaskByGroupId(params.getGroupId(),params.isAfresh());
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
    @ApiOperation("任务查询")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", responseContainer = "Map",
                    examples = @Example({
                            @ExampleProperty(value = "{\n" +
                                    "    \"msg\": \"The request is successful\",\n" +
                                    "    \"code\": \"2000\",\n" +
                                    "    \"data\": [\n" +
                                    "        {\n" +
                                    "            \"taskId\": \"DE034278589D47FAB92D3B3DCBC668D1\",\n" +
                                    "            \"groupId\": \"DE034278589D47FAB92D3B3DCBC668D1\",\n" +
                                    "            \"taskName\": \"firsttest\",\n" +
                                    "            \"sourceRedisAddress\": \"114.67.100.239:6379\",\n" +
                                    "            \"targetRedisAddress\": \"114.67.100.240:6379\",\n" +
                                    "            \"fileAddress\": \"\",\n" +
                                    "            \"autostart\": false,\n" +
                                    "            \"afresh\": true,\n" +
                                    "            \"batchSize\": 0,\n" +
                                    "            \"tasktype\": \"TOTAL\",\n" +
                                    "            \"offsetPlace\": \"ENDBUFFER\",\n" +
                                    "            \"taskMsg\": \"\",\n" +
                                    "            \"brokenReason\": \"\",\n" +
                                    "            \"offset\": -1,\n" +
                                    "            \"status\": \"STOP\",\n" +
                                    "            \"redisVersion\": 5.0,\n" +
                                    "            \"rdbVersion\": 9,\n" +
                                    "            \"syncType\": \"SYNC\",\n" +
                                    "            \"sourceRedisType\": \"SINGLE\",\n" +
                                    "            \"targetRedisType\": \"SINGLE\",\n" +
                                    "            \"dbMapper\": null,\n" +
                                    "            \"analysisMap\": null,\n" +
                                    "            \"createTime\": \"2020-11-03 09:42:23\",\n" +
                                    "            \"updateTime\": \"2020-11-03 09:42:23\",\n" +
                                    "            \"replId\": \"\",\n" +
                                    "            \"rdbKeyCount\": 0,\n" +
                                    "            \"allKeyCount\": 0,\n" +
                                    "            \"realKeyCount\": 0,\n" +
                                    "            \"commandKeyCount\": 0,\n" +
                                    "            \"rate\": 0.0,\n" +
                                    "            \"rate2Int\": 0,\n" +
                                    "            \"lastDataUpdateIntervalTime\": 1604367752973,\n" +
                                    "            \"lastDataCommitIntervalTime\": 1604367752973\n" +
                                    "        }\n" +
                                    "    ]\n" +
                                    "}", mediaType = "application/json")
                    })),
            @ApiResponse(code = 400, message = "parameters are not correct"),
            @ApiResponse(code = 404, message = "path is not correct")
    })
    public ResultMap listTask(@RequestBody @Validated ListTaskMsgDto params) throws Exception {
        List<TaskModelResult>  listCreateThread=TaskDataManagerUtils.listTaskList(params);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(listCreateThread);
    }



    /**
     * 根据taskId查询任务列表
     * @param listTaskMsgDto
     * @return
     */
    @RequestMapping(value = "/listtasksByPage",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")


    @ApiImplicitParams({

            @ApiImplicitParam(paramType = "query",name = "regulation",value ="查询规则['bynames','all','byids','bystatus','byGroupIds']",dataType ="String",required = false),
            @ApiImplicitParam(paramType = "query",name = "tasknames",value ="taskId List ['taskId1','taskId2']",dataType ="String",required = false),
            @ApiImplicitParam(paramType = "query",name = "taskstatus",value ="任务状态",dataType ="String",required = false),
            @ApiImplicitParam(paramType = "query",name = "taskids",value ="taskId List ['taskId1','taskId2']",dataType ="String",required = false),
            @ApiImplicitParam(paramType = "query",name = "groupIds",value ="groupId List ['groupId1','groupId2']",dataType ="String",required = false),
            @ApiImplicitParam(paramType = "query",name = "currentPage",value ="当前页数",dataType ="Integer",required = false),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value ="页数大小",dataType ="Integer",required = false),
    })


    @ApiResponses({
            @ApiResponse(code=400,message="信息传入错误(请检查JSON格式是否正确) 错误请求...."),
            @ApiResponse(code=4000,message="taskids或GroupId不能为空"),
            @ApiResponse(code=2000,message="The request is successful"),
            @ApiResponse(code = 500,message = "服务器开小差了..."),
            @ApiResponse(code = 4009,message = "tasknames参数不能有为空"),
            @ApiResponse(code = 4010,message = "taskstatus 不能有为空"),
            @ApiResponse(code = 4011,message = "taskstatus 格式不正确"),
            @ApiResponse(code = 4012,message = "groupIds不能有为空"),
    })

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

//    @ApiImplicitParams({
//            @ApiImplicitParam(paramType = "query",name = "taskids",value ="taskId List ['taskId1','taskId2']",dataType ="String",required = false),
//            @ApiImplicitParam(paramType = "query",name = "groupIds",value ="groupId List ['groupId1','groupId2']",dataType ="String",required = false)
//    })


//
//    @ApiJsonObject(name = "manager-removetask", value = {
//            @ApiJsonProperty(name = JSON_TASKIDS),
//            @ApiJsonProperty(name = JSON_TGROUPIDS)
//    },
//            result = @ApiJsonResult(type = CommonData.RESULT_TYPE_NORMAL_FINAL,name = "data",value = {
//                    JSON_RESULT_CODE,
//                    JSON_RESULT_MSG,
//                    JSON_REMOVETASK_RESULT_DATA
//            }))
    @ApiImplicitParam(name = "params", required = true, dataType = "manager-removetask")
    @ApiResponses({@ApiResponse(response=String.class,code = 200, message = "OK", reference = "manager-removetask")})


//    @ApiResponses({
//            @ApiResponse(code=400,message="信息传入错误(请检查JSON格式是否正确) 错误请求...."),
//            @ApiResponse(code=4000,message="taskids或GroupId不能为空"),
//            @ApiResponse(code=2000,message="The request is successful"),
//
//            @ApiResponse(code = 500,message = "服务器开小差了..."),
//            @ApiResponse(code = 1000,message = "Delete failed"),
//            @ApiResponse(code = 1001,message = "task is running,please stop the task first"),
//            @ApiResponse(code = 1002,message = "Task does not exist"),
//            @ApiResponse(code = 2000,message = "Delete successful"),
//            @ApiResponse(code=2000,message="OK")
//    })
    @Resubmit(delaySeconds = 10)
    public ResultMap deleteTask(@RequestBody @Validated TaskMsgDto params) throws Exception {
        List<StartTaskEntity> msg=null;
        if(params.getTaskids()==null&&params.getGroupIds()==null){
            return  ResultMap.builder().code("4000").msg("taskids或GroupId不能为空");
        }
        if(params.getGroupIds()!=null&&params.getGroupIds().size()>0){
            msg= TaskDataManagerUtils.removeTaskByGroupId(params.getGroupIds());
        }else {
            msg= TaskDataManagerUtils.removeTask(params.getTaskids());
        }
        return  ResultMap.builder().code("2000").msg("The request is successful").data(msg);
    }


    @RequestMapping(value = "/edittask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap editTaskByTaskId(@RequestBody @Validated RedisClusterDto redisClusterDto)throws Exception {
        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(redisClusterDto,true);
        TaskModel taskModel=taskModelList.get(0);
        RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.SYNCGROUP,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);
        return taskGroupService.editSyncerTaskByTaskId(taskModel);
    }


    @RequestMapping(value = "/checktask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap checkTask(@RequestBody @Validated RedisSyncNumCheckDto redisSyncNumCheckDto) throws TaskMsgException {
        Map<String,Long>sourceNumMap=new ConcurrentHashMap<>();
        Map<String,Long>targetNumMap=new ConcurrentHashMap<>();
        Map<String,String>resMap=new ConcurrentHashMap<>();
        String sum="sum";
        redisSyncNumCheckDto.getSourceRedisAddressSet().forEach(data->{
            try {
                if(StringUtils.isEmpty(data)){
                    return;
                }
                String[] hostAndPort=data.split(":");
                List<List<String>>dbSource= RedisUrlCheckUtils.getRedisClientKeyNum(hostAndPort[0], Integer.valueOf(hostAndPort[1]),redisSyncNumCheckDto.getSourcePassword());
                for (int i=0;i<dbSource.size();i++){
                    if(sourceNumMap.containsKey(dbSource.get(i).get(0))){
                        Long numData=sourceNumMap.get(dbSource.get(i).get(0))+Long.valueOf(dbSource.get(i).get(1));
                        sourceNumMap.put(dbSource.get(i).get(0),numData);
                    }else {
                        sourceNumMap.put(dbSource.get(i).get(0),Long.valueOf(dbSource.get(i).get(1)));
                    }
                    if(sourceNumMap.containsKey(sum)){
                        Long num=sourceNumMap.get(sum)+Long.valueOf(dbSource.get(i).get(1));
                        sourceNumMap.put(sum,num);
                    }else {
                        sourceNumMap.put(sum,Long.valueOf(dbSource.get(i).get(1)));
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
                List<List<String>>targetDbSource= RedisUrlCheckUtils.getRedisClientKeyNum(hostAndPort[0], Integer.valueOf(hostAndPort[1]),redisSyncNumCheckDto.getTargetPassword());

                for (int i=0;i<targetDbSource.size();i++){
                    if(targetNumMap.containsKey(targetDbSource.get(i).get(0))){
                        Long numData=targetNumMap.get(targetDbSource.get(i).get(0))+Long.valueOf(targetDbSource.get(i).get(1));
                        targetNumMap.put(targetDbSource.get(i).get(0),numData);
                    }else {
                        targetNumMap.put(targetDbSource.get(i).get(0),Long.valueOf(targetDbSource.get(i).get(1)));
                    }
                    if(targetNumMap.containsKey(sum)){
                        targetNumMap.put(sum,(targetNumMap.get(sum)+Long.valueOf(targetDbSource.get(i).get(1))));
                    }else {
                        targetNumMap.put(sum,Long.valueOf(targetDbSource.get(i).get(1)));
                    }
                }


            } catch (TaskMsgException e) {
                e.printStackTrace();
            }


        });


        resMap.put("目标总key数量：源总key数量", new StringBuilder(loadingValue(String.valueOf(targetNumMap.get(sum)))).append(":").append(loadingValue(String.valueOf(sourceNumMap.get(sum)))).toString());
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        df.setMaximumFractionDigits(2);
        df.setGroupingSize(0);
        df.setRoundingMode(RoundingMode.FLOOR);

        if(null==sourceNumMap.get(sum)||0==sourceNumMap.get(sum)){
            resMap.put("目标/源key比例：", "源redis没有数据无法比较");
        }else {
            String num="[有库无数据]无法比较";
            if((targetNumMap.get(sum)==null||targetNumMap.get(sum)==0)||(sourceNumMap.get(sum)==null||sourceNumMap.get(sum)==0)){
                if(targetNumMap.get(sum)!=null&&sourceNumMap.get(sum)!=null&&targetNumMap.get(sum)==0&&sourceNumMap.get(sum)==0){
                    num="1.00";
                }
            }else {


                num = df.format((float)targetNumMap.get(sum)/(float)sourceNumMap.get(sum));//返回的是String类型
            }

            resMap.put("目标/源key比例：", num);
        }

        for (Map.Entry<String,Long>source:sourceNumMap.entrySet()
        ) {
            if(!source.getKey().equalsIgnoreCase(sum)){
                String key=new StringBuilder("目标:源 Db")
                        .append("[")
                        .append(source.getKey())
                        .append("]")
                        .toString();
                String value=new StringBuilder()
                        .append(getTargetMapValue(source.getKey(),targetNumMap))
                        .append(":")
                        .append(source.getValue())
                        .toString();
                resMap.put(key, value);
            }
        }

        for (Map.Entry<String,Long>target:targetNumMap.entrySet()
        ) {
            if(!target.getKey().equalsIgnoreCase(sum)&&!sourceNumMap.containsKey(target.getKey())){
                String key=new StringBuilder("目标:源 Db")
                        .append("[")
                        .append(target.getKey())
                        .append("]")
                        .toString();
                String value=new StringBuilder()
                        .append(target.getValue())
                        .append(":")
                        .append(getTargetMapValue(target.getKey(),sourceNumMap))
                        .toString();
                resMap.put(key, value);
                resMap.put(key, value);
            }
        }
//        List<ThreadReturnMsgEntity> listCreateThread=TaskMsgUtils.listCreateThread(listTaskMsgDto);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(resMap);
    }

//    public static void main(String[] args) throws TaskMsgException {
//        List<String>list=new ArrayList<>();
//        list.add("wdw");
//        System.out.println(list.get(0));
//        System.out.println(JSON.toJSONString( RedisUrlCheckUtils.getRedisClientKeyNum("114.67.100.238",6379,"redistest0102")));
//    }


    String getTargetMapValue(String key,Map<String,Long>map){
        if(map.containsKey(key)){
            return String.valueOf(map.get(key));
        }
        return "无数据";
    }

    String loadingValue(String key){
        if(StringUtils.isEmpty(key)||"null".equals(key)){
            return "无数据";
        }
        return key;
    }






}
