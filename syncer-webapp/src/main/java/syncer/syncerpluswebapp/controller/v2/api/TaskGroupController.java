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
import syncer.syncerplusredis.entity.dto.RedisSyncNumCheckDto;
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
import syncer.syncerservice.util.RedisUrlCheckUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(redisClusterDto,false);
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
