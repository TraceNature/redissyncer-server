package syncer.replica.rdb.datatype;

/**
 * @author zhanenqiang
 * @Description 数据淘汰类型
 * @Date 2020/8/7
 */
public enum  EvictType {
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
