package syncer.syncerreplication.rdb.datatype;

import lombok.Getter;
import lombok.Setter;
import syncer.syncerreplication.event.AbstractEvent;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
@Getter
@Setter
public class KeyValuePair<K, V> extends AbstractEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    protected DB db;
    protected int valueRdbType;
    protected ExpiredType expiredType = ExpiredType.NONE;
    protected Long expiredValue;
    protected EvictType evictType = EvictType.NONE;
    protected Long evictValue;
    protected K key;
    protected V value;

    protected DataType dataType;
    protected  Long size;
    public int getValueRdbType() {
        return valueRdbType;
    }

    public void setValueRdbType(int valueRdbType) {
        this.valueRdbType = valueRdbType;
    }

    public ExpiredType getExpiredType() {
        return expiredType;
    }

    public void setExpiredType(ExpiredType expiredType) {
        this.expiredType = expiredType;
    }

    public Long getExpiredValue() {
        return expiredValue;
    }

    public void setExpiredValue(Long expiredValue) {
        this.expiredValue = expiredValue;
    }

    public EvictType getEvictType() {
        return evictType;
    }

    public void setEvictType(EvictType evictType) {
        this.evictType = evictType;
    }

    public Long getEvictValue() {
        return evictValue;
    }

    public void setEvictValue(Long evictValue) {
        this.evictValue = evictValue;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

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
