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

package syncer.transmission.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.replica.replication.Replication;
import syncer.transmission.model.ExpandTaskModel;
import syncer.transmission.model.TaskModel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TaskDataEntity {
    private TaskModel taskModel;
    private OffSetEntity offSetEntity;
    private Replication replication;
    @Builder.Default
    private ExpandTaskModel expandTaskModel=new ExpandTaskModel();


    @Builder.Default
    private AtomicLong rdbKeyCount=new AtomicLong(0);

    @Builder.Default
    private AtomicLong allKeyCount=new AtomicLong(0);

    @Builder.Default
    private AtomicLong realKeyCount=new AtomicLong(0);

    @Builder.Default
    private AtomicInteger abandonKeyCount=new AtomicInteger(0);


    /**
     * 被抛弃key阈值
     */
    @Builder.Default
    private AtomicLong errorNums = new AtomicLong(0L);




}
