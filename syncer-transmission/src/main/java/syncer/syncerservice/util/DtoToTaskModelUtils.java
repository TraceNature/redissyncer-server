package syncer.syncerservice.util;
import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerpluscommon.util.md5.MD5Utils;
import syncer.syncerplusredis.constant.*;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.SyncTypeUtils;
import syncer.syncerpluscommon.util.file.FileUtils;
import syncer.syncerplusredis.util.code.CodeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhanenqiang
 * @Description 用户传入数据转换TaskModelList
 * @Date 2020/3/19
 */
public class DtoToTaskModelUtils {
    /**
     * 获取在线任务列表
     * @param redisClusterDto
     * @return
     */
    public synchronized static List<TaskModel>getTaskModelList(RedisClusterDto redisClusterDto,boolean change){
        List<TaskModel>taskModelList=new ArrayList<>();
        String[] addressList=redisClusterDto.getSourceRedisAddress().split(";");
//        int targetRedisType=1;
//        if(redisClusterDto.getTargetRedisAddress().split(";").length>1){
//            targetRedisType=2;
//        }
        int sourceRedisType=1;
        if(addressList.length>1){
            sourceRedisType=2;
        }

        String taskId=null;




        for (String address:
                addressList) {
            if(StringUtils.isEmpty(address)){
                continue;
            }

            if(change){
                taskId=redisClusterDto.getTaskId();
            }else{
                taskId=TemplateUtils.uuid();
            }

            TaskModel taskModel=TaskModel.builder()
                    .afresh(redisClusterDto.isAfresh())
                    //自动启动
                    .autostart(redisClusterDto.isAutostart())
                    //批次大小
                    .batchSize(redisClusterDto.getBatchSize())
                    //offset
                    .offset(-1L)
                    //id
                    .id(taskId)
                    //taskName
                    .taskName(redisClusterDto.getTaskName())
                    //源地址
                    .sourceRedisAddress(address)
                    //源密码
                    .sourcePassword(redisClusterDto.getSourcePassword())

                    //目标地址
                    .targetRedisAddress(redisClusterDto.getTargetRedisAddress())
                    //目标密码
                    .targetPassword(redisClusterDto.getTargetPassword())
                    //任务状态
                    .status(TaskStatusType.CREATING.getCode())
                    //原目标类型
                    .sourceRedisType(sourceRedisType)
                    //文件地址
                    .fileAddress("")
                    //Redis 6.0 ACL相关
                    .sourceAcl(redisClusterDto.isSourceAcl())
                    .sourceUserName(redisClusterDto.getSourceUserName())
                    .targetAcl(redisClusterDto.isTargetAcl())
                    .targetUserName(redisClusterDto.getTargetUserName())
                    .syncType(SyncTypeUtils.getSyncType(redisClusterDto.getFileType()).getCode())
                    .errorCount(redisClusterDto.getErrorCount())
                    .build();


                    if(redisClusterDto.getDbMapper()!=null){
                        taskModel.setDbMapper(JSON.toJSONString(redisClusterDto.getDbMapper()));
                    }else {
                        taskModel.setDbMapper(JSON.toJSONString(new HashMap<>()));
                    }

                    if("total".equalsIgnoreCase(redisClusterDto.getTasktype())){
                        taskModel.setTasktype(TaskType.TOTAL.getCode());
                    }else if("stockonly".equalsIgnoreCase(redisClusterDto.getTasktype())){
                        taskModel.setTasktype(TaskType.STOCKONLY.getCode());
                    }else if("incrementonly".equalsIgnoreCase(redisClusterDto.getTasktype())){
                        taskModel.setTasktype(TaskType.INCREMENTONLY.getCode());
                    }

                    if("endbuffer".equalsIgnoreCase(redisClusterDto.getOffsetPlace())){
                        taskModel.setOffsetPlace(OffsetPlace.ENDBUFFER.getCode());
                    }else if("beginbuffer".equalsIgnoreCase(redisClusterDto.getOffsetPlace())){
                        taskModel.setOffsetPlace(OffsetPlace.BEGINBUFFER.getCode());
                    }



                    taskModel.setSyncType(getFileType(redisClusterDto.getFileType()));

                    taskModel.setMd5(getTaskMd5(taskModel));
                    taskModelList.add(taskModel);
        }

        return taskModelList;
    }


    /**
     * 获取文件任务列表
     * @param redisFileDataDto
     * @return
     */
    public synchronized static List<TaskModel>getTaskModelList( RedisFileDataDto redisFileDataDto,boolean change){

        List<String>addressList=new ArrayList<>();
        List<TaskModel>taskModelList=new ArrayList<>();

        String taskId=null;




        if(redisFileDataDto.getFileType().equals(SyncType.RDB.getFileType())){
            if(redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
                redisFileDataDto.setFileType(FileType.ONLINERDB);
            }else {
                redisFileDataDto.setFileType(FileType.RDB);
            }
        }

        if(redisFileDataDto.getFileType().equals(SyncType.AOF.getFileType())){
            if(redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
                redisFileDataDto.setFileType(FileType.ONLINEAOF);
            }else {
                redisFileDataDto.setFileType(FileType.AOF);
            }
        }

        if(redisFileDataDto.getFileType().equals(SyncType.MIXED.getFileType())){
            if(redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    redisFileDataDto.getFileAddress().trim().toLowerCase().startsWith("https://")){
                redisFileDataDto.setFileType(FileType.ONLINEMIXED);
            }else {
                redisFileDataDto.setFileType(FileType.MIXED);
            }
        }

        if(redisFileDataDto.getFileAddress().indexOf(";")>0){
            addressList= Arrays.asList(redisFileDataDto.getFileAddress().split(";"));
        } else if(redisFileDataDto.getFileAddress().startsWith("http://")||redisFileDataDto.getFileAddress().startsWith("https://")){
            addressList.add(redisFileDataDto.getFileAddress());
        }  else {
            File file=new File(redisFileDataDto.getFileAddress());
            if(file.isDirectory()){

                addressList=FileUtils.getFiles(redisFileDataDto.getFileAddress()).stream().filter(data->{
                    if(redisFileDataDto.getFileType().equals(FileType.RDB)){
                        if(data.endsWith(".rdb")){
                            return true;
                        }
                    }
                    if(redisFileDataDto.getFileType().equals(FileType.AOF)){
                        if(data.endsWith(".aof")){
                            return true;
                        }
                    }
                    if(redisFileDataDto.getFileType().equals(FileType.MIXED)){
                        if(data.endsWith(".mixed")){
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());

            }else if(file.isFile()){

                addressList.add(redisFileDataDto.getFileAddress());
            }
        }





        for (String address:
                addressList) {
            if(StringUtils.isEmpty(address)){
                continue;
            }

            if(change){
                taskId=redisFileDataDto.getTaskId();
            }else{
                taskId=TemplateUtils.uuid();
            }


            TaskModel taskModel=TaskModel.builder()
                    .afresh(true)
                    //自动启动
                    .autostart(redisFileDataDto.isAutostart())
                    //批次大小
                    .batchSize(redisFileDataDto.getBatchSize())
                    //offset
                    .offset(-1L)
                    //id
                    .id(taskId)
                    //taskName
                    .taskName(redisFileDataDto.getTaskName())
                    //源地址
                    .sourceRedisAddress("")

                    //目标地址
                    .targetRedisAddress(redisFileDataDto.getTargetRedisAddress())
                    //目标密码
                    .targetPassword(redisFileDataDto.getTargetPassword())
                    //任务状态
                    .status(TaskStatusType.CREATING.getCode())
                    //原目标类型 3 file
                    .sourceRedisType(3)
                    //文件地址
                    .fileAddress(address)
                    //Redis 6.0 ACL相关
                    .sourceAcl(redisFileDataDto.isSourceAcl())
                    .sourceUserName(redisFileDataDto.getSourceUserName())
                    .targetAcl(redisFileDataDto.isTargetAcl())
                    .targetUserName(redisFileDataDto.getTargetUserName())
                    .syncType(SyncTypeUtils.getSyncType(redisFileDataDto.getFileType()).getCode())
                    .errorCount(redisFileDataDto.getErrorCount())
                    .build();

            if(redisFileDataDto.getDbMapper()!=null){
                taskModel.setDbMapper(JSON.toJSONString(redisFileDataDto.getDbMapper()));
            }else {
                taskModel.setDbMapper(JSON.toJSONString(new HashMap<>()));
            }

            taskModel.setSyncType(getFileType(redisFileDataDto.getFileType()));
            taskModel.setMd5(getTaskMd5(taskModel));
            taskModelList.add(taskModel);
        }

        return taskModelList;
    }


    /**
     * 实时文件备份
     * @param redisFileDataDto
     * @param change
     * @return
     */
    public synchronized static List<TaskModel>getTaskModelList(FileCommandBackupDataDto redisFileDataDto, boolean change) throws TaskMsgException {
        List<TaskModel>taskModelList=new ArrayList<>();
        String[] addressList=redisFileDataDto.getSourceRedisAddress().split(";");
        String taskId=null;

        if(!redisFileDataDto.getFileType().equals(SyncType.COMMANDDUMPUP.getFileType())){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_SYNCTYPE_ERROR_CODE,TaskMsgConstant.TASK_MSG_SYNCTYPE_ERROR));

        }
        for (String address:
                addressList) {
            if(StringUtils.isEmpty(address)){
                continue;
            }

            if(change){
                taskId=redisFileDataDto.getTaskId();
            }else{
                taskId=TemplateUtils.uuid();
            }

            TaskModel taskModel=TaskModel.builder()
                    .afresh(true)
                    //自动启动
                    .autostart(redisFileDataDto.isAutostart())
                    //批次大小
                    .batchSize(100)
                    //offset
                    .offset(-1L)
                    //id
                    .id(taskId)
                    //taskName
                    .taskName(redisFileDataDto.getTaskName())
                    //源地址
                    .sourceRedisAddress(address)
                    .sourcePassword(redisFileDataDto.getSourcePassword())
                    //目标地址
                    .targetRedisAddress("")
                    //目标密码
                    .targetPassword("")
                    .syncType(SyncType.COMMANDDUMPUP.getCode())
                    //任务状态
                    .status(TaskStatusType.CREATING.getCode())
                    //原目标类型 1 单机
                    .sourceRedisType(1)
                    //文件地址
                    .fileAddress(redisFileDataDto.getFileAddress())
                    //Redis 6.0 ACL相关
                    .sourceAcl(redisFileDataDto.isSourceAcl())
                    .sourceUserName(redisFileDataDto.getSourceUserName())
                    .targetAcl(redisFileDataDto.isTargetAcl())
                    .targetUserName(redisFileDataDto.getTargetUserName())
                    .errorCount(redisFileDataDto.getErrorCount())
                    .build();

                if(redisFileDataDto.getDbMapper()!=null){
                    taskModel.setDbMapper(JSON.toJSONString(redisFileDataDto.getDbMapper()));
                }else {
                    taskModel.setDbMapper(JSON.toJSONString(new HashMap<>()));
                }

                taskModel.setMd5(getTaskMd5(taskModel));
                taskModelList.add(taskModel);
        }

        return taskModelList;
    }


    public synchronized static String getTaskMd5(TaskModel taskModel){
        StringBuilder stringBuilder=new StringBuilder();
        if(!StringUtils.isEmpty(taskModel.getTargetRedisAddress())){
            stringBuilder.append(taskModel.getTargetRedisAddress());
            stringBuilder.append("_");
        }else {
            stringBuilder.append("null");
            stringBuilder.append("_");
        }

        if(!StringUtils.isEmpty(taskModel.getTargetPassword())){
            stringBuilder.append(taskModel.getTargetPassword());
            stringBuilder.append("_");
        }else {
            stringBuilder.append("null");
            stringBuilder.append("_");
        }

        if(!StringUtils.isEmpty(taskModel.getSourceRedisAddress())){
            stringBuilder.append(taskModel.getSourceRedisAddress());
            stringBuilder.append("_");
        }else {
            stringBuilder.append("null");
            stringBuilder.append("_");
        }

        if(!StringUtils.isEmpty(taskModel.getSourcePassword())){
            stringBuilder.append(taskModel.getSourcePassword());
            stringBuilder.append("_");
        }else {
            stringBuilder.append("null");
            stringBuilder.append("_");
        }


        if(!StringUtils.isEmpty(taskModel.getFileAddress())){
            stringBuilder.append(taskModel.getFileAddress());
            stringBuilder.append("_");
        }else {
            stringBuilder.append("null");
            stringBuilder.append("_");
        }

        if(!StringUtils.isEmpty(taskModel.getTaskName())){
            stringBuilder.append(taskModel.getTaskName());
            stringBuilder.append("_");
        }else {
            stringBuilder.append("null");
            stringBuilder.append("_");
        }


        String md5= MD5Utils.getMD5(stringBuilder.toString());
        return md5;
    }




    public synchronized static Integer getFileType(FileType fileType){
        if(FileType.SYNC.equals(fileType)){
         return  SyncType.SYNC.getCode();
        }else if(FileType.RDB.equals(fileType)){
            return SyncType.RDB.getCode();
        }else if(FileType.AOF.equals(fileType)){
            return SyncType.AOF.getCode();
        }else if(FileType.MIXED.equals(fileType)){
            return SyncType.MIXED.getCode();
        }else if(FileType.ONLINERDB.equals(fileType)){
            return SyncType.ONLINERDB.getCode();
        }else if(FileType.ONLINEAOF.equals(fileType)){
            return SyncType.ONLINEAOF.getCode();
        }else if(FileType.ONLINEMIXED.equals(fileType)){
            return  SyncType.ONLINEMIXED.getCode();
        }else if(FileType.COMMANDDUMPUP.equals(fileType)){
            return  SyncType.COMMANDDUMPUP.getCode();
        }
        return  SyncType.SYNC.getCode();
    }


}
