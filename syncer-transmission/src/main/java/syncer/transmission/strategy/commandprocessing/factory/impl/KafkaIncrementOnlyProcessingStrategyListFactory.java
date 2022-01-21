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

package syncer.transmission.strategy.commandprocessing.factory.impl;

import com.google.common.collect.Lists;
import lombok.Builder;
import syncer.transmission.model.TaskModel;
import syncer.transmission.mq.kafka.KafkaProducerClient;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.strategy.commandprocessing.factory.KafkaCommonProcessingStrategyListFactory;
import syncer.transmission.strategy.commandprocessing.impl.*;
import syncer.transmission.strategy.commandprocessing.kafka.CommandProcessingKafkaAofCommandSendStrategy;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 只增量策略链表生成工厂
 * @Date 2020/12/24
 */
@Builder
public class KafkaIncrementOnlyProcessingStrategyListFactory implements KafkaCommonProcessingStrategyListFactory {
    @Override
    public List<CommonProcessingStrategy> getStrategyList(TaskModel taskModel, KafkaProducerClient client) {
        List<CommonProcessingStrategy> strategyList = Lists.newArrayList();
        //过滤策略
        strategyList.add(CommandProcessingCommandFilterStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());
        //sentinel PUBLISH filter
        strategyList.add(CommandProcessingSentinelCommandFileterStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());


        strategyList.add(CommandProcessingDataAnalysisStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());
        //更新offset
       // strategyList.add(CommandProcessingOffsetUpdateStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());

        strategyList.add(CommandProcessingDbMappingStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());

        strategyList.add(CommandProcessingKafkaAofCommandSendStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).kafkaProducerClient(client).build());

        return strategyList;
    }
}
