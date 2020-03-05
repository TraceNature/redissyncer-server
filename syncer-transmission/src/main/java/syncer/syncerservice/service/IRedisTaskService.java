package syncer.syncerservice.service;

import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.entity.RedisStartCheckEntity;
import syncer.syncerplusredis.exception.TaskMsgException;

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
    ResultMap runSyncerTask(RedisStartCheckEntity redisStartCheckEntity, RedisStartCheckTypeEnum redisStartCheckType) throws TaskMsgException;




}
