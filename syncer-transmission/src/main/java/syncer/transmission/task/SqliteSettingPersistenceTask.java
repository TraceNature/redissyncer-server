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

import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description sqlite持久化
 * @Date 2020/4/1
 */
public class SqliteSettingPersistenceTask  implements Runnable{

    @Override
    public void run() {
        while (true){
            try {
                Map<String, TaskDataEntity> aliveThreadHashMap = SingleTaskDataManagerUtils.getAliveThreadHashMap();
                for (Map.Entry<String, TaskDataEntity> data:aliveThreadHashMap.entrySet()){
                    if(!(data.getValue().getRdbKeyCount().get()==0L
                            &&data.getValue().getAllKeyCount().get()==0L
                            &&data.getValue().getRealKeyCount().get()==0L)){
                        SqlOPUtils.updateKeyCountById(data.getKey(),data.getValue().getRdbKeyCount().get(),data.getValue().getAllKeyCount().get(),data.getValue().getRealKeyCount().get());
                    }

                    /**
                     * todo
                     * null异常
                     */
                    if(null!=data.getValue().getOffSetEntity().getReplOffset()&&data.getValue().getOffSetEntity().getReplOffset().get()>-1L){
                        TaskModel sqliteTaskModel=SqlOPUtils.findTaskById(data.getKey());
                        if(null!=sqliteTaskModel&&null!=sqliteTaskModel.getOffset()&&!sqliteTaskModel.getOffset().equals(data.getValue().getOffSetEntity().getReplOffset().get())){
                            SqlOPUtils.updateOffset(data.getKey(),data.getValue().getOffSetEntity().getReplOffset().get());
                        }

                    }
                }

                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}