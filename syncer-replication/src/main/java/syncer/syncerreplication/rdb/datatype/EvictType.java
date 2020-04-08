package syncer.syncerreplication.rdb.datatype;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 淘汰机制类型
 * @Date 2020/4/7
 */
public enum EvictType implements Serializable {

    /**
     * maxmemory-policy : volatile-lru, allkeys-lru. unit : second
     */
    LRU,

    /**
     * maxmemory-policy : volatile-lfu, allkeys-lfu.
     */
    LFU,

    /**
     * maxmemory-policy : noeviction, volatile-random, allkeys-random, volatile-ttl.
     */
    NONE
}
