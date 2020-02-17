package syncer.syncerpluswebapp.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerjedis.Jedis;
import syncer.syncerjedis.exceptions.JedisDataException;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.constant.RedisType;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.RedisInfo;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.entity.dto.RedisSyncDataDto;
import syncer.syncerplusredis.entity.dto.common.SyncDataDto;
import syncer.syncerplusredis.entity.dto.task.EditRedisClusterDto;
import syncer.syncerplusredis.entity.dto.task.EditRedisFileDataDto;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerplusredis.util.code.CodeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import syncer.syncerpluswebapp.constant.CodeConstant;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.regex.RegexUtil;

import java.net.URISyntaxException;
import java.util.*;

@Slf4j
public class DtoCheckUtils {

    /**
     * create任务参数校验
     * @param redisClusterDto
     * @throws TaskMsgException
     */
    public synchronized static void checkRedisClusterDto(RedisClusterDto redisClusterDto) throws TaskMsgException {


//            if(!StringUtils.isEmpty(redisClusterDto.getTasktype())&&redisClusterDto.getTasktype().trim().toLowerCase().equals("file")){
//                if(StringUtils.isEmpty(redisClusterDto.getFileAddress())){
//                    throw new TaskMsgException(CodeUtils.codeMessages(CodeConstant.VALITOR_ERROR_CODE,"AOF/RDB/MIXED 地址不能为空"));
//                }
//            }else {
//                if(StringUtils.isEmpty(redisClusterDto.getSourceRedisAddress())){
//                    throw new TaskMsgException(CodeUtils.codeMessages(CodeConstant.VALITOR_ERROR_CODE,"源redis路径地址不能为空"));
//                }
//            }

            if("incrementonly".equals(redisClusterDto.getTasktype().trim().toLowerCase())){
                String incrementtype =redisClusterDto.getOffsetPlace().trim().toLowerCase();
                if(StringUtils.isEmpty(incrementtype)){
                    incrementtype="endbuffer";
                }
                if(!"endbuffer".equals(incrementtype)&&!"beginbuffer".equals(incrementtype)){
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_INCREMENT_ERROR_CODE,TaskMsgConstant.TASK_MSG_INCREMENT_ERROR));
                }
            }


//            String type= String.valueOf(redisClusterDto.getFileType()).toUpperCase();
            redisClusterDto.setFileType(FileType.SYNC);
//            if(StringUtils.isEmpty(type)){
//                redisClusterDto.setFileType(FileType.SYNC);
//            }else {
//                if(type.indexOf("RDB")>=0){
//                    if(redisClusterDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
//                            redisClusterDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
//                        redisClusterDto.setFileType(FileType.ONLINERDB);
//                    }else {
//                        redisClusterDto.setFileType(FileType.RDB);
//                    }
//                }
//
//                if(type.indexOf("AOF")>=0){
//                    if(redisClusterDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
//                            redisClusterDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
//                        redisClusterDto.setFileType(FileType.ONLINEAOF);
//                    }else {
//                        redisClusterDto.setFileType(FileType.AOF);
//                    }
//                }
//
//                if(type.indexOf("MIXED")>=0){
//                    if(redisClusterDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
//                            redisClusterDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
//                        redisClusterDto.setFileType(FileType.ONLINEMIXED);
//                    }else {
//                        redisClusterDto.setFileType(FileType.MIXED);
//                    }
//                }
//            }


//            if(redisClusterDto.getTasktype().trim().toLowerCase().equals("file")){
//                DtoCheckUtils.updateUris(redisClusterDto);
//                redisClusterDto.setSourceRedisAddress(redisClusterDto.getFileAddress());
//            }



    }


    /**
     * 补全参数
     *
     * @param syncDataDto
     * @param redisPoolProps
     * @return
     */
    public synchronized static Object ckeckRedisClusterDto(SyncDataDto syncDataDto, RedisPoolProps redisPoolProps) throws TaskMsgException {


        if (syncDataDto instanceof RedisSyncDataDto) {
//            if (syncDataDto.getIdleTimeRunsMillis() == 0) {
//                syncDataDto.setIdleTimeRunsMillis(redisPoolProps.getIdleTimeRunsMillis());
//            }
//            if (syncDataDto.getMaxWaitTime() == 0) {
//                syncDataDto.setMaxWaitTime(redisPoolProps.getMaxWaitTime());
//            }
//            if (syncDataDto.getMaxPoolSize() == 0) {
//                syncDataDto.setMaxPoolSize(redisPoolProps.getMaxPoolSize());
//            }
//            if (syncDataDto.getMinPoolSize() == 0) {
//                syncDataDto.setMinPoolSize(redisPoolProps.getMinPoolSize());
//            }
            syncDataDto.setIdleTimeRunsMillis(redisPoolProps.getIdleTimeRunsMillis());
            syncDataDto.setMaxWaitTime(redisPoolProps.getMaxWaitTime());
            syncDataDto.setMaxPoolSize(redisPoolProps.getMaxPoolSize());
            syncDataDto.setMinPoolSize(redisPoolProps.getMinPoolSize());
            syncDataDto.setTimeBetweenEvictionRunsMillis(redisPoolProps.getTimeBetweenEvictionRunsMillis());
        }

        if (syncDataDto instanceof RedisClusterDto) {

//            if (syncDataDto.getMaxWaitTime() == 0) {
//                syncDataDto.setMaxWaitTime(redisPoolProps.getMaxWaitTime());
//            }
//            if (syncDataDto.getIdleTimeRunsMillis() == 0) {
//                syncDataDto.setIdleTimeRunsMillis(redisPoolProps.getIdleTimeRunsMillis());
//            }
//            if (syncDataDto.getMaxPoolSize() == 0) {
//                syncDataDto.setMaxPoolSize(redisPoolProps.getMaxPoolSize());
//            }
//            if (syncDataDto.getMinPoolSize() == 0) {
//                syncDataDto.setMinPoolSize(redisPoolProps.getMinPoolSize());
//            }


            syncDataDto.setMaxWaitTime(redisPoolProps.getMaxWaitTime());
            syncDataDto.setIdleTimeRunsMillis(redisPoolProps.getIdleTimeRunsMillis());
            syncDataDto.setMaxPoolSize(redisPoolProps.getMaxPoolSize());
            syncDataDto.setMinPoolSize(redisPoolProps.getMinPoolSize());


            if (syncDataDto.getDbMapper() == null) {
                syncDataDto.setDbMapper(new HashMap<>());
            }
            updateUri((RedisClusterDto) syncDataDto);
            syncDataDto.setTimeBetweenEvictionRunsMillis(redisPoolProps.getTimeBetweenEvictionRunsMillis());
        }

        if (syncDataDto instanceof EditRedisClusterDto) {
            updateUri((RedisClusterDto) syncDataDto);
            syncDataDto.setTimeBetweenEvictionRunsMillis(redisPoolProps.getTimeBetweenEvictionRunsMillis());
        }
        return syncDataDto;
    }


    /**
     * 编辑任务信息
     * @param syncDataDto
     * @return
     * @throws TaskMsgException
     */
    public synchronized static Object loadingRedisClusterDto(EditRedisClusterDto syncDataDto) throws TaskMsgException {
        ThreadMsgEntity data=TaskMsgUtils.getThreadMsgEntity(syncDataDto.getTaskId());
        if(data==null){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_EXIST_ERROR_CODE,"任务【"+syncDataDto.getTaskId()+"】不存在"));

        }
        if(data.getStatus().equals(ThreadStatusEnum.RUN)){
//            throw new TaskMsgException("不能编辑正在运行中的任务【"+syncDataDto.getTaskId()+"】");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASK_EDIT_ERROR_CODE,"不能编辑正在运行中的任务【"+syncDataDto.getTaskId()+"】"));
        }
        RedisClusterDto dto=data.getRedisClusterDto();

        RedisClusterDto newDto=new RedisClusterDto(syncDataDto.getSourceRedisAddress(),
                syncDataDto.getTargetRedisAddress(),
                syncDataDto.getSourcePassword(),
                syncDataDto.getTargetPassword(),
                syncDataDto.getTaskName(),
                dto.getMinPoolSize(),
                dto.getMaxPoolSize(),
                dto.getMaxWaitTime(),
                dto.getTimeBetweenEvictionRunsMillis(),
                dto.getIdleTimeRunsMillis(),
                dto.getDiffVersion(),
                dto.getPipeline());

        if(!StringUtils.isEmpty(syncDataDto.getTargetRedisVersion())){
            newDto.setTargetRedisVersion(syncDataDto.getTargetRedisVersion());
        }else {
            newDto.setTargetRedisVersion(dto.getTargetRedisVersion());
        }

        if(syncDataDto.getBatchSize()!=0&&syncDataDto.getBatchSize()!=dto.getBatchSize()){
            newDto.setBatchSize(syncDataDto.getBatchSize());
        }else {
            newDto.setBatchSize(dto.getBatchSize());
        }

        if(syncDataDto.getDbMapper()!=null&&syncDataDto.getDbMapper().size()>0){
            newDto.setDbMapper(syncDataDto.getDbMapper());
        }else {
            newDto.setDbMapper(dto.getDbMapper());
        }





        if(StringUtils.isEmpty(syncDataDto.getTargetRedisAddress())){
            newDto.setTargetRedisAddress(dto.getTargetRedisAddress());
            newDto.setTargetPassword(dto.getTargetPassword());
        }else if(StringUtils.isEmpty(syncDataDto.getSourceRedisAddress())){
            newDto.setSourceRedisAddress(dto.getSourceRedisAddress());
            newDto.setSourcePassword(dto.getSourcePassword());
        }

        updateUri(newDto);

        if(StringUtils.isEmpty(syncDataDto.getTaskName())){
            newDto.setTaskName(dto.getTaskName());
        }
        newDto.setAutostart(syncDataDto.isAutostart());
        newDto.setAfresh(dto.isAfresh());

        data.setRedisClusterDto(newDto);
        return syncDataDto;
    }



    public synchronized static Object loadingRedisClusterDto(EditRedisFileDataDto syncDataDto) throws TaskMsgException {
        ThreadMsgEntity data=TaskMsgUtils.getThreadMsgEntity(syncDataDto.getTaskId());
        if(data==null){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_EXIST_ERROR_CODE,"任务【"+syncDataDto.getTaskId()+"】不存在"));

        }
        if(data.getStatus().equals(ThreadStatusEnum.RUN)){
//            throw new TaskMsgException("不能编辑正在运行中的任务【"+syncDataDto.getTaskId()+"】");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASK_EDIT_ERROR_CODE,"不能编辑正在运行中的任务【"+syncDataDto.getTaskId()+"】"));
        }
        RedisClusterDto dto=data.getRedisClusterDto();


        if(!StringUtils.isEmpty(syncDataDto.getTargetRedisVersion())){
            dto.setTargetRedisVersion(syncDataDto.getTargetRedisVersion());
        }

        if(syncDataDto.getBatchSize()!=0&&syncDataDto.getBatchSize()!=dto.getBatchSize()){
            dto.setBatchSize(syncDataDto.getBatchSize());
        }

        if(syncDataDto.getDbMapper()!=null&&syncDataDto.getDbMapper().size()>0){
            dto.setDbMapper(syncDataDto.getDbMapper());
        }

        if(!StringUtils.isEmpty(syncDataDto.getFileAddress())){
            dto.setFileAddress(syncDataDto.getFileAddress());
            dto.setSourceRedisAddress(syncDataDto.getFileAddress());
        }

        if(!StringUtils.isEmpty(syncDataDto.getTargetRedisAddress())){
            dto.setTargetRedisAddress(syncDataDto.getTargetRedisAddress());
        }

        if(!StringUtils.isEmpty(syncDataDto.getTargetPassword())){
            dto.setTargetPassword(syncDataDto.getTargetPassword());
        }

        if(!StringUtils.isEmpty(syncDataDto.getFileType())){
            dto.setFileType(syncDataDto.getFileType());
        }

        if(!StringUtils.isEmpty(syncDataDto.getTaskName())){
            dto.setTaskName(syncDataDto.getTaskName());
        }




        RedisFileDataDto nefileDto=new RedisFileDataDto(dto.getMinPoolSize(),dto.getMaxPoolSize(),dto.getMaxWaitTime(),dto.getTimeBetweenEvictionRunsMillis(),
                dto.getIdleTimeRunsMillis(),dto.getDiffVersion(),dto.getPipeline(),dto.getDbMapper());

        BeanUtils.copyProperties(dto,nefileDto);
        updateUri(nefileDto);

        if(StringUtils.isEmpty(syncDataDto.getTaskName())){
            dto.setTaskName(dto.getTaskName());
        }
        dto.setAutostart(syncDataDto.isAutostart());
        dto.setAfresh(dto.isAfresh());

        data.setRedisClusterDto(dto);
        return dto;
    }



    /**
     * 更新uri
     *
     * @param redisClusterDto
     */
    public static void updateUri(RedisClusterDto redisClusterDto) throws TaskMsgException {

        redisClusterDto.setSourceUris(getUrlList(redisClusterDto.getSourceRedisAddress(), redisClusterDto.getSourcePassword()));
        redisClusterDto.setTargetUris(getUrlList(redisClusterDto.getTargetRedisAddress(), redisClusterDto.getTargetPassword()));

        for (String uri : redisClusterDto.getTargetUris()
        ) {
            String redisVersion = null;
            try {
                redisVersion = RedisUrlCheckUtils.selectSyncerVersion(uri);

            } catch (Exception e) {
                redisVersion="0";
            }
            Integer rdbVersion = RedisUrlCheckUtils.getRdbVersion(redisClusterDto.getTargetRedisVersion());
            Integer integer = RedisUrlCheckUtils.getRdbVersion(redisVersion);
            log.warn("自动获取redis版本号：{} ,对应rdb版本号：{},手动输入版本号：{}，对应rdb版本号：{}",redisVersion,integer,redisClusterDto.getTargetRedisVersion(),rdbVersion);
            if (integer == 0) {
                if (rdbVersion == 0) {
//                    throw new TaskMsgException("targetRedisVersion can not be empty /targetRedisVersion error");
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR));
                } else {
                    redisClusterDto.addRedisInfo(new RedisInfo(redisClusterDto.getTargetRedisVersion(), uri, rdbVersion));
                }
            } else {
                redisClusterDto.addRedisInfo(new RedisInfo(redisVersion, uri, RedisUrlCheckUtils.getRdbVersion(redisVersion)));
            }
//            rdbVersion

            redisClusterDto.setTargetRedisVersion(redisVersion);
        }

    }


    public static void updateFileCommandBackUpUri(FileCommandBackupDataDto redisFileDataDto) throws TaskMsgException {

        redisFileDataDto.setSourceUris(getUrlList(redisFileDataDto.getSourceRedisAddress(), redisFileDataDto.getSourcePassword()));

    }



    public static void updateUri(RedisFileDataDto redisFileDataDto) throws TaskMsgException {

        redisFileDataDto.setTargetUris(getUrlList(redisFileDataDto.getTargetRedisAddress(), redisFileDataDto.getTargetPassword()));

        for (String uri : redisFileDataDto.getTargetUris()
        ) {
            String redisVersion = null;
            try {
                redisVersion = RedisUrlCheckUtils.selectSyncerVersion(uri);

            } catch (Exception e) {
                redisVersion="0";
            }
            Integer rdbVersion = RedisUrlCheckUtils.getRdbVersion(redisFileDataDto.getTargetRedisVersion());
            Integer integer = RedisUrlCheckUtils.getRdbVersion(redisVersion);
            log.warn("自动获取redis版本号：{} ,对应rdb版本号：{},手动输入版本号：{}，对应rdb版本号：{}",redisVersion,integer,redisFileDataDto.getTargetRedisVersion(),rdbVersion);
            if (integer == 0) {
                if (rdbVersion == 0) {
//                    throw new TaskMsgException("targetRedisVersion can not be empty /targetRedisVersion error");
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR));
                } else {
                    redisFileDataDto.addRedisInfo(new RedisInfo(redisFileDataDto.getTargetRedisVersion(), uri, rdbVersion));
                }
            } else {
                redisFileDataDto.addRedisInfo(new RedisInfo(redisVersion, uri, RedisUrlCheckUtils.getRdbVersion(redisVersion)));
            }
//            rdbVersion
            redisFileDataDto.setTargetRedisVersion(redisVersion);

        }

    }

    public static void updateUris(RedisClusterDto redisFileDataDto) throws TaskMsgException {

        redisFileDataDto.setTargetUris(getUrlList(redisFileDataDto.getTargetRedisAddress(), redisFileDataDto.getTargetPassword()));

        for (String uri : redisFileDataDto.getTargetUris()
        ) {
            String redisVersion = null;
            try {
                redisVersion = RedisUrlCheckUtils.selectSyncerVersion(uri);

            } catch (Exception e) {
                redisVersion="0";
            }
            Integer rdbVersion = RedisUrlCheckUtils.getRdbVersion(redisFileDataDto.getTargetRedisVersion());
            Integer integer = RedisUrlCheckUtils.getRdbVersion(redisVersion);
            log.warn("自动获取redis版本号：{} ,对应rdb版本号：{},手动输入版本号：{}，对应rdb版本号：{}",redisVersion,integer,redisFileDataDto.getTargetRedisVersion(),rdbVersion);
            if (integer == 0) {
                if (rdbVersion == 0) {
//                    throw new TaskMsgException("targetRedisVersion can not be empty /targetRedisVersion error");
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR));
                } else {
                    redisFileDataDto.addRedisInfo(new RedisInfo(redisFileDataDto.getTargetRedisVersion(), uri, rdbVersion));
                }
            } else {
                redisFileDataDto.addRedisInfo(new RedisInfo(redisVersion, uri, RedisUrlCheckUtils.getRdbVersion(redisVersion)));
            }
//            rdbVersion
            redisFileDataDto.setTargetRedisVersion(redisVersion);

        }

    }
    /**
     * 生成uri集合
     *
     * @param sourceUrls
     * @param password
     * @return
     */
    public synchronized static Set<String> getUrlList(String sourceUrls, String password) {
        Set<String> urlList = new HashSet<>();
        if (StringUtils.isEmpty(sourceUrls)){
            return new HashSet<>();
        }
        String[] sourceUrlsList = sourceUrls.split(";");
        //循环遍历所有的url
        for (String url : sourceUrlsList) {
            StringBuilder stringHead = new StringBuilder("redis://");
            //如果截取出空字符串直接跳过
            if (url != null && url.length() > 0) {
                stringHead.append(url);
                //判断密码是否为空如果为空直接跳过
                if (password != null && password.length() > 0) {
                    stringHead.append("?authPassword=");
                    stringHead.append(password);
                }
                urlList.add(stringHead.toString());

            }
        }
        return urlList;
    }




    public synchronized static List<RedisClusterDto> loadingRedisClusterDto(RedisClusterDto redisClusterDto) throws TaskMsgException {
        String sourceAddress=redisClusterDto.getSourceRedisAddress();



        String[]sourceAdd=sourceAddress.split(";");
        List<RedisClusterDto>res=new ArrayList<>();

        if(RedisType.CLUSTER.equals(RedisType.valueOf(redisClusterDto.getRedistype().toUpperCase()))){


            //        Jedis target = new Jedis("114.67.100.240",8002);


            //获取password
//
//        Object targetAuth = target.auth("redistest0102");
//        String clusternodes=target.clusterNodes();

            for (String data:sourceAdd){
                String[]redisSata=data.split(":");
                Jedis client=new Jedis(redisSata[0], Integer.parseInt(redisSata[1]));

                if(!StringUtils.isEmpty(redisClusterDto.getSourcePassword())){
                    try {
                        Object targetAuth = client.auth(redisClusterDto.getSourcePassword());
                    }catch (JedisDataException e){
                        throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_CONNECT_ERROR_CODE,"源redis链接失败:"+e.getMessage()));
                    }

                }
                try {
                    String remsg=client.clusterNodes();
                    //\w+\s+(.*?)@(.*?)master -
                    List<List<String>>redisDataList= RegexUtil.getSubListUtil(remsg,"\\w+\\s+(.*?)master -",1);
                    System.out.println(JSON.toJSONString(redisDataList));
                    String[]resdata=new String[redisDataList.size()];
                    for (int i = 0; i <redisDataList.size() ; i++) {
                        List<String>oneData=redisDataList.get(i);
                        String[] splitData=oneData.get(0).split(" ");

                        if(splitData.length>=2){
                            resdata[i]=splitData[1].split("@")[0].trim();
                        }else if(splitData.length==1){
                            resdata[i]=splitData[0].trim();
                        }else {
                            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,"计算集群节点失败:"));

                        }

                    }

                    sourceAdd=resdata;
                }catch (JedisDataException e){
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,"获取集群节点失败(请检查当前节点是否为集群节点):"+e.getMessage()));

                }catch (Exception e){
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,"获取集群节点失败:"+e.getMessage()));

                }finally {
                    client.close();
                }

                break;

            }

        }


        for (int i = 0; i < sourceAdd.length; i++) {
            if(sourceAdd[i].indexOf("@")>0){
                sourceAdd[i]=sourceAdd[i].split("@")[0].trim();
            }
        }
        for (String data:sourceAdd){

            if(!StringUtils.isEmpty(data)){
                RedisClusterDto dto = new RedisClusterDto( 100
                        , 110, 10000
                        , 1000, 100000);
                BeanUtils.copyProperties(redisClusterDto,dto);
                dto.setSourceRedisAddress(data);
                res.add(dto);
            }
        }


        if(res.size()==0){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_URI_ERROR,"sourceRedisAddress存在错误"));
        }
        return res;
    }


    public static void main(String[] args) {

    }


}
