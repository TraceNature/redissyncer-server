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

package syncer.transmission.strategy.taskcheck;

import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;

/**
 * @author zhanenqiang
 * @Description 任务启动前检查策略
 * @Date 2020/2/25
 */
public interface ITaskCheckStrategy {

    void run(RedisClient client, TaskModel taskModel) throws Exception;

    void toNext(RedisClient client, TaskModel taskModel) throws Exception;

    void setNext(ITaskCheckStrategy nextStrategy);

}
