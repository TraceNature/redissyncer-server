package com.i1314i.syncerplusservice.service.dump;

import com.i1314i.syncerplusredis.cmd.Command;
import com.i1314i.syncerplusredis.cmd.impl.SelectCommand;
import com.i1314i.syncerplusredis.rdb.datatype.DB;
import com.i1314i.syncerplusredis.rdb.datatype.ExpiredType;
import com.i1314i.syncerplusredis.rdb.datatype.Module;
import com.i1314i.syncerplusredis.rdb.datatype.ZSetEntry;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter@Setter
public class CommandKeyValuePair<T> extends SelectCommand implements Command {
    private static final long serialVersionUID = 1L;

    protected DB db;
    protected int valueRdbType;
    protected ExpiredType expiredType = ExpiredType.NONE;
    protected Long expiredValue;
    protected String key;
    protected byte[] rawValue;
    protected byte[] rawKey;
    protected T value;
//    protected int index;
    private Long ex;




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

    /**
     * @return RDB_TYPE_STRING
     */
    public String getValueAsString() {
        return (String) value;
    }

    /**
     * @return RDB_TYPE_HASH, RDB_TYPE_HASH_ZIPMAP, RDB_TYPE_HASH_ZIPLIST
     */
    public Map<String, String> getValueAsHash() {
        return (Map<String, String>) value;
    }

    /**
     * @return RDB_TYPE_SET, RDB_TYPE_SET_INTSET
     */
    public Set<String> getValueAsSet() {
        return (Set<String>) value;
    }

    /**
     * @return RDB_TYPE_ZSET, RDB_TYPE_ZSET_2, RDB_TYPE_ZSET_ZIPLIST
     */
    public Set<ZSetEntry> getValueAsZSet() {
        return (Set<ZSetEntry>) value;
    }

    /**
     * @return RDB_TYPE_LIST, RDB_TYPE_LIST_ZIPLIST, RDB_TYPE_LIST_QUICKLIST
     */
    public List<String> getValueAsStringList() {
        return (List<String>) value;
    }

    /**
     * @return RDB_TYPE_MODULE
     */
    public Module getValueAsModule() {
        return (Module) value;
    }

    @Override
    public String toString() {
        return "KeyValuePair{" +
                "db=" + db +
                ", valueRdbType=" + valueRdbType +
                ", expiredType=" + expiredType +
                ", expiredValue=" + expiredValue +
                ", key='" + key + '\'' +
                ", value=" + value +
                '}';
    }

}
