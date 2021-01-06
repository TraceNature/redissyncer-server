package syncer.transmission.strategy.commandprocessing;

import com.google.common.collect.Maps;
import syncer.replica.entity.TaskRunTypeEnum;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.commandprocessing.factory.CommonProcessingStrategyListFactory;
import syncer.transmission.strategy.commandprocessing.factory.impl.IncrementOnlyProcessingStrategyListFactory;
import syncer.transmission.strategy.commandprocessing.factory.impl.StockonlyProcessingStrategyListFactory;
import syncer.transmission.strategy.commandprocessing.factory.impl.TotalProcessingStrategyListFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/24
 */
public class ProcessingRunStrategyListSelecter {
    private static final Map<TaskRunTypeEnum, CommonProcessingStrategyListFactory> kvFilterListSelectorMap= Maps.newConcurrentMap();

    public static List<CommonProcessingStrategy> getStrategyList(TaskRunTypeEnum taskRunTypeEnum, TaskModel taskModel, RedisClient client){
        if(!kvFilterListSelectorMap.containsKey(taskRunTypeEnum)){
            if(taskRunTypeEnum.equals(TaskRunTypeEnum.TOTAL)){
                kvFilterListSelectorMap.put(taskRunTypeEnum, TotalProcessingStrategyListFactory.builder().build());
            }else if(taskRunTypeEnum.equals(TaskRunTypeEnum.STOCKONLY)){
                kvFilterListSelectorMap.put(taskRunTypeEnum, StockonlyProcessingStrategyListFactory.builder().build());
            }else if(taskRunTypeEnum.equals(TaskRunTypeEnum.INCREMENTONLY)){
                kvFilterListSelectorMap.put(taskRunTypeEnum, IncrementOnlyProcessingStrategyListFactory.builder().build());
            }
        }
        return kvFilterListSelectorMap.get(taskRunTypeEnum).getStrategyList(taskModel,client);
    }

}
