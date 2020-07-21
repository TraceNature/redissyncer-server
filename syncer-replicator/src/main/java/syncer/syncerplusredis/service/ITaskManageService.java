package syncer.syncerplusredis.service;

import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.model.TaskModel;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/11
 */
public interface ITaskManageService {

    boolean createTaskService(TaskModel model)throws Exception;

    boolean startTaskServiceById(String id, TaskStatusType statusType)throws Exception;

    boolean startTaskServiceByGroupId(String groupId, TaskStatusType statusType)throws Exception;

    boolean stopTaskServiceById(String id, TaskStatusType statusType)throws Exception;

    boolean stopTaskServiceByGroupId(String groupId, TaskStatusType statusType)throws Exception;

    boolean brokenTaskServiceById(String id, TaskStatusType statusType)throws Exception;

    boolean brokenTaskServiceByGroupId(String groupId, TaskStatusType statusType)throws Exception;
}
