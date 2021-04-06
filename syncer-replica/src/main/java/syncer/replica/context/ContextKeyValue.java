package syncer.replica.context;

import syncer.replica.kv.KeyValuePairEvent;

/**
 * @author: Eq Zhan
 * @create: 2021-03-17
 **/
public class ContextKeyValue extends KeyValuePairEvent<Void, Void> {

    private static final long serialVersionUID = 1L;

    public <K, V> KeyValuePairEvent<K, V> valueOf(KeyValuePairEvent<K, V> kv) {
        kv.setDb(this.getDb());
        kv.setEvictType(this.getEvictType());
        kv.setEvictValue(this.getEvictValue());
        kv.setExpiredType(this.getExpiredType());
        kv.setExpiredValue(this.getExpiredValue());
        return kv;
    }
}