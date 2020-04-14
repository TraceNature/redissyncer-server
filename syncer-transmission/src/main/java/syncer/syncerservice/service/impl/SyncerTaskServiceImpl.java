package syncer.syncerservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerservice.service.IRedisSyncerService;
import syncer.syncerservice.service.ISyncerTaskService;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.SyncTaskUtils;
import syncer.syncerservice.util.TaskCheckUtils;
import syncer.syncerpluscommon.util.file.FileUtils;

import java.io.File;
import java.util.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/6
 */

@Service("syncerTaskService")
@Slf4j
public class SyncerTaskServiceImpl implements ISyncerTaskService {


    @Autowired
    RedisPoolProps redisPoolProps;
    @Autowired
    IRedisSyncerService redisBatchedSyncerService;


    /**
     * 将文件数据刷到目标redis
     * @param redisFileDataDto
     * @return
     * @throws TaskMsgException
     */
    @Override
    public ResultMap createFileToRedisTask(RedisFileDataDto redisFileDataDto) throws TaskMsgException {

        List<String>taskIdList=new ArrayList<>();
        List<String>errorList=new ArrayList<>();
        List<String>addressList=new ArrayList<>();

        if(redisFileDataDto.getFileAddress().indexOf(";")>0){
            addressList= Arrays.asList(redisFileDataDto.getFileAddress().split(";"));
        } else if(redisFileDataDto.getFileAddress().startsWith("http://")||redisFileDataDto.getFileAddress().startsWith("https://")){
            addressList.add(redisFileDataDto.getFileAddress());
        }  else {
            File file=new File(redisFileDataDto.getFileAddress());
            if(file.isDirectory()){
                addressList= FileUtils.getFiles(redisFileDataDto.getFileAddress());
            }else if(file.isFile()){
                addressList.add(redisFileDataDto.getFileAddress());
            }
        }


        for (String fileAdress:addressList
             ) {

            if(StringUtils.isEmpty(fileAdress)){
                continue;
            }

            RedisFileDataDto dataDto=new RedisFileDataDto(redisPoolProps.getMinPoolSize(),
                    redisPoolProps.getMaxPoolSize(),
                    redisPoolProps.getMaxWaitTime(),
                    redisPoolProps.getTimeBetweenEvictionRunsMillis(),
                    redisPoolProps.getIdleTimeRunsMillis(),1,"",redisFileDataDto.getDbMapper());
            BeanUtils.copyProperties(redisFileDataDto,dataDto);
            dataDto.setFileAddress(fileAdress);
            try {
                taskIdList.add(createSingleFileToRedisTask(dataDto));
            }catch (Exception e){
                //失败
//                e.printStackTrace();
//                System.out.println(e.getMessage());
                StringBuilder builder=new StringBuilder("[");
                builder.append(fileAdress);
                builder.append("]");
                builder.append(",message:[");
                builder.append(e.getMessage()).append("]");

                errorList.add(builder.toString());
            }


        }

        HashMap msg=new HashMap(10);
        msg.put("taskids",taskIdList);
        msg.put("errors",errorList);
        return  ResultMap.builder().code("2000").msg("Task created successfully").data(msg);
    }



    /**
     * 创建单个任务
     */
    public String createSingleFileToRedisTask(RedisFileDataDto redisFileDataDto) throws TaskMsgException{
        String threadId= TemplateUtils.uuid();
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
        TaskCheckUtils.updateUri(redisFileDataDto);

        BeanUtils.copyProperties(redisFileDataDto,redisClusterDto);

        redisClusterDto.setSourceRedisAddress(redisFileDataDto.getFileAddress());
        redisClusterDto.setTargetUriData(redisFileDataDto.getTargetUriData());
        redisClusterDto.setTargetUris(redisFileDataDto.getTargetUris());

        try{

            String threadName=redisFileDataDto.getTaskName();
            if(StringUtils.isEmpty(threadName)){
                threadName=threadId;
                redisFileDataDto.setTaskName(threadId);
            }

            ThreadMsgEntity msgEntity=ThreadMsgEntity.builder().id(threadId)
                    .status(ThreadStatusEnum.CREATED)
                    .taskName(threadName)
                    .redisClusterDto(redisClusterDto)
                    .build();

            TaskMsgUtils.addAliveThread(threadId, msgEntity);

            if(redisClusterDto.isAutostart()){
                redisBatchedSyncerService.batchedSync(redisClusterDto,threadId,redisFileDataDto.isAutostart());
                msgEntity.setStatus(ThreadStatusEnum.RUN);
            }else {
                msgEntity.getRedisClusterDto().setAfresh(true);
            }


        }catch (Exception ex){
            throw ex;
        }

        return threadId;
    }





    /**
     * 创建实时命令备份任务
     * @param redisFileDataDto
     * @return
     * @throws TaskMsgException
     */

    @Override
    public ResultMap creatCommandDumpUptask(FileCommandBackupDataDto redisFileDataDto) throws TaskMsgException {

        List<String>taskIdList=new ArrayList<>();
        List<String>errorList=new ArrayList<>();

        redisFileDataDto.setRedisFileDataDto(redisPoolProps.getMinPoolSize(),
                redisPoolProps.getMaxPoolSize(),
                redisPoolProps.getMaxWaitTime(),
                redisPoolProps.getTimeBetweenEvictionRunsMillis(),
                redisPoolProps.getIdleTimeRunsMillis());
        redisFileDataDto.setFileType(FileType.COMMANDDUMPUP);

        String[] addressList=redisFileDataDto.getSourceRedisAddress().split(";");

        File file=new File(redisFileDataDto.getFileAddress());

        if(!file.isDirectory()){
            return  ResultMap.builder().code("1000").msg("fileAddress请填写目录地址");
        }


        if(redisFileDataDto.getFileAddress().indexOf(".")>0){
            return  ResultMap.builder().code("1000").msg("fileAddress请填写目录地址");

        }

        for (String addressData:addressList){
            String dumpAddress= String.valueOf(TaskCheckUtils.getUrlList(addressData,redisFileDataDto.getSourcePassword()).toArray()[0]);
            RedisUrlCheckUtils.getRedisClientConnectState(dumpAddress,addressData);
        }


        String dizhi=redisFileDataDto.getFileAddress();
        for (String addressData:addressList
        ) {
            if(StringUtils.isEmpty(addressData)){
                continue;
            }
            FileCommandBackupDataDto commandBackupDataDto=new FileCommandBackupDataDto(redisPoolProps.getMinPoolSize(),
                    redisPoolProps.getMaxPoolSize(),
                    redisPoolProps.getMaxWaitTime(),
                    redisPoolProps.getTimeBetweenEvictionRunsMillis(),
                    redisPoolProps.getIdleTimeRunsMillis(),1,"",redisFileDataDto.getDbMapper());
            BeanUtils.copyProperties(redisFileDataDto,commandBackupDataDto);

            commandBackupDataDto.setSourceRedisAddress(addressData);

            try {
                taskIdList.add(createSingleRedisToFileTask(commandBackupDataDto,dizhi));
            }catch (Exception e){
                //失败
                e.printStackTrace();
                StringBuilder builder=new StringBuilder("[");
                builder.append(addressData);
                builder.append("]");
                builder.append(",message:[");
                builder.append(e.getMessage()).append("]");

                errorList.add(builder.toString());
            }

        }



        HashMap msg=new HashMap(10);
        msg.put("taskids",taskIdList);
        msg.put("errors",errorList);
        return  ResultMap.builder().code("2000").msg("Task created successfully").data(msg);


    }

    /**
     * 创建单个任务
     */
    public String createSingleRedisToFileTask(FileCommandBackupDataDto redisFileDataDto,String dizhi) throws TaskMsgException{
        RedisClusterDto redisClusterDto=new RedisClusterDto(redisPoolProps.getMinPoolSize(),
                redisPoolProps.getMaxPoolSize(),
                redisPoolProps.getMaxWaitTime(),
                redisPoolProps.getTimeBetweenEvictionRunsMillis(),
                redisPoolProps.getIdleTimeRunsMillis());
        redisClusterDto.setDbMapper(redisFileDataDto.getDbMapper());



        String threadId= TemplateUtils.uuid();

        String addressMenuAddress="";
        if(StringUtils.isEmpty(dizhi)){
            addressMenuAddress=threadId+".aof";
        }else {
            if(addressMenuAddress.endsWith("/")){
                addressMenuAddress=dizhi+threadId+".aof";
            }else {
                addressMenuAddress=dizhi+"/"+threadId+".aof";
            }

        }

        redisClusterDto.setSourceRedisAddress(redisFileDataDto.getSourceRedisAddress());


        redisFileDataDto.setFileAddress(addressMenuAddress);
        redisClusterDto.setTargetRedisAddress(addressMenuAddress);



        TaskCheckUtils.updateFileCommandBackUpUri(redisFileDataDto);

        BeanUtils.copyProperties(redisFileDataDto,redisClusterDto);

        redisClusterDto.setSourceRedisAddress(redisFileDataDto.getSourceRedisAddress());
        redisClusterDto.setSourceUris(TaskCheckUtils.getUrlList(redisFileDataDto.getSourceRedisAddress(),redisFileDataDto.getSourcePassword()));



        String threadName=redisFileDataDto.getTaskName();
        if(StringUtils.isEmpty(threadName)){
            threadName=threadId;
            redisFileDataDto.setTaskName(threadId);
        }

        redisClusterDto.setTaskName(threadName+"【"+redisFileDataDto.getSourceRedisAddress()+"节点】");
        redisFileDataDto.setFileType(FileType.COMMANDDUMPUP);
        ThreadMsgEntity msgEntity=ThreadMsgEntity.builder().id(threadId)
                .status(ThreadStatusEnum.CREATED)
                .taskName(threadName+"【"+redisFileDataDto.getSourceRedisAddress()+"节点】")

                .redisClusterDto(redisClusterDto)
                .build();

        try {
            TaskMsgUtils.addFileAliveThread(threadId, msgEntity);

            if(redisClusterDto.isAutostart()){
                redisBatchedSyncerService.fileCommandBackUpSync(redisClusterDto,threadId);
                msgEntity.setStatus(ThreadStatusEnum.RUN);
            }else {
                msgEntity.getRedisClusterDto().setAfresh(true);
            }

        }catch (TaskMsgException ex){
            msgEntity.setStatus(ThreadStatusEnum.BROKEN);
            throw ex;
        }

        return threadId;
    }




    @Override
    public ResultMap createRedisToRedisTask(RedisClusterDto redisClusterDto) throws TaskMsgException {

        List<RedisClusterDto> redisClusterDtoList=TaskCheckUtils.loadingRedisClusterDto(redisClusterDto);


        List<String>taskIdList=new ArrayList<>();

        List<String>errorList=new ArrayList<>();

        for (RedisClusterDto dto:redisClusterDtoList
        ) {
            TaskCheckUtils.checkRedisClusterDto(dto);

            try {
                taskIdList.add(createSingleRedisToRedisTask(dto));
            }catch (Exception e){
                //失败
//                e.printStackTrace();
//                System.out.println(e.getMessage());
                StringBuilder builder=new StringBuilder("[");
                builder.append(dto.getSourceRedisAddress());
                builder.append("]");
                builder.append(",message:[");
                builder.append(e.getMessage()).append("]");

                errorList.add(builder.toString());
            }

        }


        HashMap msg=new HashMap(10);
        msg.put("taskids",taskIdList);
        msg.put("errors",errorList);
        return  ResultMap.builder().code("2000").msg("Task created successfully").data(msg);


    }



    /**
     * 创建单个任务
     */
    public String createSingleRedisToRedisTask(RedisClusterDto dto) throws TaskMsgException{

        dto= (RedisClusterDto) TaskCheckUtils.ckeckRedisClusterDto(dto,redisPoolProps);

        String threadId= TemplateUtils.uuid();
        String threadName=dto.getTaskName();
        if(StringUtils.isEmpty(threadName)){
            threadName=threadId;
            dto.setTaskName(threadId);
        }

        ThreadMsgEntity  msgEntity=ThreadMsgEntity.builder().id(threadId)
                .status(ThreadStatusEnum.CREATED)
                .taskName(threadName+"【"+dto.getSourceRedisAddress()+"节点】")
                .redisClusterDto(dto)
                .build();


        try {
            TaskMsgUtils.addAliveThread(threadId, msgEntity);
        } catch (Exception e){
            msgEntity.setStatus(ThreadStatusEnum.BROKEN);
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(threadId), e.getMessage());
            } catch (TaskMsgException ex) {

                log.warn("任务Id【{}】任务创建失败 ，失败原因【{}】", threadId, e.getMessage());
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】任务创建失败，停止原因【{}】", threadId, e.getMessage());
            throw  e;
        }


        if(dto.isAutostart()){
            try{
                redisBatchedSyncerService.batchedSync(dto,threadId,dto.isAfresh());
                msgEntity.setStatus(ThreadStatusEnum.RUN);
            }catch (Exception e){
                msgEntity.setStatus(ThreadStatusEnum.BROKEN);
                log.warn("任务Id【{}】任务启动失败 ，失败原因【{}】", threadId, e.getMessage());
                e.printStackTrace();
            }

        }else {
            msgEntity.getRedisClusterDto().setAfresh(true);
        }

        return threadId;
    }


}
