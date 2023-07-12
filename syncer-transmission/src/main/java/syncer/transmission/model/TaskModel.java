// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.*;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import syncer.common.config.EtcdServerConfig;
import syncer.common.util.TimeUtils;
import syncer.replica.type.SyncType;
import syncer.transmission.constants.CommandKeyFilterType;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor //无参构造
@Slf4j
public class TaskModel {

    /**
     * 节点id
     */
    private String nodeId;

    private boolean circleReplication;

    private String targetNodeId;

    /**
     * 任务Id
     */
    private String id;

    /**
     * 任务Id
     */
    private String taskId;

    /**
     * 任务组Id
     */
    private String groupId;
    /**
     * 任务名称
     */
    private String taskName;

    /**
    * 源redis名称
    */
    private String sourceRedisName;

    /**
     * 源RedisUri
     */
    private String sourceRedisAddress;

    /**
     * 源Redis密码
     */
    private String sourcePassword;

    /**
    * 目标redis名称
    */
    private String targetRedisName;

    /**
     * 目标RedisUri
     */
    private String targetRedisAddress;

    /**
     * 目标Redis密码
     */
    private String targetPassword;

    /**
     * 文件地址
     */
    private String fileAddress;

    /**
     * 创建任务时是否自动启动
     */
    private boolean autostart;

    /**
     * 进入增量状态后已有OffSet
     * 重新启动时从头开始为 true 续传为false
     */
    private boolean afresh;

    /**
     * 批次大小 默认为 1500
     */
    private Integer batchSize;

    /**
     *任务类型
     * default total   1  全量＋增量
     *  stockonly      2  全量
     *  incrementonly  3  增量
     */
    @Builder.Default
    private Integer tasktype=1;

    /**
     *增量模式下从缓冲区开始同步的位置
     * 默认为 endbuffer 缓冲区尾
     *
     * "endbuffer"    1
     * "beginbuffer"  2
     *
     */
    @Builder.Default
    private Integer offsetPlace=1;

    /**
     * 任务反馈信息
     */
    @Builder.Default
    private String taskMsg="";

    /**
     * offset地址
     */
    @Builder.Default
    private volatile Long offset=-1L;



    /**
     * 任务状态
     *
     * CREATING,CREATE,RUN,STOP,PAUSE,BROKEN
     *
     *  STOP        0      停止
     *  CREATING    1      创建中
     *  CREATED      2      创建完成（完成任务信息校验进入启动阶段）
     *  RUN         3      任务启动完成，进入运行状态
     *  PAUSE       4      任务暂停
     *  BROKEN      5      任务因异常停止
     *  RDBRUNING   6      全量任务进行中
     *  COMMANDRUNING 7    增量任务进行中
     */
    @Builder.Default
    private Integer status=0;




    /**
     * redis版本
     */
    private  double redisVersion;

    /**
     * rdb版本
     */
    @Builder.Default
    private Integer rdbVersion=6;


    /**
     * 数据同步类型  ---->SyncType
     * 1 sync
     * 2 rdb
     * 3 aof
     * 4 mixed
     * 5 onlineRdb
     * 6 onlineAof
     * 7 onlineMixed
     * 8 commandDumpUp
     */
    @Builder.Default
    private Integer syncType=1;



    /**
     * 源Redis类型  RedisType
     * 1 单机
     * 2 cluster
     * 3 file
     * 4 哨兵
     */
    @Builder.Default
    private Integer sourceRedisType=1;

    /**
     * 源哨兵密码
     */
    @Builder.Default
    private String sourceSentinelAuthPassword="";


    /**
     * 目标Redis类型
     * 1 单机
     * 2 cluster
     * 3 file
     */
    @Builder.Default
    private Integer targetRedisType=1;

    /**
     * 目标哨兵密码
     */
    @Builder.Default
    private String targetSentinelAuthPassword="";

    private String sourceHost;

    private String targetHost;

    @Builder.Default
    private Integer sourcePort=6379;
    @Builder.Default
    private Integer targetPort=6379;

    /**
     * db库映射关系
     */
    private String dbMapper;

    private String createTime;

    private String updateTime;

    private String md5;

    /**
     * 全量数据分析报告
     */
    private String dataAnalysis;

    @Builder.Default
    private String replId="";


    /**
     * 全量key的数量
     */
    @Builder.Default
    private volatile Long rdbKeyCount=0L;

    /**
     * 从运行到现在的key总量
     */
    @Builder.Default
    private volatile Long allKeyCount=0L;

    /**
     * 同步到目标的key数量
     */
    @Builder.Default
    private volatile Long realKeyCount=0L;




    /**
     * 是否是第一次创建
     */
//    @Builder.Default
//    private boolean first=true;

    /**
     * 当前Key的最后一次更新时间（数据流入）
     */
    private volatile  long lastKeyUpdateTime=0L;

    /**
     * 当前Key的最后一次pipeline提交时间（数据流出）
     */
    private volatile long lastKeyCommitTime=0L;

    @Builder.Default
    private boolean sourceAcl=false;

    @Builder.Default
    private boolean targetAcl=false;

    //源用户名
    @Builder.Default
    private String sourceUserName="";
    //目标用户名
    @Builder.Default
    private String targetUserName="";


    /**
     * 错误数据总数  30L
     */
    @Builder.Default
    private volatile Long errorCount=1L;
    @Builder.Default
    private String expandJson="";

    /**
     * ttl 调整时间偏差
     */
    @Builder.Default
    private Long timeDeviation=0L;


    /**
     * 过滤器类型
     * 任意不生效
     */
    @Builder.Default
    private CommandKeyFilterType filterType=CommandKeyFilterType.NONE;

    /**
     * 命令过滤器
     */
//    @Builder.Default
//    private Set<String>commandFilter= Sets.newHashSet();

    @Builder.Default
    private String commandFilter=null;

    /**
     * Key过滤器
     */
    @Builder.Default
    private String keyFilter=null;

    /**
     * 源哨兵主节点Name
     */
    private String sourceRedisMasterName;
    /**
     * 目标主节点Name
     */
    private String targetRedisMasterName;

    private String topicName;

    private String targetKafkaAddress;

    /**
     * 当目标存在相同key的时候是否覆盖
     *
     * 适用于全量同步阶段
     */
    @Builder.Default
    private boolean rewrite=false;


    public String getExpandJson() {
        if (StringUtils.isEmpty(this.expandJson)){
            this.expandJson= JSON.toJSONString(new ExpandTaskModel());
        }
        return this.expandJson;
    }


    public ExpandTaskModel getExpandTaskJson() {
        if (StringUtils.isEmpty(this.expandJson)){
            ExpandTaskModel taskModel=new ExpandTaskModel();
            return taskModel;
        }
        return JSON.parseObject(this.expandJson,ExpandTaskModel.class);
    }

    public void updateExpandJson(ExpandTaskModel expandTaskModel) {
        this.expandJson= JSON.toJSONString(expandTaskModel);
    }

    public Map<String,Object> getDataAnalysis(){
        Map mapObj =null;
        try {
            if(StringUtils.isEmpty(dataAnalysis)){
                return null;
            }
            mapObj = JSONObject.parseObject(dataAnalysis,Map.class);
            if(mapObj==null||mapObj.size()==0){
                return null;
            }
        }catch (Exception e){
            return null;
        }
        mapObj.put("millis2String", TimeUtils.millis2String((Long) mapObj.get("time")));
        return mapObj;
    }

    public Map<Integer,Integer>loadDbMapping(){
        Map mapObj =null;
        try {
            if(StringUtils.isEmpty(dbMapper)){
                return null;
            }
            mapObj = JSONObject.parseObject(dbMapper,Map.class);
            if(mapObj==null||mapObj.size()==0){
                return null;
            }
        }catch (Exception e){
            return null;
        }
        return mapObj;
    }

    public String getSourceUri(){
        return getUri(getSourceRedisAddress(),getSourceUserName(),getSourcePassword(),sourceRedisMasterName,sourceSentinelAuthPassword);
    }

    public Set<String> getTargetUri(){
        return getUrlList(getTargetRedisAddress(),getTargetUserName(),getTargetPassword(),targetRedisMasterName,targetSentinelAuthPassword);
    }


    private   Set<String> getUrlList(String sourceUrls, String authUser,String password,String redisMasterName,String sentinelAuthPassword) {
        Set<String> urlList = new HashSet<>();
        if (StringUtils.isEmpty(sourceUrls)){
            return new HashSet<>();
        }
        String[] sourceUrlsList = sourceUrls.split(";");
        //循环遍历所有的url
        for (String url : sourceUrlsList) {
            String uri=getUri(url,authUser,password,redisMasterName,sentinelAuthPassword);
            if(!StringUtils.isEmpty(uri)) {
                urlList.add(uri);
            }
        }
        return urlList;
    }

    public  String getUri(String address,String authUser,String password,String masterName,String sentinelPassword){
        StringBuilder stringHead = new StringBuilder("redis://");
        int index=0;
        //如果截取出空字符串直接跳过
        if (address != null && address.length() > 0) {
            stringHead.append(address);
            //判断密码是否为空如果为空直接跳过
            if (password != null && password.length() > 0) {
                index++;
                stringHead.append("?authPassword=");
                stringHead.append(password);
            }

            if(authUser!=null&&authUser.length()>0){
                if(index>0){
                    stringHead.append("&authUser=");
                    stringHead.append(authUser);
                    index++;
                }else{
                    stringHead.append("?authUser=");
                    stringHead.append(authUser);
                    index++;
                }
            }

            if(masterName!=null&&masterName.length()>0){
                if(index>0){
                    stringHead.append("&masterRedisName=");
                    stringHead.append(masterName);
                    index++;
                }else{
                    stringHead.append("?masterRedisName=");
                    stringHead.append(masterName);
                    index++;
                }
            }

            if(sentinelPassword!=null&&sentinelPassword.length()>0){
                if(index>0){
                    stringHead.append("&sentinelAuthPassword=");
                    stringHead.append(sentinelPassword);
                    index++;
                }else{
                    stringHead.append("?sentinelAuthPassword=");
                    stringHead.append(sentinelPassword);
                    index++;
                }
            }

            return stringHead.toString();
        }
        return null;
    }

    /**
     * sentinel模式下校验连通性
     * @return
     */
    public String[] getSourceHostUris(){
        try {
            String[]host=sourceHost.split(";");
            for (int i = 0; i < host.length; i++) {
                host[i]=getUri(host[i],sourceUserName,sourcePassword,sourceRedisMasterName,sourceSentinelAuthPassword);
            }
            return host;
        }catch (Exception e){
            log.error("[{}] getSourceHostUris error [{}]",taskId,e.getMessage());
        }
        return null;

    }

    /**
     * sentinel模式下校验连通性
     * @return
     */
    public String[] getTargetHostUris(){
        String[]host=targetHost.split(";");
        for (int i = 0; i < host.length; i++) {
            host[i]=getUri(host[i],targetUserName,targetPassword,targetRedisMasterName,targetSentinelAuthPassword);
        }
        return host;
    }

    public void setSourceRedisAddress(String sourceRedisAddress) {
        if(StringUtils.isEmpty(sourceRedisAddress)||!(syncType.equals(SyncType.SYNC.getCode())||syncType.equals(SyncType.COMMANDDUMPUP.getCode()))){
            this.sourceRedisAddress=sourceRedisAddress;
            return;
        }


        String[] address=sourceRedisAddress.split(";");
        if(address.length==1){
            String[]data=address[0].split(":");
            if(data.length==2){
//                this.sourceHost=data[0];
                setSourceHost(data[0]);
                this.sourcePort= Integer.valueOf(data[1]);
            }
        }else{
            this.sourceHost=sourceRedisAddress;
        }

        this.sourceRedisAddress=sourceRedisAddress;
    }

    public void  setTargetRedisAddress(String targetRedisAddress) {
        if(StringUtils.isEmpty(targetRedisAddress)){
            this.targetRedisAddress = targetRedisAddress;
            return;
        }
        String[] address=targetRedisAddress.split(";");
        if(targetRedisAddress.split(";").length==1){
            if(address[0].indexOf(":")>0){
                String[]data=address[0].split(":");
                if(data.length==2){
                    this.targetHost=data[0];

                    this.targetPort= Integer.valueOf(data[1]);
                }
            }else {
                this.targetHost=address[0];
                this.targetPort=6379;
            }

        }else{
            this.targetHost=targetRedisAddress;
        }

        this.targetRedisAddress = targetRedisAddress;
    }


    public Integer getSourceRedisType() {

        return sourceRedisType;
    }

    public TaskModel(String nodeId, boolean circleReplication, String targetNodeId, String id, String taskId, String groupId, String taskName, String sourceRedisName, String sourceRedisAddress, String sourcePassword, String targetRedisName, String targetRedisAddress, String targetPassword, String fileAddress, boolean autostart, boolean afresh, Integer batchSize, Integer tasktype, Integer offsetPlace, String taskMsg, Long offset, Integer status, double redisVersion, Integer rdbVersion, Integer syncType, Integer sourceRedisType, String sourceSentinelAuthPassword, Integer targetRedisType, String targetSentinelAuthPassword, String sourceHost, String targetHost, Integer sourcePort, Integer targetPort, String dbMapper, String createTime, String updateTime, String md5, String dataAnalysis, String replId, Long rdbKeyCount, Long allKeyCount, Long realKeyCount, long lastKeyUpdateTime, long lastKeyCommitTime, boolean sourceAcl, boolean targetAcl, String sourceUserName, String targetUserName, Long errorCount, String expandJson, Long timeDeviation, CommandKeyFilterType filterType, String commandFilter, String keyFilter, String sourceRedisMasterName, String targetRedisMasterName, String topicName, String targetKafkaAddress, boolean rewrite) {
        this.id = id;
        this.taskId=taskId;
        this.circleReplication = circleReplication;
        this.targetNodeId = targetNodeId;
        this.sourceRedisName = sourceRedisName;
        this.targetRedisName = targetRedisName;
        this.groupId = groupId;
        this.taskName = taskName;
        this.sourceRedisAddress = sourceRedisAddress;
        this.sourcePassword = sourcePassword;
        this.targetRedisAddress = targetRedisAddress;
        this.targetPassword = targetPassword;
        this.fileAddress = fileAddress;
        this.autostart = autostart;
        this.afresh = afresh;
        this.batchSize = batchSize;
        this.tasktype = tasktype;
        this.offsetPlace = offsetPlace;
        this.taskMsg = taskMsg;
        this.offset = offset;
        this.status = status;
        this.redisVersion = redisVersion;
        this.rdbVersion = rdbVersion;
        this.syncType = syncType;
        this.sourceHost = sourceHost;
        this.targetHost = targetHost;
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.dbMapper = dbMapper;
//        this.first=first;
        this.md5=md5;
        this.createTime=createTime;
        this.updateTime=updateTime;
        this.dataAnalysis=dataAnalysis;
        this.replId=replId;
        this.rdbKeyCount=rdbKeyCount;
        this.allKeyCount=allKeyCount;
        this.realKeyCount=realKeyCount;
        this.lastKeyUpdateTime=lastKeyUpdateTime;
        this.lastKeyCommitTime=lastKeyCommitTime;
        this.sourceAcl=sourceAcl;
        this.targetAcl=targetAcl;
        this.sourceUserName=sourceUserName;
        this.targetUserName=targetUserName;
        this.errorCount=errorCount;
        this.expandJson=expandJson;
        this.timeDeviation=timeDeviation;
        this.filterType=filterType;
        this.commandFilter=commandFilter;
        this.keyFilter=keyFilter;
        this.sourceSentinelAuthPassword=sourceSentinelAuthPassword;
        this.targetSentinelAuthPassword=targetSentinelAuthPassword;
        this.sourceRedisType = sourceRedisType;
        this.targetRedisType = targetRedisType;
        this.sourceRedisMasterName=sourceRedisMasterName;
        this.targetRedisMasterName=targetRedisMasterName;
        this.topicName=topicName;
        this.targetKafkaAddress=targetKafkaAddress;
        setTargetRedisAddress(this.getTargetRedisAddress());
        setSourceRedisAddress(this.getSourceRedisAddress());
        EtcdServerConfig config=new EtcdServerConfig();
        this.setNodeId(config.getNodeId());
        this.rewrite=rewrite;
    }


    public String getNodeId() {
        if(Objects.isNull(nodeId)){
            EtcdServerConfig config=new EtcdServerConfig();
            return config.getNodeId();
        }
        return nodeId;
    }

    public String getId() {
        if(StringUtils.isEmpty(id)&&!StringUtils.isEmpty(taskId)){
            id=taskId;
        }
        return id;
    }

    public String getTaskId() {
        if(StringUtils.isEmpty(taskId)&&!StringUtils.isEmpty(id)){
            taskId=id;
        }
        return taskId;
    }

    public static void main(String[] args) {
//        TaskModel taskModel=TaskModel.builder()
//                .dbMapper("{\"0\":\"1\",\"1\":\"1\",\"2\":\"1\",\"3\":\"1\"}").build();
//
//        System.out.println(taskModel.getDbMapping());

        TaskModel taskModel=TaskModel.builder().targetRedisAddress("111.20:0;awddwa:0").sourceRedisAddress("111.20:1;").build();



//        taskModel.setSourceRedisAddress(taskModel.getSourceRedisAddress());
//        taskModel.setTargetRedisAddress(taskModel.getTargetRedisAddress());
    }
}
