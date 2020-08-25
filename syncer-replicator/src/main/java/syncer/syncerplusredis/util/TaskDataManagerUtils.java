package syncer.syncerplusredis.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.bean.PageBean;
import syncer.syncerpluscommon.constant.ResultCodeAndMessage;

import syncer.syncerpluscommon.util.TaskCreateRunUtils;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.dao.*;
import syncer.syncerplusredis.entity.StartTaskEntity;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.entity.TaskOffsetEntity;
import syncer.syncerplusredis.entity.dto.task.ListTaskMsgDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.ExpandTaskModel;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.model.TaskModelResult;
import syncer.syncerplusredis.util.code.CodeUtils;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;


/**
 * @author zhanenqiang
 * @Description 任务内存状态管理
 * @Date 2020/3/11
 */
@Slf4j
public class TaskDataManagerUtils {
    @Getter
    @Setter
    private static Map<String, TaskDataEntity> aliveThreadHashMap=new ConcurrentHashMap<String,TaskDataEntity>();
    @Getter
    public static final Map<String, Integer> rdbVersionMap=new ConcurrentHashMap<String,Integer>();

    /**
     *   "2": "6",
     *   "2.6": "6",
     *   "2.8": "6",
     *   "3": "6",
     *   "3.0": "6",
     *   "3.2": "7",
     *   "4.0": "8",
     *   "4": "8",
     *   "5.0": "9",
     *   "5": "9",
     *   "6": "9",
     *   "6.0": "9",
     *   "jimdb_3.2": "6",
     *   "jimdb_4.0": "6",
     *   "jimdb_4.1": "6",
     *   "jimdb_5.0": "6",
     *   "jimdb": "6"
     */
    static {
        rdbVersionMap.put("2",6);
        rdbVersionMap.put("2.6",6);
        rdbVersionMap.put("2.8",6);
        rdbVersionMap.put("3",6);
        rdbVersionMap.put("3.0",6);
        rdbVersionMap.put("3.2",7);
        rdbVersionMap.put("4.0",8);
        rdbVersionMap.put("4",8);
        rdbVersionMap.put("5.0",9);
        rdbVersionMap.put("5",9);
        rdbVersionMap.put("6",9);
        rdbVersionMap.put("6.0",9);

        rdbVersionMap.put("jimdb_3.2",6);
        rdbVersionMap.put("jimdb_4.0",6);
        rdbVersionMap.put("jimdb_4.1",6);
        rdbVersionMap.put("jimdb_5.0",6);
        rdbVersionMap.put("jimdb",6);
    }
    /**
     * 添加线程到内存和库
     * @param threadId
     * @param taskDataEntity
     */
    public synchronized static void addMemAndDbThread(String threadId, TaskDataEntity taskDataEntity) throws Exception {

            if(checkThreadMsg(taskDataEntity)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR));
            }

            if(checkDbThreadMsg(taskDataEntity.getTaskModel().getId())){
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
//            taskMapper.insertTask(taskDataEntity.getTaskModel());
                SqliteOPUtils.insertTask(taskDataEntity.getTaskModel());
            }else {
                //已存在
            }

            if(!aliveThreadHashMap.containsKey(threadId)){
                aliveThreadHashMap.put(threadId,taskDataEntity);
            }

    }

    /**
     * 把任务信息入库
     * @param threadId
     * @param taskModel
     * @throws Exception
     */
    public synchronized static void addDbThread(String threadId, TaskModel taskModel) throws Exception {

            if(checkDbThreadMsg(threadId)){
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
//            taskMapper.insertTask(taskModel);
                boolean status= SqliteOPUtils.insertTask(taskModel);
                if(!status){
                    throw new Exception("任务基本信息持久化失败");
                }
//            TaskOffsetMapper taskOffsetMapper=SpringUtil.getBean(TaskOffsetMapper.class);
//            taskOffsetMapper.insetTaskOffset(TaskOffsetEntity.builder().taskId(taskModel.getTaskId()).replId(taskModel.getReplId()).offset(taskModel.getOffset()).build());
//                SqliteOPUtils.insetTaskOffset(TaskOffsetEntity.builder().taskId(taskModel.getTaskId()).replId(taskModel.getReplId()).offset(taskModel.getOffset()).build());
            }

    }

    /**
     * 把任务信息加入内存
     * @param threadId
     * @param taskDataEntity
     * @throws Exception
     */
    public synchronized static void addMemThread(String threadId, TaskDataEntity taskDataEntity) throws Exception {

            if(checkThreadMsg(taskDataEntity)){
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR));
            }
            if(!aliveThreadHashMap.containsKey(threadId)){
                aliveThreadHashMap.put(threadId,taskDataEntity);
            }

    }

    /**
     * 把任务信息加入内存
     * @param threadId
     * @param taskDataEntity
     * @throws Exception
     */
    public synchronized static void addMemThread(String threadId, TaskDataEntity taskDataEntity,boolean status) throws Exception {

        if(checkThreadMsg(taskDataEntity)&&!status){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR));
        }

        if(status){
            aliveThreadHashMap.put(threadId,taskDataEntity);
        }else {
            if(!aliveThreadHashMap.containsKey(threadId)){
                aliveThreadHashMap.put(threadId,taskDataEntity);
            }
        }

    }

    /**
     *
     * @param threadId
     */
    public synchronized static void removeThread(String threadId, Long offset) throws Exception {

            if(aliveThreadHashMap.containsKey(threadId)){
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
//            taskMapper.updateTaskStatusById(threadId, TaskStatusType.BROKEN.getCode());
                SqliteOPUtils.updateTaskStatusById(threadId, TaskStatusType.BROKEN.getCode());

                if(offset!=null&&offset>-1L){
                    SqliteOPUtils.updateTaskOffsetById(threadId,offset);
//                taskMapper.updateTaskOffsetById(threadId,offset);
                }
                aliveThreadHashMap.remove(threadId);
            }


    }


    /**
     * 更改任务状态
     * @param threadId
     * @param offset
     * @param taskType
     * @throws Exception
     */
    public synchronized static void changeThreadStatus(String threadId, Long offset, TaskStatusType taskType) throws Exception {


            if(aliveThreadHashMap.containsKey(threadId)){
                TaskDataEntity taskDataEntity=aliveThreadHashMap.get(threadId);
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
                SqliteOPUtils.updateTaskStatusById(threadId, taskType.getCode());
//            taskMapper.updateTaskStatusById(threadId, taskType.getCode());
                if(offset!=null&&offset>=-1L){
//                taskMapper.updateTaskOffsetById(threadId,offset);

                    SqliteOPUtils.updateTaskOffsetById(threadId,offset);
                }
                if(taskType.getStatus().equals(ThreadStatusEnum.BROKEN)||taskType.getStatus().equals(ThreadStatusEnum.STOP)){
                    aliveThreadHashMap.remove(threadId);
                    deleteTaskDataByTaskId(threadId);
                    taskDataEntity=null;
                }else {
                    taskDataEntity.getTaskModel().setStatus(taskType.getCode());
                }

            }


    }


    /**
     * brokenThread
     * @param threadId
     * @throws Exception
     */
    public synchronized static void removeThread(String threadId) throws Exception {

            if(aliveThreadHashMap.containsKey(threadId)){
                TaskDataEntity data=aliveThreadHashMap.get(threadId);
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
//            taskMapper.updateTaskStatusById(threadId, TaskStatusType.BROKEN.getCode());
                SqliteOPUtils.updateTaskStatusById(threadId, TaskStatusType.BROKEN.getCode());
                if(data.getOffSetEntity().getReplOffset()!=null&&data.getOffSetEntity().getReplOffset().get()>-1){
                    SqliteOPUtils.updateTaskOffsetById(threadId,data.getOffSetEntity().getReplOffset().get());
//                taskMapper.updateTaskOffsetById(threadId,data.getOffSetEntity().getReplOffset().get());
                }
                aliveThreadHashMap.remove(threadId);
            }

    }




    /**
     * 停止任务
     * @param taskids
     * @return
     * @throws Exception
     */
    public synchronized static List<StartTaskEntity> stopTaskList(List<String> taskids) throws Exception {
        List<StartTaskEntity>result=new ArrayList<>();
        for (String taskId:taskids
             ) {
            if(StringUtils.isEmpty(taskId)){
                continue;
            }
            Lock lock=  TaskCreateRunUtils.getTaskLock(taskId);
            lock.lock();
            try {
                if(aliveThreadHashMap.containsKey(taskId)){

                    try {
                        TaskDataEntity data=aliveThreadHashMap.get(taskId);
                        try {
                            data.getReplicator().close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        changeThreadStatus(taskId,data.getOffSetEntity().getReplOffset().get(),TaskStatusType.STOP);

                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("2000")
                                .taskId(taskId)
                                .msg("Task stopped successfully")
                                .build();
                        result.add(startTaskEntity);

                    }catch (Exception e){
                        e.printStackTrace();
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("1000")
                                .taskId(taskId)
                                .msg("Task stopped fail")
                                .build();
                        result.add(startTaskEntity);
                    }
                }else{
//                TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
//
//                TaskModel taskModel=taskMapper.findTaskById(taskId);
                    TaskModel taskModel=SqliteOPUtils.findTaskById(taskId);
                    if(taskModel!=null){
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("1001")
                                .taskId(taskId)
                                .msg("The current task is not running")
                                .build();
                        result.add(startTaskEntity);
                    }else {
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("1002")
                                .taskId(taskId)
                                .msg("The task does not exist. Please create the task first")
                                .build();
                        result.add(startTaskEntity);
                    }

                }
            }finally {
                lock.unlock();
            }

        }
        return result;
    }


    /**
     * 根据GroupId停止任务
     * @param groupIdList
     * @return
     * @throws Exception
     */
    public synchronized static List<StartTaskEntity> stopTaskListByGroupIds(List<String> groupIdList) throws Exception {
        List<StartTaskEntity>result=new ArrayList<>();

        List<String>groupIds=groupIdList.stream().filter(groupId->!StringUtils.isEmpty(groupId)).collect(Collectors.toList());
//        TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);

        groupIds.forEach(groupId-> {
            try {

//                List<TaskModel> taskModelList = taskMapper.findTaskByGroupId(groupId);
                List<TaskModel> taskModelList =SqliteOPUtils.findTaskByGroupId(groupId);
                taskModelList.forEach(taskModel -> {
                    Lock lock=  TaskCreateRunUtils.getTaskLock(taskModel.getId());
                    lock.lock();
                    try {
                        if (aliveThreadHashMap.containsKey(taskModel.getId())) {
                            TaskDataEntity data = aliveThreadHashMap.get(taskModel.getId());


                            try {
                                try {
                                    data.getReplicator().close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                changeThreadStatus(taskModel.getId(), data.getOffSetEntity().getReplOffset().get(), TaskStatusType.STOP);
                                StartTaskEntity startTaskEntity=StartTaskEntity
                                        .builder()
                                        .code("2000")
                                        .taskId(taskModel.getId())
                                        .msg("Task stopped successfully")
                                        .build();
                                result.add(startTaskEntity);
                            } catch (Exception e) {
                                StartTaskEntity startTaskEntity=StartTaskEntity
                                        .builder()
                                        .code("1000")
                                        .taskId(taskModel.getId())
                                        .msg("Task stopped fail")
                                        .build();
                                result.add(startTaskEntity);
                            }
                        } else {

                            if (taskModel != null) {
                                StartTaskEntity startTaskEntity=StartTaskEntity
                                        .builder()
                                        .code("1001")
                                        .taskId(taskModel.getId())
                                        .msg("The current task is not running")
                                        .build();
                                result.add(startTaskEntity);
                            } else {
                                StartTaskEntity startTaskEntity=StartTaskEntity
                                        .builder()
                                        .code("1002")
                                        .taskId(taskModel.getId())
                                        .msg("The task does not exist. Please create the task first")
                                        .build();
                                result.add(startTaskEntity);
                            }
                        }
                    }finally {
                        lock.unlock();
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        return result;
    }


    /**
     * 启动任务列表
     * @param taskids
     * @return
     * @throws Exception
     */
    public synchronized static Map<String,String> startTaskList(List<String> taskids) throws Exception{
        Map<String,String> result=new HashMap<>();
        for (String taskId:taskids
        ) {
            if(StringUtils.isEmpty(taskId)){
                continue;
            }
            if(aliveThreadHashMap.containsKey(taskId)){
                result.put(taskId,"Task is running");
            }else{
//                TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
//                TaskModel taskModel=taskMapper.findTaskById(taskId);
                TaskModel taskModel=SqliteOPUtils.findTaskById(taskId);
                Lock lock=  TaskCreateRunUtils.getTaskLock(taskId);
                lock.lock();
                try {
                    if(taskModel!=null){

//                    taskMapper.updateTaskStatusById(taskModel.getId(), TaskStatusType.BROKEN.getCode());
                        SqliteOPUtils.updateTaskStatusById(taskModel.getId(), TaskStatusType.BROKEN.getCode());
                        result.put(taskId,"The task does not exist. Please create the task first");
                    }else {

                        result.put(taskId,"The task does not exist. Please create the task first");
                    }

                }finally {
                    lock.unlock();
                }



            }
        }
        return result;
    }

    /**
     * 查询所有任务
     * @return
     * @throws Exception
     */
    public synchronized static List<TaskModelResult> listTaskList() throws Exception {
//        TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
//        List<TaskModel>taskModelList=taskMapper.selectAll();

        List<TaskModel>taskModelList=SqliteOPUtils.selectAll();
        List<TaskModelResult>taskModelResultList=new ArrayList<>();
        if(taskModelList==null||taskModelList.size()==0){
            return taskModelResultList;
        }
        taskModelResultList.addAll(toTaskModelResult(taskModelList));
        return taskModelResultList;

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
            if(TaskDataManagerUtils.containsKey(taskModel.getId())){
                TaskDataEntity taskDataEntity=TaskDataManagerUtils.get(taskModel.getId());

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
            DecimalFormat df = new DecimalFormat("0.00");//格式化小数
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
                rate=1.0;
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
        }catch (Exception e){
            log.error("[{}]最后一次数据流入时间间隔计算失败",taskModel.getId());
        }

        try{
            lastDataCommitIntervalTime=System.currentTimeMillis()-lastCommitTime;
        }catch (Exception e){
            log.error("[{}]最后一次数据流出时间间隔计算失败",taskModel.getId());
        }

        String brokenResult="";
        try {
            brokenResult=JSON.parseObject(taskModel.getExpandJson(), ExpandTaskModel.class).getBrokenReason();

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
                .lastDataUpdateIntervalTime(lastDataUpdateIntervalTime)
                .lastDataCommitIntervalTime(lastDataCommitIntervalTime)
                .rate2Int(rate2Int)
                .build();
      return result;
    }

    /**
     * listtasks
     * @param listTaskMsgDto
     * @return
     * @throws Exception
     */
    public synchronized static PageBean<TaskModelResult> listTaskListByPages(ListTaskMsgDto listTaskMsgDto) throws Exception {
        checklistTaskMsgDto(listTaskMsgDto);
//        TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);

        if("bynames".equals(listTaskMsgDto.getRegulation().trim())){
            List<String>nameList=listTaskMsgDto.getTasknames().stream().filter(name->!name.isEmpty()).collect(Collectors.toList());
            if(nameList.size()==0){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_NAMES_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_NAMES_NOT_EMPTY.getMsg()));
            }

            List<TaskModelResult>listTaskList=new ArrayList<>();
            nameList.forEach(name-> {
                try {
                    listTaskList.addAll(toTaskModelResult(SqliteOPUtils.findTaskBytaskName(name)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            PageBean<TaskModelResult>pageBean=new PageBean<>(1,listTaskList.size(),listTaskList.size(),true);
            pageBean.setItems(listTaskList);
            return pageBean;
        }else if("all".equalsIgnoreCase(listTaskMsgDto.getRegulation().trim())){
            PageHelper.startPage(listTaskMsgDto.getCurrentPage(), listTaskMsgDto.getPageSize());
            List<TaskModel> allItems = SqliteOPUtils.selectAll();        //全部商品
            int countNums = SqliteOPUtils.countItem();            //总记录数
            PageBean<TaskModelResult> pageData = new PageBean<>(listTaskMsgDto.getCurrentPage(), listTaskMsgDto.getPageSize(), countNums);
            pageData.setItems(toTaskModelResult(allItems));
            return pageData ;
        }else if("byids".equalsIgnoreCase(listTaskMsgDto.getRegulation().trim())){

            List<TaskModelResult> resultList=listTaskMsgDto.getTaskids().stream().filter(id->!StringUtils.isEmpty(id)).map(id->{
                try {
                    return toTaskModelResult(SqliteOPUtils.findTaskById(id));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

            PageBean<TaskModelResult>pageBean=new PageBean<>(1,resultList.size(),resultList.size(),true);
            pageBean.setItems(resultList);
            return pageBean;
        }else if("bystatus".equalsIgnoreCase(listTaskMsgDto.getRegulation().trim())){
            if(StringUtils.isEmpty(listTaskMsgDto.getTaskstatus())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_STATUS_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_STATUS_NOT_EMPTY.getMsg()));
            }
            //状态是否正确
            TaskStatusType type=TaskStatusType.getTaskStatusTypeByName(listTaskMsgDto.getTaskstatus());
            if(type==null){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_STATUS_NOT_FIND.getCode(),ResultCodeAndMessage.TASK_STATUS_NOT_FIND.getMsg()));
            }
            List<TaskModelResult>resultList=SqliteOPUtils.findTaskBytaskStatus(type.getCode()).stream().filter(taskModel -> taskModel!=null).map(taskModel ->{
                return toTaskModelResult(taskModel);
            }).collect(Collectors.toList());
            PageBean<TaskModelResult>pageBean=new PageBean<>(1,resultList.size(),resultList.size(),true);
            pageBean.setItems(resultList);
            return pageBean;
        }else if("byGroupIds".equalsIgnoreCase(listTaskMsgDto.getRegulation().trim())){
            List<String>groupIdList=listTaskMsgDto.getGroupIds().stream().filter(groupId->!StringUtils.isEmpty(groupId)).collect(Collectors.toList());
            if(groupIdList.size()==0){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_GROUPID_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_GROUPID_NOT_EMPTY.getMsg()));
            }
            List<TaskModelResult>listTaskList=new ArrayList<>();
            groupIdList.forEach(groupId-> {
                try {
                    listTaskList.addAll(toTaskModelResult(SqliteOPUtils.findTaskByGroupId(groupId)));
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


    /**
     * listtasks
     * @param listTaskMsgDto
     * @return
     * @throws Exception
     */
    public synchronized static List<TaskModelResult> listTaskList(ListTaskMsgDto listTaskMsgDto) throws Exception {
        checklistTaskMsgDto(listTaskMsgDto);

        if("bynames".equals(listTaskMsgDto.getRegulation().trim())){
            List<String>nameList=listTaskMsgDto.getTasknames().stream().filter(name->!name.isEmpty()).collect(Collectors.toList());
            if(nameList.size()==0){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_NAMES_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_NAMES_NOT_EMPTY.getMsg()));
            }

            List<TaskModelResult>listTaskList=new ArrayList<>();
            nameList.forEach(name-> {
                try {
                    listTaskList.addAll(toTaskModelResult(SqliteOPUtils.findTaskBytaskName(name)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return listTaskList;
        }else if("all".equalsIgnoreCase(listTaskMsgDto.getRegulation().trim())){
            return toTaskModelResult(SqliteOPUtils.selectAll());
        }else if("byids".equalsIgnoreCase(listTaskMsgDto.getRegulation().trim())){

            List<TaskModelResult> resultList=listTaskMsgDto.getTaskids().stream().filter(id->!StringUtils.isEmpty(id)).map(id->{
                try {
                    return toTaskModelResult(SqliteOPUtils.findTaskById(id));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            return resultList;
        }else if("bystatus".equalsIgnoreCase(listTaskMsgDto.getRegulation().trim())){
            if(StringUtils.isEmpty(listTaskMsgDto.getTaskstatus())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_STATUS_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_STATUS_NOT_EMPTY.getMsg()));
            }
            //状态是否正确
            TaskStatusType type=TaskStatusType.getTaskStatusTypeByName(listTaskMsgDto.getTaskstatus());
            if(type==null){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_STATUS_NOT_FIND.getCode(),ResultCodeAndMessage.TASK_STATUS_NOT_FIND.getMsg()));
            }
            List<TaskModelResult>resultList=SqliteOPUtils.findTaskBytaskStatus(type.getCode()).stream().filter(taskModel -> taskModel!=null).map(taskModel ->{
                return toTaskModelResult(taskModel);
            }).collect(Collectors.toList());

            return resultList;
        }else if("byGroupIds".equalsIgnoreCase(listTaskMsgDto.getRegulation().trim())){
            List<String>groupIdList=listTaskMsgDto.getGroupIds().stream().filter(groupId->!StringUtils.isEmpty(groupId)).collect(Collectors.toList());
            if(groupIdList.size()==0){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_GROUPID_NOT_EMPTY.getCode(),ResultCodeAndMessage.TASK_GROUPID_NOT_EMPTY.getMsg()));
            }
            List<TaskModelResult>listTaskList=new ArrayList<>();
            groupIdList.forEach(groupId-> {
                try {
                    listTaskList.addAll(toTaskModelResult(SqliteOPUtils.findTaskByGroupId(groupId)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return listTaskList;
        }

        return new ArrayList<>();
    }


    /**
     * 删除任务
     * @param taskIdList
     * @return
     */
    public synchronized static List<StartTaskEntity> removeTask(List<String>taskIdList)throws Exception{
        List<StartTaskEntity>result=new ArrayList<>();
        taskIdList.stream().filter(taskId->taskId!=null).forEach(taskId->{
            Lock lock=  TaskCreateRunUtils.getTaskLock(taskId);
            lock.lock();
            try {
                if(aliveThreadHashMap.containsKey(taskId)){
                    StartTaskEntity startTaskEntity=StartTaskEntity
                            .builder()
                            .code("1001")
                            .taskId(taskId)
                            .msg("task is running,please stop the task first")
                            .build();
                    result.add(startTaskEntity);
                }else {
//                TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
                    try {
                        if( SqliteOPUtils.findTaskById(taskId)!=null){
                            SqliteOPUtils.deleteTaskById(taskId);
                            StartTaskEntity startTaskEntity=StartTaskEntity
                                    .builder()
                                    .code("2000")
                                    .taskId(taskId)
                                    .msg("Delete successful")
                                    .build();
                            result.add(startTaskEntity);

                        }else {
                            StartTaskEntity startTaskEntity=StartTaskEntity
                                    .builder()
                                    .code("1002")
                                    .taskId(taskId)
                                    .msg("Task does not exist")
                                    .build();
                            result.add(startTaskEntity);

                        }

                    } catch (Exception e) {
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("1000")
                                .taskId(taskId)
                                .msg("Delete failed")
                                .build();
                        result.add(startTaskEntity);
                    }

                }
            }finally {
                lock.unlock();
            }


        });
        return result;
    }


    static void deleteTaskDataByTaskId(String taskId){
        try {
            SpringUtil.getBean(AbandonCommandMapper.class).deleteAbandonCommandModelByTaskId(taskId);
            SpringUtil.getBean(BigKeyMapper.class).deleteBigKeyCommandModelByTaskId(taskId);
            SpringUtil.getBean(BigKeyMapper.class).deleteBigKeyCommandModelByTaskId(taskId);
            SpringUtil.getBean(DataMonitorMapper.class).deleteDataMonitorModelByTaskId(taskId);
            SpringUtil.getBean(TaskOffsetMapper.class).delOffsetEntityByTaskId(taskId);
            SpringUtil.getBean(DataCompensationMapper.class).deleteDataCompensationModelByTaskId(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void deleteTaskDataByGroupId(String groupId){
        try {
            SpringUtil.getBean(AbandonCommandMapper.class).deleteAbandonCommandModelByGroupId(groupId);
            SpringUtil.getBean(BigKeyMapper.class).deleteBigKeyCommandModelByGroupId(groupId);
            SpringUtil.getBean(BigKeyMapper.class).deleteBigKeyCommandModelByGroupId(groupId);
            SpringUtil.getBean(DataMonitorMapper.class).deleteDataMonitorModelByGroupId(groupId);
            SpringUtil.getBean(TaskOffsetMapper.class).delOffsetEntityByGroupId(groupId);
            SpringUtil.getBean(DataCompensationMapper.class).deleteDataCompensationModelByGroupId(groupId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据GroupId删除任务
     * @param groupIdList
     * @return
     * @throws Exception
     */
    public static List<StartTaskEntity>removeTaskByGroupId(List<String> groupIdList) throws Exception {
        List<StartTaskEntity>result=new ArrayList<>();
        for (String groupId:groupIdList){
            List<TaskModel>taskModelList=SqliteOPUtils.findTaskByGroupId(groupId);
            for (TaskModel taskModel:taskModelList){
                Lock lock=  TaskCreateRunUtils.getTaskLock(taskModel.getId());
                lock.lock();
                try {
                    if(aliveThreadHashMap.containsKey(taskModel.getId())){
                        StartTaskEntity startTaskEntity=StartTaskEntity
                                .builder()
                                .code("1001")
                                .taskId(taskModel.getId())
                                .groupId(taskModel.getGroupId())
                                .msg("task is running,please stop the task first")
                                .build();
                        result.add(startTaskEntity);

                    }else {

                        try {
                            SqliteOPUtils.deleteTaskById(taskModel.getId());
                            StartTaskEntity startTaskEntity=StartTaskEntity
                                    .builder()
                                    .code("2000")
                                    .taskId(taskModel.getId())
                                    .groupId(taskModel.getGroupId())
                                    .msg("Delete successful")
                                    .build();
                            result.add(startTaskEntity);

                        } catch (Exception e) {
                            StartTaskEntity startTaskEntity=StartTaskEntity
                                    .builder()
                                    .code("1000")
                                    .taskId(taskModel.getId())
                                    .groupId(taskModel.getGroupId())
                                    .msg("Delete failed")
                                    .build();
                            result.add(startTaskEntity);
                        }
                    }
                }finally {
                    lock.unlock();
                }


            }
        }

        return result;
    }


    /**
     * 检查TaskMsgDto
     * @param listTaskMsgDto
     */
    public static void checklistTaskMsgDto(ListTaskMsgDto listTaskMsgDto) throws TaskMsgException {
        if(listTaskMsgDto.getCurrentPage()==0){
            listTaskMsgDto.setCurrentPage(1);
        }
        if(listTaskMsgDto.getPageSize()==0){
            listTaskMsgDto.setPageSize(10);
        }
        //all、bynames、byids、bystatus
        if(!"all".equals(listTaskMsgDto.getRegulation().trim())
                &&!"bynames".equals(listTaskMsgDto.getRegulation().trim())
                && !"byids".equals(listTaskMsgDto.getRegulation().trim())
                && !"bystatus".equals(listTaskMsgDto.getRegulation().trim())
                && !"byGroupIds".equals(listTaskMsgDto.getRegulation().trim())
        ){
//            throw new TaskMsgException("regulation 参数类型错误");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_REGULATION_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_REGULATION_ERROR));
        }
    }

    /**
     * 更新offset
     * @param taskId
     * @throws Exception
     */
    public synchronized static void updateTaskOffset(String taskId)throws Exception{
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity data=aliveThreadHashMap.get(taskId);
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
            if(data.getOffSetEntity().getReplOffset()!=null&&data.getOffSetEntity().getReplOffset().get()>-1L){
                SqliteOPUtils.updateTaskOffsetById(taskId,data.getOffSetEntity().getReplOffset().get());
            }
        }

    }

    /**
     * 更新状态
     * @param taskId
     * @param taskStatusType
     * @throws Exception
     */
    public synchronized static void updateThreadStatusAndMsg(String taskId,String msg,TaskStatusType taskStatusType) throws Exception {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity data=aliveThreadHashMap.get(taskId);
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);

            data.getTaskModel().setStatus(taskStatusType.getCode());
            data.getTaskModel().setTaskMsg(msg);
            SqliteOPUtils.updateTaskMsgAndStatusById( taskStatusType.getCode(),msg,taskId);
            if(taskStatusType.equals(TaskStatusType.BROKEN)||taskStatusType.equals(TaskStatusType.STOP)){
                updateTaskOffset(taskId);
                if(taskStatusType.equals(TaskStatusType.BROKEN)){
                    TaskDataManagerUtils.updateBrokenResult(taskId,msg);
                }else {
                    data.getTaskModel().setTaskMsg("任务主动关闭或停止");
                    TaskDataManagerUtils.updateBrokenResult(taskId,"");
                }

                aliveThreadHashMap.remove(taskId);
            }
//            if(taskStatusType.getStatus().equals(ThreadStatusEnum.BROKEN)
//                    ||taskStatusType.getStatus().equals(ThreadStatusEnum.STOP)){
//                updateTaskOffset(taskId);
//                aliveThreadHashMap.remove(taskId);
//            }
        }
    }

    public synchronized static void updateThreadMsg(String taskId,String msg) throws Exception {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity data=aliveThreadHashMap.get(taskId);
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
            SqliteOPUtils.updateTaskMsgById(msg,taskId);
            data.getTaskModel().setTaskMsg(msg);
        }
    }
    /**
     * 更新状态
     * @param taskId
     * @param taskStatusType
     * @throws Exception
     */
    public synchronized static void updateThreadStatus(String taskId,TaskStatusType taskStatusType) throws Exception {
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity data=aliveThreadHashMap.get(taskId);
//            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
            SqliteOPUtils.updateTaskStatusById(taskId, taskStatusType.getCode());
            data.getTaskModel().setStatus(taskStatusType.getCode());
            if(taskStatusType.getStatus().equals(ThreadStatusEnum.BROKEN)
                    ||taskStatusType.getStatus().equals(ThreadStatusEnum.STOP)){
                updateTaskOffset(taskId);
                aliveThreadHashMap.remove(taskId);
            }
        }
    }

    /**
     * 任务是否关闭或者停止
     * @param taskId
     * @return
     */
    public static synchronized boolean isTaskClose(String taskId){
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskModel taskModel=aliveThreadHashMap.get(taskId).getTaskModel();
            if(taskModel.getStatus().equals(TaskStatusType.BROKEN.getCode())||taskModel.getStatus().equals(TaskStatusType.STOP.getCode())){
                return true;
            }
            return false;
        }

        return true;
    }





    public static boolean containsKey(String key){
        return aliveThreadHashMap.containsKey(key);
    }

    public static TaskDataEntity get(String key){
        return aliveThreadHashMap.get(key);
    }

    /**
     * 检查信息
     * @param threadMsgEntity
     * @return
     */
    public synchronized  static  boolean checkThreadMsg(TaskDataEntity threadMsgEntity){
        AtomicBoolean status= new AtomicBoolean(false);
        aliveThreadHashMap.entrySet().forEach(alive->{
            if(alive.getValue().getTaskModel().getId().equals(threadMsgEntity.getTaskModel().getId())){
                status.set(true);
                return;
            }
        });
        return status.get();
    }


    public synchronized  static  boolean checkDbThreadMsg(String taskId){
        AtomicBoolean status= new AtomicBoolean(false);
//        TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
        try {
           TaskModel taskModel= SqliteOPUtils.findTaskById(taskId);
            if(taskModel==null){
                status.set(true);
            }
        } catch (Exception e) {
            status.set(true);
        }
        return status.get();
    }


    /**
     * 将内存相关字段入库
     * @param taskId
     */
    public synchronized static void updateExpandTaskModel(String taskId){
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity dataEntity=aliveThreadHashMap.get(taskId);
            try {
                SqliteOPUtils.updateExpandTaskModelById(taskId, JSON.toJSONString(dataEntity.getExpandTaskModel()));
            } catch (Exception e) {
                log.error("[{}]更新扩展字段失败",taskId);
                e.printStackTrace();
            }
        }
    }


    /**
     * 更新BrokenResult
     * @param taskId
     * @param brokenResult
     */
    public synchronized static void updateBrokenResult(String taskId,String brokenResult){
        if(aliveThreadHashMap.containsKey(taskId)){
            TaskDataEntity dataEntity=aliveThreadHashMap.get(taskId);
            if(null!=dataEntity.getExpandTaskModel()){
                dataEntity.getTaskModel().setExpandJson(JSON.toJSONString(dataEntity.getExpandTaskModel()));
                dataEntity.getExpandTaskModel().setBrokenReason(brokenResult);
                updateExpandTaskModel(taskId);
            }
        }
    }


}
