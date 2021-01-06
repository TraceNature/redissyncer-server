package syncer.transmission.strategy.commandprocessing.factory;

import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/24
 */
public interface CommonProcessingStrategyListFactory {
    List<CommonProcessingStrategy> getStrategyList(TaskModel taskModel, RedisClient client);
}
