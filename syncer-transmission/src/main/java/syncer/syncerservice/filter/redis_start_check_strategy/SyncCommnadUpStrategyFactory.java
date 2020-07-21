package syncer.syncerservice.filter.redis_start_check_strategy;

import lombok.Builder;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 命令实时备份AOF策略
 * @Date 2020/7/20
 */
@Builder
public class SyncCommnadUpStrategyFactory implements IRedisStartCheckStrategyFactory{

    @Override
    public List<IRedisStartCheckBaseStrategy> getStrategyList(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) {
        List<IRedisStartCheckBaseStrategy>startCheckBaseStrategyList=new ArrayList<>();
        //判断是否重复
        startCheckBaseStrategyList.add(RedisStartDistinctStrategy.builder().client(client).redisPoolProps(redisPoolProps).taskModel(taskModel).build());
        startCheckBaseStrategyList.add(RedisStartCheckRedisUrlStrategy.builder().client(client).redisPoolProps(redisPoolProps).taskModel(taskModel).build());
        return startCheckBaseStrategyList;
    }
}
