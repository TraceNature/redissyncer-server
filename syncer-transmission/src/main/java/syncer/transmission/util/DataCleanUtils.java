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

package syncer.transmission.util;

import syncer.replica.event.Event;
import syncer.transmission.po.entity.KeyValueEventEntity;

/**
 * @author zhanenqiang
 * @Description 对象清理 不使用的对象应手动赋值为null
 * @Date 2020/3/23
 */
public class DataCleanUtils {
    public static synchronized void cleanData(Object event){
        if(event!=null){
            event=null;
        }
    }

    public static synchronized void cleanData(KeyValueEventEntity keyValueEventEntity){
        if(keyValueEventEntity!=null){
            cleanData(keyValueEventEntity.getEvent());
            keyValueEventEntity=null;
        }
    }

    public static synchronized void cleanData(KeyValueEventEntity keyValueEventEntity, Event event){
        cleanData(event);
        cleanData(keyValueEventEntity);
    }
}
