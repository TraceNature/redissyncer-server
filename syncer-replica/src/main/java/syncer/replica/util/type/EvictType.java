package syncer.replica.util.type;

/**
 * @author: Eq Zhan
 * 数据淘汰类型
 * @create: 2021-03-16
 **/
public enum EvictType {
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
