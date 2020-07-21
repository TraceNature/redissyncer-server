package syncer.syncerservice.filter.redis_start_check_strategy;

import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.RedisStartCheckEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/25
 */
public interface IRedisStartCheckBaseStrategy {

    void run(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) throws Exception;

    void toNext(JDRedisClient client, TaskModel taskModel,RedisPoolProps redisPoolProps) throws Exception;

    void setNext(IRedisStartCheckBaseStrategy nextStrategy);

}
