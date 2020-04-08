package syncer.syncerservice.filter.redis_start_check_strategy;

import lombok.Builder;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 统一策略不判断是否重复
 * @Date 2020/3/19
 */
@Builder
public class SyncStartCheckNoDistinctStrategyFactory implements IRedisStartCheckStrategyFactory{
    @Override
    public List<IRedisStartCheckBaseStrategy> getStrategyList(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) {
        List<IRedisStartCheckBaseStrategy>startCheckBaseStrategyList=new ArrayList<>();

        startCheckBaseStrategyList.add(RedisStartCheckRedisUrlStrategy.builder().client(client).redisPoolProps(redisPoolProps).taskModel(taskModel).build());
        startCheckBaseStrategyList.add(RedisStartSelectVersionStrategy.builder().client(client).redisPoolProps(redisPoolProps).taskModel(taskModel).build());

        return startCheckBaseStrategyList;
    }
}
