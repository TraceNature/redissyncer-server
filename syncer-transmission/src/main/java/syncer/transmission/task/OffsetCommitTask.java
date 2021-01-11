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

package syncer.transmission.task;

import syncer.replica.entity.SyncType;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.po.entity.OffSetCommitEntity;
import syncer.transmission.queue.DbDataCommitQueue;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/25
 */
public class OffsetCommitTask implements Runnable{
    @Override
    public void run() {
        while (true){
            try {
                Map<String, TaskDataEntity> aliveThreadHashMap= SingleTaskDataManagerUtils.getAliveThreadHashMap();
                aliveThreadHashMap.entrySet().forEach(data ->{
                    if(SyncType.SYNC.getCode().equals(data.getValue().getTaskModel().getSyncType())
                            ||SyncType.COMMANDDUMPUP.getCode().equals(data.getValue().getTaskModel().getSyncType())){
                        if(data.getValue().getOffSetEntity().getReplId()!=null&&data.getValue().getOffSetEntity().getReplOffset()!=null){
                            DbDataCommitQueue.put(OffSetCommitEntity.builder().taskId(data.getKey())
                                    .replId(data.getValue().getOffSetEntity().getReplId())
                                    .offset(data.getValue().getOffSetEntity().getReplOffset().get())
                                    .build());


                        }

                    }
                });

                Thread.sleep(1000*60);
            }catch (Exception e){

            }
        }
    }
}
