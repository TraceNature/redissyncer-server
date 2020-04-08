package syncer.syncerservice.util;

import syncer.syncerplusredis.event.Event;
import syncer.syncerservice.po.KeyValueEventEntity;

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

    public static synchronized void cleanData(KeyValueEventEntity keyValueEventEntity,Event event){
        cleanData(event);
        cleanData(keyValueEventEntity);
    }
}
