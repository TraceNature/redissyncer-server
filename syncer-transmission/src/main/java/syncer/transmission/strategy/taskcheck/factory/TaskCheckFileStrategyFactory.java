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

package syncer.transmission.strategy.taskcheck.factory;

import com.google.common.collect.Lists;
import lombok.Builder;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategyFactory;
import syncer.transmission.strategy.taskcheck.impl.TaskCheckRedisTypeStrategy;
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
        //判断RedisType是否为空
        taskCheckStrategyList.add(TaskCheckRedisTypeStrategy.builder().client(client).taskModel(taskModel).build());
        //判断是否重复
        taskCheckStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());

        taskCheckStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());
        taskCheckStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());
        taskCheckStrategyList.add(TaskDistinctStrategy.builder().client(client).taskModel(taskModel).build());
        return taskCheckStrategyList;
    }
}
