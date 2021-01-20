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

import syncer.replica.replication.Replication;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
public interface CommonProcessingStrategy {
    void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException;

    void toNext(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException;

    void setNext(CommonProcessingStrategy nextStrategy);
}
