package syncer.transmission.strategy.taskcheck.factory;

import com.google.common.collect.Lists;
import lombok.Builder;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategyFactory;
import syncer.transmission.strategy.taskcheck.impl.TaskCheckRedisUrlStrategy;
import syncer.transmission.strategy.taskcheck.impl.TaskDistinctStrategy;
import syncer.transmission.strategy.taskcheck.impl.TaskSelectVersionStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 统一策略
 * @Date 2020/12/15
 */
@Builder
public class TaskCheckStrategyFactory implements ITaskCheckStrategyFactory {
    @Override
    public List<ITaskCheckStrategy> getStrategyList(RedisClient client, TaskModel taskModel) {
        List<ITaskCheckStrategy>startCheckBaseStrategyList= Lists.newArrayList();
        //判断是否重复
        startCheckBaseStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());

        startCheckBaseStrategyList.add(TaskCheckRedisUrlStrategy.builder().client(client).taskModel(taskModel).build());
        startCheckBaseStrategyList.add(TaskSelectVersionStrategy.builder().client(client).taskModel(taskModel).build());

//        startCheckBaseStrategyList.add(RedisStartCheckRedisUrlStrategy.builder().client(client).redisPoolProps(redisPoolProps).taskModel(taskModel).build());


        return startCheckBaseStrategyList;
    }
}
