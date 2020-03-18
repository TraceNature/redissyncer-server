package syncer.syncerplusredis.util;

import lombok.Getter;
import lombok.Setter;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.code.CodeUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author zhanenqiang
 * @Description 任务内存状态管理
 * @Date 2020/3/11
 */
public class TaskDataManagerUtils {
    @Getter
    @Setter
    private static Map<String, TaskDataEntity> aliveThreadHashMap=new ConcurrentHashMap<String,TaskDataEntity>();


    /**
     * 添加线程到aliveMap
     * @param threadId
     * @param taskDataEntity
     */
    public synchronized static void addAliveThread(String threadId, TaskDataEntity taskDataEntity) throws Exception {
        if(checkThreadMsg(taskDataEntity)){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKSETTING_ERROR));
        }
        if(!aliveThreadHashMap.containsKey(threadId)){
            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
            taskMapper.insertTask(taskDataEntity.getTaskModel());
            aliveThreadHashMap.put(threadId,taskDataEntity);
        }
    }


    /**
     * 添加线程到aliveMap
     * @param threadId
     */
    public synchronized static void removeThread(String threadId,Long offset) throws Exception {
        if(aliveThreadHashMap.containsKey(threadId)){
            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
            taskMapper.updateTaskStatusById(threadId, TaskStatusType.BROKEN.getCode());
            if(offset!=null&&offset>-1){
                taskMapper.updateTaskOffsetById(threadId,offset);
            }
            aliveThreadHashMap.remove(threadId);
        }
    }


    public synchronized static void removeThread(String threadId) throws Exception {
        if(aliveThreadHashMap.containsKey(threadId)){
            TaskDataEntity data=aliveThreadHashMap.get(threadId);
            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
            taskMapper.updateTaskStatusById(threadId, TaskStatusType.BROKEN.getCode());
            if(data.getOffSetEntity().getReplOffset()!=null&&data.getOffSetEntity().getReplOffset().get()>-1){
                taskMapper.updateTaskOffsetById(threadId,data.getOffSetEntity().getReplOffset().get());
            }
            aliveThreadHashMap.remove(threadId);
        }
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

}
