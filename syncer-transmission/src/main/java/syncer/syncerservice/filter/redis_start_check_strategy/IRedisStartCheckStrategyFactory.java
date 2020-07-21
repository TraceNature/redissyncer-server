package syncer.syncerservice.filter.redis_start_check_strategy;

import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/19
 */
public interface IRedisStartCheckStrategyFactory {
    List<IRedisStartCheckBaseStrategy> getStrategyList(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps);
}
