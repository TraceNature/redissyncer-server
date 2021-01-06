package syncer.transmission.strategy.taskcheck.factory;

import com.google.common.collect.Lists;
import lombok.Builder;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategyFactory;
import syncer.transmission.strategy.taskcheck.impl.TaskDistinctStrategy;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 文件统一策略
 * @Date 2020/12/15
 */
@Builder
public class TaskCheckFileStrategyFactory implements ITaskCheckStrategyFactory {
    @Override
    public List<ITaskCheckStrategy> getStrategyList(RedisClient client, TaskModel taskModel) {
        List<ITaskCheckStrategy>taskCheckStrategyList= Lists.newArrayList();
        //判断是否重复
        taskCheckStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());

        taskCheckStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());
        taskCheckStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());
        taskCheckStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());
        return taskCheckStrategyList;
    }
}
