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
package syncer.replica.listener;

import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.replication.Replication;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/18
 */
@FunctionalInterface
public interface TaskStatusListener {
    void handle(Replication replication, SyncerTaskEvent event);
}
