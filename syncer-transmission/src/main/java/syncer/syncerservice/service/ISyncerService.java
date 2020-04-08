package syncer.syncerservice.service;

import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/6
 */
public interface ISyncerService {




    /**
     * 创建实时命令备份任务
     * @param taskModelList
     * @return
     * @throws TaskMsgException
     */
    ResultMap createCommandDumpUptask(List<TaskModel> taskModelList) throws TaskMsgException;


    /**
     * 创建redis数据同步/数据文件恢复
     * @param taskModelList
     * @return
     * @throws TaskMsgException
     */
    ResultMap createRedisToRedisTask(List<TaskModel> taskModelList) throws TaskMsgException;

    /**
     * 批量启动
     * @param taskStartMsgDtoList
     * @return
     * @throws Exception
     */
    ResultMap startSyncerTask(List<TaskStartMsgDto> taskStartMsgDtoList) throws Exception;

    ResultMap startSyncerTaskByGroupId(String groupId,boolean afresh) throws Exception;
}
