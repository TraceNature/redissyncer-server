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
import syncer.transmission.strategy.commandprocessing.kafka.CommandProcessingKafkaRdbCommandSendStrategy;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 只全量策略工厂
 * @Date 2020/12/24
 */
@Builder
public class KafkaStockonlyProcessingStrategyListFactory implements KafkaCommonProcessingStrategyListFactory {
    @Override
    public List<CommonProcessingStrategy> getStrategyList(TaskModel taskModel, KafkaProducerClient client) {
        List<CommonProcessingStrategy> strategyList = Lists.newArrayList();
        strategyList.add(CommandProcessingTimeCalculationStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());
        //过滤策略
        strategyList.add(CommandProcessingCommandFilterStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());

        strategyList.add(CommandProcessingDataAnalysisStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());
        strategyList.add(CommandProcessingDbMappingStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).build());

        strategyList.add(CommandProcessingKafkaRdbCommandSendStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).kafkaProducerClient(client).build());


        return strategyList;
    }
}
