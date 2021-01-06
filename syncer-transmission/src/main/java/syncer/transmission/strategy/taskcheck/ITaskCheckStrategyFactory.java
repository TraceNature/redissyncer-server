package syncer.transmission.strategy.taskcheck;

import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/14
 */
public interface ITaskCheckStrategyFactory {
    List<ITaskCheckStrategy> getStrategyList(RedisClient client, TaskModel taskModel);

}
