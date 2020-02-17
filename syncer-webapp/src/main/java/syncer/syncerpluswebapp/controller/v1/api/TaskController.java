package syncer.syncerpluswebapp.controller.v1.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import syncer.syncerjedis.Jedis;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.Configuration;
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
import syncer.syncerservice.service.ISyncerTaskService;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.SyncTaskUtils;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.regex.RegexUtil;

import java.math.RoundingMode;
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
    @Autowired
    ISyncerTaskService syncerTaskService;
    /**
     * 创建同步任务
     * @param redisClusterDto
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap createTask(@RequestBody @Validated RedisClusterDto redisClusterDto) throws TaskMsgException {
        return syncerTaskService.createRedisToRedisTask(redisClusterDto);
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


    public static void main(String[] args) {

        //        Jedis target = new Jedis("114.67.100.239",8002);
//
////
//        Object targetAuth = target.auth("redistest0102");
//        System.out.println(targetAuth);
//        String clusternodes=target.clusterNodes();
//        System.out.println();

//        String nodes1="a6f467f245fd4172a42cb7038fcae129590bebd4 10.0.16.3:6380 master - 0 1577951370037 2 connected 5461-10922\n" +
//                "7388fa0db0e438c2b5f06d36d36ef3ee58f39239 10.0.16.3:6381 master - 0 1577951369036 3 connected 10923-16383\n" +
//                "423cdb8b5f4272a7a1bdd049b95b9c16cb36d8ee 10.0.16.3:6379 myself,master - 0 0 1 connected 0-5460";
//
//
//
//        String nodes="1607a71be306ab53a477fab5559c969d3079014e 114.67.100.238:8002@18002 master - 0 1577946889000 15 connected 10923-16383\n" +
//                "1bb3c372b59acf3c52437c11c21ac0fb39e8f4e7 10.0.1.45:8002@18002 myself,slave ea26f2dff87889b1c4be6c9cfcc9f8b8bdabcdf1 0 1577946888000 16 connected\n" +
//                "aa7107f9f155f0d595c8400cef55ba0fce442b97 114.67.100.240:8002@18002 master - 0 1577946889000 1 connected 0-5460\n" +
//                "e0608f2c7eb70197ec8ce238f1054cb48549cdac 114.67.105.55:8002@18002 slave,fail aa7107f9f155f0d595c8400cef55ba0fce442b97 1573011196875 1573011194000 12 connected\n" +
//                "ea26f2dff87889b1c4be6c9cfcc9f8b8bdabcdf1 114.67.83.163:8002@18002 master - 0 1577946887000 17 connected 5461-10922\n" +
//                "bda5e75bf3c65f9acf76cf408a34513f901ae413 114.67.83.131:8002@18002 slave 1607a71be306ab53a477fab5559c969d3079014e 0 1577946889959 15 connected";
//
//        List<List<String>>list=RegexUtil.getSubListUtil(nodes,"\\w+\\s+(.*?) master -",1);
//        System.out.println( list.get(2).get(0).split("@")[0]);
////        System.out.println(JSON.toJSONString(RegexUtil.getSubListUtil(nodes1,"\\w+\\s+(.*?) master -",1)));
//
//        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
//        df.setMaximumFractionDigits(2);
//        df.setGroupingSize(0);
//        df.setRoundingMode(RoundingMode.FLOOR);
//
//        System.out.println( df.format((float) 188874189/(float) 189213005));
//
    }

}
