package syncer.transmission.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/24
 */
public class KVPersistenceDataEntity {

    private volatile List<EventEntity> keys = new ArrayList<>();

    public synchronized void addKey(EventEntity key) {
        keys.add(key);
//        keys.add(key);
    }

    public List<EventEntity> getKeys() {
        return keys;
    }


    public EventEntity getKey(int i) {
        return keys.get(i);
    }

    public synchronized int size() {
        return keys.size();
    }


    public synchronized void clear() {
        keys.clear();
    }

}
