package syncer.webapp.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.util.StringUtils;
import syncer.common.exception.TaskMsgException;
import syncer.common.util.MD5Utils;
import syncer.common.util.TemplateUtils;
import syncer.common.util.file.FileUtils;
import syncer.replica.constant.OffsetPlace;
import syncer.replica.entity.FileType;
import syncer.replica.entity.SyncType;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.entity.TaskType;
import syncer.replica.util.SyncTypeUtils;
import syncer.transmission.constants.CommandKeyFilterType;
import syncer.transmission.constants.TaskMsgConstant;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.code.CodeUtils;
import syncer.webapp.request.CreateDumpUpParam;
import syncer.webapp.request.CreateFileTaskParam;
import syncer.webapp.request.CreateTaskParam;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhanenqiang
 * @Description 用户传入数据转换TaskModelList
 * @Date 2020/3/19
 */
public class DtoToTaskModelUtils {

    /**
     * 获取在线任务列表
     * @param param
     * @return
     */

    public synchronized static List<TaskModel> getTaskModelList(CreateTaskParam param, boolean change){
        List<TaskModel>taskModelList= Lists.newArrayList();
        String[] addressList=param.getSourceRedisAddress().split(";");
        int sourceRedisType=taskType(addressList);
        String taskId=null;
        for (String address:addressList) {
            if(StringUtils.isEmpty(address)){
                continue;
            }
            if(change){
                taskId=param.getTaskId();
            }else{
                taskId= TemplateUtils.uuid();
            }
            FileType syncerType=null;

            if(Objects.nonNull(param.getFileType())){
                syncerType=param.getFileType();
            }
            if(Objects.nonNull(param.getSynctype())){
                syncerType=param.getSynctype();
            }
            if(Objects.isNull(syncerType)){
                syncerType=FileType.SYNC;
            }

            String keyFilter=param.getKeyFilter();
            CommandKeyFilterType commandKeyFilterType= param.getFilterType()==null?CommandKeyFilterType.NONE:param.getFilterType();
            TaskModel taskModel=TaskModel.builder()
                    .afresh(param.isAfresh())
                    //自动启动
                    .autostart(param.isAutostart())
                    //批次大小
                    .batchSize(param.getBatchSize())
                    .targetUserName("")
                    .sourceUserName("")
                    //offset
                    .offset(-1L)
                    //id
                    .id(taskId)
                    //taskName
                    .taskName(param.getTaskName())
                    //源地址
                    .sourceRedisAddress(address)
                    //源密码
                    .sourcePassword(param.getSourcePassword())
                    //目标地址
                    .targetRedisAddress(param.getTargetRedisAddress())
                    //目标密码
                    .targetPassword(param.getTargetPassword())
                    //任务状态
                    .status(TaskStatusType.CREATING.getCode())
                    //原目标类型
                    .sourceRedisType(sourceRedisType)
                    //文件地址
                    .fileAddress("")
                    //Redis 6.0 ACL相关
                    .sourceAcl(param.isSourceAcl())
//                    .sourceUserName(param.getSourceUserName())
                    .targetAcl(param.isTargetAcl())
//                    .targetUserName(param.getTargetUserName())
                    .syncType(SyncTypeUtils.getSyncType(syncerType).getCode())
                    .errorCount(param.getErrorCount())
                    .timeDeviation(param.getTimeDeviation())
                    .commandFilter(param.getCommandFilter())
                    .keyFilter(keyFilter)
                    .filterType(commandKeyFilterType)
                    .build();
            if(param.getDbMapper()!=null){
                taskModel.setDbMapper(JSON.toJSONString(param.getDbMapper()));
            }else {
                taskModel.setDbMapper(JSON.toJSONString(new HashMap<>()));
            }
            if("total".equalsIgnoreCase(param.getTasktype())){
                taskModel.setTasktype(TaskType.TOTAL.getCode());
            }else if("stockonly".equalsIgnoreCase(param.getTasktype())){
                taskModel.setTasktype(TaskType.STOCKONLY.getCode());
            }else if("incrementonly".equalsIgnoreCase(param.getTasktype())){
                taskModel.setTasktype(TaskType.INCREMENTONLY.getCode());
            }

            if("endbuffer".equalsIgnoreCase(param.getOffsetPlace())){
                taskModel.setOffsetPlace(OffsetPlace.ENDBUFFER.getCode());
            }else if("beginbuffer".equalsIgnoreCase(param.getOffsetPlace())){
                taskModel.setOffsetPlace(OffsetPlace.BEGINBUFFER.getCode());
            }
            taskModel.setSyncType(getFileType(param.getSynctype()));
            taskModel.setMd5(getTaskMd5(taskModel));
            taskModelList.add(taskModel);
        }
        return taskModelList;
    }


    public synchronized static List<TaskModel>getTaskModelList(CreateFileTaskParam param, boolean change){
        List<TaskModel>taskModelList=Lists.newArrayList();
        List<String>addressList=Lists.newArrayList();
        String taskId=null;
        if(param.getFileType().equals(SyncType.RDB.getFileType())){
            if(param.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    param.getFileAddress().trim().toLowerCase().startsWith("https://")){
                param.setFileType(FileType.ONLINERDB);
            }else {
                param.setFileType(FileType.RDB);
            }
        }

        if(param.getFileType().equals(SyncType.AOF.getFileType())){
            if(param.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    param.getFileAddress().trim().toLowerCase().startsWith("https://")){
                param.setFileType(FileType.ONLINEAOF);
            }else {
                param.setFileType(FileType.AOF);
            }
        }

        if(param.getFileType().equals(SyncType.MIXED.getFileType())){
            if(param.getFileAddress().trim().toLowerCase().startsWith("http://")||
                    param.getFileAddress().trim().toLowerCase().startsWith("https://")){
                param.setFileType(FileType.ONLINEMIXED);
            }else {
                param.setFileType(FileType.MIXED);
            }
        }

        if(param.getFileAddress().indexOf(";")>0){
            addressList= Arrays.asList(param.getFileAddress().split(";"));
        } else if(param.getFileAddress().startsWith("http://")||param.getFileAddress().startsWith("https://")){
            addressList.add(param.getFileAddress());
        }  else {
            File file=new File(param.getFileAddress());
            if(file.isDirectory()){
                addressList= FileUtils.getFiles(param.getFileAddress()).stream().filter(data->{
                    if(param.getFileType().equals(FileType.RDB)){
                        if(data.endsWith(".rdb")){
                            return true;
                        }
                    }
                    if(param.getFileType().equals(FileType.AOF)){
                        if(data.endsWith(".aof")){
                            return true;
                        }
                    }
                    if(param.getFileType().equals(FileType.MIXED)){
                        if(data.endsWith(".mixed")){
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
            }else if(file.isFile()){
                addressList.add(param.getFileAddress());
            }
        }
        for (String address:
                addressList) {
            if(StringUtils.isEmpty(address)){
                continue;
            }

            if(change){
                taskId=param.getTaskId();
            }else{
                taskId=TemplateUtils.uuid();
            }

            TaskModel taskModel=TaskModel.builder()
                    .afresh(true)
                    //自动启动
                    .autostart(param.isAutostart())
                    //批次大小
                    .batchSize(param.getBatchSize())
                    //offset
                    .offset(-1L)
                    //id
                    .id(taskId)
                    //taskName
                    .taskName(param.getTaskName())
                    //源地址
                    .sourceRedisAddress("")
                    //目标地址
                    .targetRedisAddress(param.getTargetRedisAddress())
                    //目标密码
                    .targetPassword(param.getTargetPassword())
                    //任务状态
                    .status(TaskStatusType.CREATING.getCode())
                    //原目标类型 3 file
                    .sourceRedisType(3)
                    //文件地址
                    .fileAddress(address)
                    //Redis 6.0 ACL相关
                    .sourceAcl(param.isSourceAcl())
                    .sourceUserName(param.getSourceUserName())
                    .targetAcl(param.isTargetAcl())
                    .targetUserName(param.getTargetUserName())
                    .syncType(SyncTypeUtils.getSyncType(param.getFileType()).getCode())
                    .errorCount(param.getErrorCount())
                    .timeDeviation(param.getTimeDeviation())
                    .build();

            if(param.getDbMapper()!=null){
                taskModel.setDbMapper(JSON.toJSONString(param.getDbMapper()));
            }else {
                taskModel.setDbMapper(JSON.toJSONString(new HashMap<>()));
            }

            taskModel.setSyncType(getFileType(param.getFileType()));
            taskModel.setMd5(getTaskMd5(taskModel));
            taskModelList.add(taskModel);
        }

        return taskModelList;
    }


    /**
     * 根据createDumpup param生成List<TaskModel>
     * @param param
     * @param change
     * @return
     */
    public synchronized static List<TaskModel> getTaskModelList(CreateDumpUpParam param, boolean change) throws TaskMsgException {
        List<TaskModel>taskModelList=Lists.newArrayList();
        String[]addressList=param.getSourceRedisAddress().split(";");
        if(!param.getFileType().equals(SyncType.COMMANDDUMPUP.getFileType())){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_SYNCTYPE_ERROR_CODE, TaskMsgConstant.TASK_MSG_SYNCTYPE_ERROR));
        }
        String taskId=null;
        for (String address:addressList) {
            if(StringUtils.isEmpty(address)){
                continue;
            }
            if(change){
                taskId=param.getTaskId();
            }else {
                taskId=TemplateUtils.uuid();
            }
            TaskModel taskModel=TaskModel.builder()
                    .afresh(true)
                    //自动启动
                    .autostart(param.isAutostart())
                    //批次大小
                    .batchSize(100)
                    //offset
                    .offset(-1L)
                    //id
                    .id(taskId)
                    //taskName
                    .taskName(param.getTaskName())
                    //源地址
                    .sourceRedisAddress(address)
                    .sourcePassword(param.getSourcePassword())
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
                    .fileAddress(param.getFileAddress())
                    //Redis 6.0 ACL相关
                    .sourceAcl(param.isSourceAcl())
                    .sourceUserName(param.getSourceUserName())
                    .targetAcl(param.isTargetAcl())
                    .targetUserName(param.getTargetUserName())
                    .errorCount(param.getErrorCount())
                    .build();
            if(param.getDbMapper()!=null){
                taskModel.setDbMapper(JSON.toJSONString(param.getDbMapper()));
            }else {
                taskModel.setDbMapper(JSON.toJSONString(Maps.newHashMap()));
            }
            taskModel.setMd5(getTaskMd5(taskModel));
            taskModelList.add(taskModel);
        }

        return taskModelList;
    }

    /**
     * 判断任务类型
     * @param addressList
     * @return
     */
    static int taskType(String[] addressList){
        int sourceRedisType=1;
        if(addressList.length>1){
            sourceRedisType=2;
        }
        return sourceRedisType;
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
