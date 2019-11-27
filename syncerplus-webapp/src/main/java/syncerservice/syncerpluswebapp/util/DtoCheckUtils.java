package syncerservice.syncerpluswebapp.util;

import com.alibaba.fastjson.JSON;
import syncerservice.syncerpluscommon.entity.ResultMap;
import syncerservice.syncerplusredis.constant.TaskMsgConstant;
import syncerservice.syncerplusredis.constant.ThreadStatusEnum;
import syncerservice.syncerplusredis.entity.FileType;
import syncerservice.syncerplusredis.entity.RedisInfo;
import syncerservice.syncerplusredis.entity.RedisPoolProps;
import syncerservice.syncerplusredis.entity.dto.RedisClusterDto;
import syncerservice.syncerplusredis.entity.dto.RedisFileDataDto;
import syncerservice.syncerplusredis.entity.dto.RedisSyncDataDto;
import syncerservice.syncerplusredis.entity.dto.common.SyncDataDto;
import syncerservice.syncerplusredis.entity.dto.task.EditRedisClusterDto;
import syncerservice.syncerplusredis.entity.dto.task.EditRedisFileDataDto;
import syncerservice.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncerservice.syncerplusredis.exception.TaskMsgException;
import syncerservice.syncerplusservice.util.RedisUrlUtils;
import syncerservice.syncerplusredis.util.TaskMsgUtils;
import syncerservice.syncerplusredis.util.code.CodeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.util.*;

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

            if(redisClusterDto.getTasktype().trim().toLowerCase().equals("incrementonly")){
                String incrementtype =redisClusterDto.getOffsetPlace().trim().toLowerCase();
                if(StringUtils.isEmpty(incrementtype)){
                    incrementtype="endbuffer";
                }
                if(!incrementtype.equals("endbuffer")&&!incrementtype.equals("beginbuffer")){
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

        if(syncDataDto.getTargetRedisVersion()!=0L){
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


        if(syncDataDto.getTargetRedisVersion()!=0L){
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
            double redisVersion = 0L;
            try {
                redisVersion = RedisUrlUtils.selectSyncerVersion(uri);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            Integer rdbVersion = RedisUrlUtils.getRdbVersion(redisClusterDto.getTargetRedisVersion());
            Integer integer = RedisUrlUtils.getRdbVersion(redisVersion);
            if (integer == 0) {
                if (rdbVersion == 0) {
//                    throw new TaskMsgException("targetRedisVersion can not be empty /targetRedisVersion error");
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR));
                } else {
                    redisClusterDto.addRedisInfo(new RedisInfo(redisClusterDto.getTargetRedisVersion(), uri, rdbVersion));
                }
            } else {
                redisClusterDto.addRedisInfo(new RedisInfo(redisVersion, uri, RedisUrlUtils.getRdbVersion(redisVersion)));
            }
//            rdbVersion

            redisClusterDto.setTargetRedisVersion(redisVersion);
        }

    }

    public static void updateUri(RedisFileDataDto redisFileDataDto) throws TaskMsgException {

        redisFileDataDto.setTargetUris(getUrlList(redisFileDataDto.getTargetRedisAddress(), redisFileDataDto.getTargetPassword()));

        for (String uri : redisFileDataDto.getTargetUris()
        ) {
            double redisVersion = 0L;
            try {
                redisVersion = RedisUrlUtils.selectSyncerVersion(uri);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            Integer rdbVersion = RedisUrlUtils.getRdbVersion(redisFileDataDto.getTargetRedisVersion());
            Integer integer = RedisUrlUtils.getRdbVersion(redisVersion);
            if (integer == 0) {
                if (rdbVersion == 0) {
//                    throw new TaskMsgException("targetRedisVersion can not be empty /targetRedisVersion error");
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR));
                } else {
                    redisFileDataDto.addRedisInfo(new RedisInfo(redisFileDataDto.getTargetRedisVersion(), uri, rdbVersion));
                }
            } else {
                redisFileDataDto.addRedisInfo(new RedisInfo(redisVersion, uri, RedisUrlUtils.getRdbVersion(redisVersion)));
            }
//            rdbVersion
            redisFileDataDto.setTargetRedisVersion(redisVersion);

        }

    }

    public static void updateUris(RedisClusterDto redisFileDataDto) throws TaskMsgException {

        redisFileDataDto.setTargetUris(getUrlList(redisFileDataDto.getTargetRedisAddress(), redisFileDataDto.getTargetPassword()));

        for (String uri : redisFileDataDto.getTargetUris()
        ) {
            double redisVersion = 0L;
            try {
                redisVersion = RedisUrlUtils.selectSyncerVersion(uri);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            Integer rdbVersion = RedisUrlUtils.getRdbVersion(redisFileDataDto.getTargetRedisVersion());
            Integer integer = RedisUrlUtils.getRdbVersion(redisVersion);
            if (integer == 0) {
                if (rdbVersion == 0) {
//                    throw new TaskMsgException("targetRedisVersion can not be empty /targetRedisVersion error");
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_MSG_ERROR));
                } else {
                    redisFileDataDto.addRedisInfo(new RedisInfo(redisFileDataDto.getTargetRedisVersion(), uri, rdbVersion));
                }
            } else {
                redisFileDataDto.addRedisInfo(new RedisInfo(redisVersion, uri, RedisUrlUtils.getRdbVersion(redisVersion)));
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


    public static void main(String[] args) {


        Map HH = new HashMap();
        HH.put(1, 1);
        ResultMap map = ResultMap.builder().data(HH);

        RedisClusterDto dto = new RedisClusterDto("sourceAddress", "targetAddress", "sourcePassword",
                "targetPassword", "test", 100
                , 110, 10000
                , 1000, 100000, 1, "off");
        dto.setDbMapper(HH);
        System.out.println(JSON.toJSONString(dto));
    }
}
