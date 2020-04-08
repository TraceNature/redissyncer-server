package syncer.syncerreplication.rdb.datatype;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
public class ContextKeyValuePair extends KeyValuePair<Void, Void> implements Serializable {

    private static final long serialVersionUID = 1L;

    public <K, V> KeyValuePair<K, V> valueOf(KeyValuePair<K, V> kv) {
        kv.setDb(this.getDb());
        kv.setEvictType(this.getEvictType());
        kv.setEvictValue(this.getEvictValue());
        kv.setExpiredType(this.getExpiredType());
        kv.setExpiredValue(this.getExpiredValue());
        return kv;
    }
}
