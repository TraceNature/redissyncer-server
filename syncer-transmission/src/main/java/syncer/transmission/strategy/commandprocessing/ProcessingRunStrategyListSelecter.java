// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.strategy.commandprocessing;

import com.google.common.collect.Maps;
import syncer.replica.util.TaskRunTypeEnum;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.mq.kafka.KafkaProducerClient;
import syncer.transmission.strategy.commandprocessing.factory.CommonProcessingStrategyListFactory;
import syncer.transmission.strategy.commandprocessing.factory.KafkaCommonProcessingStrategyListFactory;
import syncer.transmission.strategy.commandprocessing.factory.impl.*;

import java.util.List;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 策略选择器
 * @Date 2020/12/24
 */
public class ProcessingRunStrategyListSelecter {
    private static final Map<TaskRunTypeEnum, CommonProcessingStrategyListFactory> kvFilterListSelectorMap= Maps.newConcurrentMap();
    private static final Map<TaskRunTypeEnum, KafkaCommonProcessingStrategyListFactory> kvKafkaFilterListSelectorMap= Maps.newConcurrentMap();



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


    public static List<CommonProcessingStrategy> getKafkaStrategyList(TaskRunTypeEnum taskRunTypeEnum, TaskModel taskModel, KafkaProducerClient client){
        if(!kvKafkaFilterListSelectorMap.containsKey(taskRunTypeEnum)){
            if(taskRunTypeEnum.equals(TaskRunTypeEnum.TOTAL)){
                kvKafkaFilterListSelectorMap.put(taskRunTypeEnum, KafkaTotalProcessingStrategyListFactory.builder().build());
            }else if(taskRunTypeEnum.equals(TaskRunTypeEnum.STOCKONLY)){
                kvKafkaFilterListSelectorMap.put(taskRunTypeEnum, KafkaStockonlyProcessingStrategyListFactory.builder().build());
            }else if(taskRunTypeEnum.equals(TaskRunTypeEnum.INCREMENTONLY)){
                kvKafkaFilterListSelectorMap.put(taskRunTypeEnum, KafkaIncrementOnlyProcessingStrategyListFactory.builder().build());
            }
        }
        return kvKafkaFilterListSelectorMap.get(taskRunTypeEnum).getStrategyList(taskModel,client);
    }
}
