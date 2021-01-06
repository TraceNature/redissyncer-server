package syncer.transmission.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import syncer.common.bean.PageBean;
import syncer.common.constant.ResultCodeAndMessage;
import syncer.common.exception.TaskMsgException;
import syncer.common.util.TemplateUtils;
import syncer.replica.entity.SyncType;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.util.SyncTypeUtils;
import syncer.transmission.constants.TaskMsgConstant;
import syncer.transmission.entity.OffSetEntity;
import syncer.transmission.entity.StartTaskEntity;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.ExpandTaskModel;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.ListTaskParamDto;
import syncer.transmission.po.TaskModelResult;
import syncer.transmission.service.ISingleTaskService;
import syncer.transmission.service.ITaskGroupService;
import syncer.transmission.util.code.CodeUtils;
import syncer.transmission.util.lock.TaskRunUtils;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
@Service
@Slf4j
public class TaskGroupServiceImpl implements ITaskGroupService {
    @Autowired
    ISingleTaskService singleTaskService;
    /**
     * @param taskModelList
     * @return
     * @throws TaskMsgException
     */
    @Override
    public  List<StartTaskEntity> createRedisToRedisTask(List<TaskModel> taskModelList) throws TaskMsgException {
        List<StartTaskEntity>resultList= Lists.newArrayList();
        String groupId=getGroupId(taskModelList);
        if(Objects.nonNull(taskModelList)&&taskModelList.size()>0){
            for (TaskModel taskModel : taskModelList) {
                Lock lock=  TaskRunUtils.getTaskLock(taskModel.getId());
                lock.lock();
                try{
                    taskModel.setGroupId(groupId);
                    taskModel.setStatus(TaskStatusType.STOP.getCode());
                    SingleTaskDataManagerUtils.addDbThread(taskModel.getId(),taskModel);
                    if(taskModel.isAutostart()){

                        TaskModel testTaskModel=new TaskModel();
                        BeanUtils.copyProperties(taskModel,testTaskModel);
                        testTaskModel.setStatus(TaskStatusType.CREATING.getCode());
                        TaskDataEntity dataEntity=TaskDataEntity.builder()
                                .taskModel(testTaskModel)
                                .offSetEntity(OffSetEntity.builder().replId("").build())
                                .build();
                        dataEntity.getOffSetEntity().getReplOffset().set(-1L);
                        SingleTaskDataManagerUtils.addMemThread(taskModel.getId(),dataEntity);

                        String id=singleTaskService.runSyncerTask(taskModel);


                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("2000")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("Task created successfully and entered running state")
                                .build();
                        resultList.add(startTaskEntity);

                    }else {
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("2000")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("Task created successfully")
                                .build();
                        resultList.add(startTaskEntity);
                        SingleTaskDataManagerUtils.updateThreadStatus(taskModel.getId(), TaskStatusType.STOP);
                    }


                }catch (Exception e){
                    try {
                        SingleTaskDataManagerUtils.brokenTask(taskModel.getId());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    log.error("taskId[{}],error[{}]",taskModel.getId(),e.getMessage());
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1000")
                            .taskId(taskModel.getId())
                            .groupId(taskModel.getGroupId())
                            .msg("Error_"+e.getMessage())
                            .build();
                    resultList.add(startTaskEntity);
                }finally {
                    lock.unlock();
                }
            }
        }
        return resultList;
    }


    @Override
    public List<StartTaskEntity> batchStopTaskListByTaskIdList(List<String> taskIdList) throws TaskMsgException {
        List<StartTaskEntity>result=Lists.newArrayList();
        List<String>taskIds=taskIdList.stream()
                .filter(taskId->!StringUtils.isEmpty(taskId))
                .collect(Collectors.toList());
        taskIds.forEach(taskId-> {
            result.add(singleTaskService.stopTaskListByTaskId(taskId));
        });
        return result;
    }


    @Override
    public List<StartTaskEntity> batchStopTaskListByGroupIdList(List<String> groupIdList) throws TaskMsgException {
        List<StartTaskEntity>result=Lists.newArrayList();
        List<String>groupIds=groupIdList.stream()
                .filter(groupId->!StringUtils.isEmpty(groupId))
                .collect(Collectors.toList());

        groupIds.forEach(groupId-> {
            result.addAll(singleTaskService.stopTaskListByGroupId(groupId));
        });
        return result;
    }


    @Override
    public List<StartTaskEntity> batchStartTaskListByGroupId(String groupId, boolean afresh) throws TaskMsgException {
        List<StartTaskEntity>resultList=Lists.newArrayList();
        List<TaskModel>taskModelList=null;
        try {
            taskModelList= SqlOPUtils.findTaskByGroupId(groupId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(Objects.isNull(taskModelList)){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.ERROR_CODE,"GroupId不存在"));
        }

        for (TaskModel taskModel : taskModelList) {
            Lock lock=  TaskRunUtils.getTaskLock(taskModel.getId());
            lock.lock();
            try{
                if(!SingleTaskDataManagerUtils.isTaskClose(taskModel.getId())){
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1001")
                            .taskId(taskModel.getId())
                            .msg("The task is running")
                            .build();
                    resultList.add(startTaskEntity);
                    continue;
                }
                if(afresh!=taskModel.isAfresh()){
                    try {
                        SqlOPUtils.updateAfreshsetById(taskModel.getId(),afresh);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())){
                    String id=singleTaskService.runSyncerCommandDumpUpTask(taskModel);
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("2000")
                            .taskId(id)
                            .msg("OK")
                            .build();
                    resultList.add(startTaskEntity);

                }else {
                    String id=singleTaskService.runSyncerTask(taskModel);
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("2000")
                            .taskId(id)
                            .msg("OK")
                            .build();
                    resultList.add(startTaskEntity);
                }

            }catch(Exception e){
                StartTaskEntity startTaskEntity=StartTaskEntity
                        .builder()
                        .code("1000")
                        .taskId(taskModel.getId())
                        .msg("Error_"+e.getMessage())
                        .build();
                resultList.add(startTaskEntity);
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }

        return resultList;
    }

    @Override
    public StartTaskEntity startTaskByTaskId(String taskId, boolean afresh) throws Exception {
        return singleTaskService.startTaskByTaskId(taskId,afresh);
    }

    /**
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public PageBean<TaskModelResult> listTaskListByPages(ListTaskParamDto param) throws Exception {
        if("bynames".equals(param.getRegulation().trim())){
            List<String>nameList=param.getTasknames().stream().filter(name->!name.isEmpty()).collect(Collectors.toList());
            if(nameList.size()==0){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_NAMES_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_NAMES_NOT_EMPTY.getMsg()));
            }
            List<TaskModelResult>listTaskList=new ArrayList<>();
            nameList.forEach(name-> {
                try {
                    listTaskList.addAll(toTaskModelResult(SqlOPUtils.findTaskBytaskName(name)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            PageBean<TaskModelResult>pageBean=new PageBean<>(1,listTaskList.size(),listTaskList.size(),true);
            pageBean.setItems(listTaskList);
            return pageBean;
        }else if("all".equalsIgnoreCase(param.getRegulation().trim())){
            PageHelper.startPage(param.getCurrentPage(), param.getPageSize());
            List<TaskModel> allItems = SqlOPUtils.selectAll();        //全部商品
            int countNums = SqlOPUtils.countItem();            //总记录数
            PageBean<TaskModelResult> pageData = new PageBean<>(param.getCurrentPage(), param.getPageSize(), countNums);
            pageData.setItems(toTaskModelResult(allItems));
            return pageData ;
        }else if("byids".equalsIgnoreCase(param.getRegulation().trim())){
            List<TaskModelResult> resultList=param.getTaskids().stream().filter(id->!StringUtils.isEmpty(id)).map(id->{
                try {
                    return toTaskModelResult(SqlOPUtils.findTaskById(id));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            PageBean<TaskModelResult>pageBean=new PageBean<>(1,resultList.size(),resultList.size(),true);
            pageBean.setItems(resultList);
            return pageBean;
        }else if("bystatus".equalsIgnoreCase(param.getRegulation().trim())){
            if(StringUtils.isEmpty(param.getTaskstatus())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_STATUS_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_STATUS_NOT_EMPTY.getMsg()));
            }
            //状态是否正确
            TaskStatusType type=TaskStatusType.getTaskStatusTypeByName(param.getTaskstatus());
            if(type==null){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_STATUS_NOT_FIND.getCode(), ResultCodeAndMessage.TASK_STATUS_NOT_FIND.getMsg()));
            }
            List<TaskModelResult>resultList=SqlOPUtils.findTaskBytaskStatus(type.getCode()).stream().filter(taskModel -> taskModel!=null).map(taskModel ->{
                return toTaskModelResult(taskModel);
            }).collect(Collectors.toList());
            PageBean<TaskModelResult>pageBean=new PageBean<>(1,resultList.size(),resultList.size(),true);
            pageBean.setItems(resultList);
            return pageBean;
        }else if("byGroupIds".equalsIgnoreCase(param.getRegulation().trim())){
            List<String>groupIdList=param.getGroupIds().stream().filter(groupId->!StringUtils.isEmpty(groupId)).collect(Collectors.toList());
            if(groupIdList.size()==0){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_GROUPID_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_GROUPID_NOT_EMPTY.getMsg()));
            }
            List<TaskModelResult>listTaskList=new ArrayList<>();
            groupIdList.forEach(groupId-> {
                try {
                    listTaskList.addAll(toTaskModelResult(SqlOPUtils.findTaskByGroupId(groupId)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            PageBean<TaskModelResult>pageBean=new PageBean<>(1,listTaskList.size(),listTaskList.size(),true);
            pageBean.setItems(listTaskList);
            return pageBean;
        }
        return null;
    }

    @Override
    public List<TaskModelResult> listTaskList(ListTaskParamDto param) throws Exception {
        if("bynames".equals(param.getRegulation().trim())){
            List<String>nameList=param.getTasknames().stream().filter(name->!name.isEmpty()).collect(Collectors.toList());
            if(nameList.size()==0){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_NAMES_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_NAMES_NOT_EMPTY.getMsg()));
            }

            List<TaskModelResult>listTaskList=new ArrayList<>();
            nameList.forEach(name-> {
                try {
                    listTaskList.addAll(toTaskModelResult(SqlOPUtils.findTaskBytaskName(name)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return listTaskList;
        }else if("all".equalsIgnoreCase(param.getRegulation().trim())){
            return toTaskModelResult(SqlOPUtils.selectAll());
        }else if("byids".equalsIgnoreCase(param.getRegulation().trim())){

            List<TaskModelResult> resultList=param.getTaskids().stream().filter(id->!StringUtils.isEmpty(id)).map(id->{
                try {
                    return toTaskModelResult(SqlOPUtils.findTaskById(id));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            return resultList;
        }else if("bystatus".equalsIgnoreCase(param.getRegulation().trim())){
            if(StringUtils.isEmpty(param.getTaskstatus())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_STATUS_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_STATUS_NOT_EMPTY.getMsg()));
            }
            //状态是否正确
            TaskStatusType type=TaskStatusType.getTaskStatusTypeByName(param.getTaskstatus());
            if(type==null){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_STATUS_NOT_FIND.getCode(),ResultCodeAndMessage.TASK_STATUS_NOT_FIND.getMsg()));
            }
            List<TaskModelResult>resultList=SqlOPUtils.findTaskBytaskStatus(type.getCode()).stream().filter(taskModel -> taskModel!=null).map(taskModel ->{
                return toTaskModelResult(taskModel);
            }).collect(Collectors.toList());

            return resultList;
        }else if("byGroupIds".equalsIgnoreCase(param.getRegulation().trim())){
            List<String>groupIdList=param.getGroupIds().stream().filter(groupId->!StringUtils.isEmpty(groupId)).collect(Collectors.toList());
            if(groupIdList.size()==0){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_GROUPID_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_GROUPID_NOT_EMPTY.getMsg()));
            }
            List<TaskModelResult>listTaskList=new ArrayList<>();
            groupIdList.forEach(groupId-> {
                try {
                    listTaskList.addAll(toTaskModelResult(SqlOPUtils.findTaskByGroupId(groupId)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return listTaskList;
        }

        return Lists.newArrayList();
    }

    @Override
    public List<StartTaskEntity> removeTaskByGroupIdList(List<String> groupIdList) throws Exception {
        List<StartTaskEntity> result=Lists.newArrayList();
        for (String groupId:groupIdList){
            result.addAll(singleTaskService.removeTaskByGroupId(groupId));
        }
        return result;
    }

    @Override
    public List<StartTaskEntity> removeTaskByTaskIdList(List<String> taskIdList) throws Exception {
        List<StartTaskEntity> result=Lists.newArrayList();
        for (String taskId:taskIdList){
            Lock lock=  TaskRunUtils.getTaskLock(taskId);
            lock.lock();
            try {
                result.add(singleTaskService.removeTaskByTaskId(taskId));
            }finally {
                lock.unlock();
            }

        }
        return result;
    }


    /**
     * 返回值转换
     * @param taskModelList
     * @return
     * @throws Exception
     */
    public synchronized static List<TaskModelResult> toTaskModelResult(List<TaskModel>taskModelList) throws Exception {
        List<TaskModelResult>taskModelResultList=taskModelList.stream()
                .filter(taskModel -> taskModel!=null)
                .map(taskModel -> toTaskModelResult(taskModel))
                .collect(Collectors.toList());
        return taskModelResultList;
    }

    /**
     * 对象转换
     * @param taskModel
     * @return
     */
    public synchronized static TaskModelResult toTaskModelResult(TaskModel taskModel){

        Long allKeyCount=taskModel.getAllKeyCount();
        Long realKeyCount=taskModel.getAllKeyCount();
        Long lastTime=taskModel.getLastKeyUpdateTime();
        Long lastCommitTime=taskModel.getLastKeyCommitTime();

        Long commandKeyCount= 0L;
        double rate=0.0;
        Integer rate2Int=0;
        try{
            if(SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskModel.getId())){
                TaskDataEntity taskDataEntity=SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId());

                lastTime=taskDataEntity.getTaskModel().getLastKeyUpdateTime();
                lastCommitTime=taskDataEntity.getTaskModel().getLastKeyCommitTime();

                if(taskDataEntity.getAllKeyCount().get()!=0L){
                    allKeyCount=taskDataEntity.getAllKeyCount().get();
                }
                if(taskDataEntity.getRealKeyCount().get()!=0L){
                    realKeyCount=taskDataEntity.getRealKeyCount().get();
                }
            }
            commandKeyCount=allKeyCount-taskModel.getRdbKeyCount();
            //格式化小数
            DecimalFormat df = new DecimalFormat("0.00");
            df.setMaximumFractionDigits(2);
            df.setGroupingSize(0);
            df.setRoundingMode(RoundingMode.FLOOR);
            if(taskModel.getRdbKeyCount()!=0){
                rate=(float)allKeyCount/(float)taskModel.getRdbKeyCount();
                if(rate>1.0){
                    rate=1.0;
                }

            }else {
                rate=0.0;
            }

            if(allKeyCount>=taskModel.getRdbKeyCount()||taskModel.getStatus().equals(TaskStatusType.COMMANDRUNING.getCode())){
                if(!taskModel.getSyncType().equals(SyncType.SYNC.getCode())){
                    rate=0.0;
                }else{
                    rate=1.0;
                }

            }
            if(allKeyCount==0&&taskModel.getRdbKeyCount()==0){
                rate=0.0;
            }
            rate2Int=Integer.parseInt(new DecimalFormat("0").format(rate*100));

        }catch (Exception e){
            log.warn("[{}]进度计算失败",taskModel.getId());
        }

        //最后一次数据间隔时间计算
        long lastDataUpdateIntervalTime=0L;
        long lastDataCommitIntervalTime=0L;
        try{
            lastDataUpdateIntervalTime=System.currentTimeMillis()-lastTime;
            if(lastTime==0L){
                lastDataUpdateIntervalTime=-1L;
            }
        }catch (Exception e){
            log.error("[{}]最后一次数据流入时间间隔计算失败",taskModel.getId());
        }

        try{
            lastDataCommitIntervalTime=System.currentTimeMillis()-lastCommitTime;
            if(lastCommitTime==0L){
                lastDataCommitIntervalTime=-1L;
            }
        }catch (Exception e){
            log.error("[{}]最后一次数据流出时间间隔计算失败",taskModel.getId());
        }

        String brokenResult="";
        try {
            brokenResult= JSON.parseObject(taskModel.getExpandJson(), ExpandTaskModel.class).getBrokenReason();

        }catch (Exception e){
            log.error("brokenResult获取失败");
            e.printStackTrace();
        }

        TaskModelResult result=  TaskModelResult
                .builder()
                .taskId(taskModel.getId())
                .afresh(taskModel.isAfresh())
                .autostart(taskModel.isAutostart())
                .batchSize(taskModel.getBatchSize())
                .fileAddress(taskModel.getFileAddress())
                .groupId(taskModel.getGroupId())
                .offset(taskModel.getOffset())
                .taskName(taskModel.getTaskName())
                .taskMsg(taskModel.getTaskMsg())
                .brokenReason(brokenResult)
                .tasktype(SyncTypeUtils.getTaskType(taskModel.getTasktype()))
                .syncType(SyncTypeUtils.getSyncType(taskModel.getSyncType()))
                .sourceRedisAddress(taskModel.getSourceRedisAddress())
                .targetRedisAddress(taskModel.getTargetRedisAddress())
                .redisVersion(taskModel.getRedisVersion())
                .rdbVersion(taskModel.getRdbVersion())
                .offsetPlace(SyncTypeUtils.getOffsetPlace(taskModel.getOffsetPlace()))
                .sourceRedisType(SyncTypeUtils.getRedisBranchType(taskModel.getSourceRedisType()))
                .targetRedisType(SyncTypeUtils.getRedisBranchType(taskModel.getTargetRedisType()))
                .status(SyncTypeUtils.getTaskStatusType(taskModel.getStatus()))
                .dbMapper(taskModel.getDbMapping())
                .analysisMap(taskModel.getDataAnalysis())
                .createTime(taskModel.getCreateTime())
                .updateTime(taskModel.getUpdateTime())
                .replId(taskModel.getReplId())
                .rdbKeyCount(taskModel.getRdbKeyCount())
                .allKeyCount(allKeyCount)
                .realKeyCount(realKeyCount)
                .commandKeyCount(commandKeyCount)
                .rate(rate)
                .lastDataInPutInterval(lastDataUpdateIntervalTime)
                .lastDataOutPutInterval(lastDataCommitIntervalTime)
                .rate2Int(rate2Int)
                .build();
        return result;
    }


    /**
     * 根据taskmodelList获取groupId
     * 规则：size()==1 taskId
     *       size()>1  uuid()
     * @param taskModelList
     * @return
     */
    String getGroupId(List<TaskModel> taskModelList){
        String groupId=null;
        if(Objects.isNull(taskModelList)){
            groupId = TemplateUtils.uuid();
        }
        if(taskModelList.size()==1){
            groupId = taskModelList.get(0).getId();
        }else {
            groupId = TemplateUtils.uuid();
        }

        return groupId;
    }
}
