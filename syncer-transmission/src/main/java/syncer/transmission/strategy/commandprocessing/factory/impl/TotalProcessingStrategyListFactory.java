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
import syncer.common.config.BreakPointConfig;
import syncer.common.config.EtcdServerConfig;
import syncer.common.constant.BreakpointContinuationType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.strategy.commandprocessing.factory.CommonProcessingStrategyListFactory;
import syncer.transmission.strategy.commandprocessing.impl.*;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 全量+增量策略链
 * @Date 2020/12/24
 */
@Builder
public class TotalProcessingStrategyListFactory implements CommonProcessingStrategyListFactory {

    @Override
    public List<CommonProcessingStrategy> getStrategyList(TaskModel taskModel, RedisClient client) {
        List<CommonProcessingStrategy> strategyList = Lists.newArrayList();
        strategyList.add(CommandProcessingTimeCalculationStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).build());
        //过滤策略
        strategyList.add(CommandProcessingCommandFilterStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).build());
        //sentinel PUBLISH filter
        strategyList.add(CommandProcessingSentinelCommandFileterStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).build());

        strategyList.add(CommandProcessingDataAnalysisStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).build());
        //更新offset

       // strategyList.add(CommandProcessingOffsetUpdateStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).build());

        strategyList.add(CommandProcessingDbMappingStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).build());

        if(BreakPointConfig.getBreakpointContinuationType().equals(BreakpointContinuationType.v1)){
            strategyList.add(CommandProcessingAofCommandSendStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).build());
            strategyList.add(CommandProcessingRdbCommandSendStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).redisVersion(taskModel.getRedisVersion()).build());
        }else {
            strategyList.add(CommandProcessingAofMultiCommandSendStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).build());
            strategyList.add(CommandProcessingRdbMultiCommandSendStrategy.builder().taskId(taskModel.getId()).taskModel(taskModel).client(client).redisVersion(taskModel.getRedisVersion()).build());
        }


        return strategyList;
    }
}
