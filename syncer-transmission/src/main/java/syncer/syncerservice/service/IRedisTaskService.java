package syncer.syncerservice.service;

import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.entity.RedisStartCheckEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;

/**
 * @author zhanenqiang
 * @Description 多态接口
 * @Date 2020/2/24
 */
public interface IRedisTaskService {

    /**
     * 创建数据同步任务
     * @param redisStartCheckEntity
     * @return
     * @throws TaskMsgException
     */
    String runSyncerTask(RedisStartCheckEntity redisStartCheckEntity, RedisStartCheckTypeEnum redisStartCheckType) throws Exception;


    String runSyncerTask(TaskModel taskModel) throws Exception;


}
