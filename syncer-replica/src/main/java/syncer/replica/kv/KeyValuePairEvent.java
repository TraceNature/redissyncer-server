package syncer.replica.kv;

import lombok.Getter;
import lombok.Setter;
import syncer.replica.entity.RedisDB;
import syncer.replica.util.type.EvictType;
import syncer.replica.util.type.ExpiredType;
import syncer.replica.util.type.KvDataType;

/**
 * kv事件
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
@Getter
@Setter
public class KeyValuePairEvent<K, V> extends AbstractEvent {
    private static final long serialVersionUID = 1L;
    private RedisDB db;
    private int valueRdbType;
    /**
     * Kv过期类型
     */
    private ExpiredType expiredType = ExpiredType.NONE;
    private Long expiredValue;
    /**
     * 淘汰算法 maxmemory-policy
     */
    private EvictType evictType = EvictType.NONE;

    private Long evictValue;
    private K key;
    private V value;

    protected KvDataType dataType;
    protected  Long size;


    /**
     * @return expiredValue as Integer
     */
    public Integer getExpiredSeconds() {
        return expiredValue == null ? null : expiredValue.intValue();
    }

    /**
     * @return expiredValue as Long
     */
    public Long getExpiredMs() {
        return expiredValue;
    }
}
