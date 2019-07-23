package com.i1314i.syncerplusservice.service.dump;

import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.datatype.EvictType;
import com.moilioncircle.redis.replicator.rdb.datatype.ExpiredType;
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