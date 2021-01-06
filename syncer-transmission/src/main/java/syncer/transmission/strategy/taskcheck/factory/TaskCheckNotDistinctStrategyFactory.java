package syncer.transmission.strategy.taskcheck.factory;

import com.google.common.collect.Lists;
import lombok.Builder;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategyFactory;
import syncer.transmission.strategy.taskcheck.impl.TaskCheckRedisUrlStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 统一策略不判断是否重复
 * @Date 2020/12/14
 */
@Builder

public class TaskCheckNotDistinctStrategyFactory implements ITaskCheckStrategyFactory {
    @Override
    public List<ITaskCheckStrategy> getStrategyList(RedisClient client, TaskModel taskModel) {
        List<ITaskCheckStrategy>taskCheckStrategyList= Lists.newArrayList();

        taskCheckStrategyList.add(TaskCheckRedisUrlStrategy.builder().client(client).taskModel(taskModel).build());
        taskCheckStrategyList.add(TaskCheckRedisUrlStrategy.builder().client(client).taskModel(taskModel).build());

        return taskCheckStrategyList;
    }
}
