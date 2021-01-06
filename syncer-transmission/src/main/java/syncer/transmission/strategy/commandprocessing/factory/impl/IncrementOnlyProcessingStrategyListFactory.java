package syncer.transmission.strategy.commandprocessing.factory.impl;

import com.google.common.collect.Lists;
import lombok.Builder;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.strategy.commandprocessing.factory.CommonProcessingStrategyListFactory;
import syncer.transmission.strategy.commandprocessing.impl.*;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 只增量策略链表生成工厂
 * @Date 2020/12/24
 */
@Builder
public class IncrementOnlyProcessingStrategyListFactory implements CommonProcessingStrategyListFactory {
    @Override
    public List<CommonProcessingStrategy> getStrategyList(TaskModel taskModel, RedisClient client) {
        List<CommonProcessingStrategy> strategyList = Lists.newArrayList();
        strategyList.add(CommandProcessingDataAnalysisStrategy.builder().taskId(taskModel.getId()).client(client).build());
        strategyList.add(CommandProcessingDbMappingStrategy.builder().taskId(taskModel.getId()).client(client).build());
        strategyList.add(CommandProcessingAofCommandSendStrategy.builder().taskId(taskModel.getId()).client(client).build());
        return strategyList;
    }
}
