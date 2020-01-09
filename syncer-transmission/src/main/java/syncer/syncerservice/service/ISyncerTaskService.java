package syncer.syncerservice.service;

import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.exception.TaskMsgException;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/6
 */
public interface ISyncerTaskService {

    /**
     * 创建文件数据恢复任务
     * @param redisFileDataDto
     * @return
     * @throws TaskMsgException
     */
    ResultMap createFileToRedisTask(RedisFileDataDto redisFileDataDto) throws TaskMsgException;


    /**
     * 创建实时命令备份任务
     * @param redisFileDataDto
     * @return
     * @throws TaskMsgException
     */
    ResultMap creatCommandDumpUptask(FileCommandBackupDataDto redisFileDataDto) throws TaskMsgException;



    ResultMap createRedisToRedisTask(RedisClusterDto redisClusterDto) throws TaskMsgException;

}
