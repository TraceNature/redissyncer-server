package syncerservice.syncerplusservice.service.dump;

import syncerservice.syncerplusredis.event.PreRdbSyncEvent;
import syncerservice.syncerplusredis.rdb.datatype.DB;
import syncerservice.syncerplusredis.rdb.datatype.EvictType;
import syncerservice.syncerplusredis.rdb.datatype.ExpiredType;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class RdbKeyValuePair extends PreRdbSyncEvent {
    private static final long serialVersionUID = 1L;

    protected DB db;
    protected int valueRdbType;
    protected ExpiredType expiredType = ExpiredType.NONE;
    protected Long expiredValue;
    protected EvictType evictType = EvictType.NONE;
    protected Long evictValue;
    protected Object key;
    protected Object value;
    protected Integer expiredSeconds;
    protected Long expiredMs;
    protected Long checksum;
}