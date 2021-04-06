package syncer.replica.util.type;

/**
 * KV过期类型
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public enum ExpiredType {
    /**
     * not set
     */
    NONE,
    /**
     * expired by seconds
     */
    SECOND,
    /**
     * expired by millisecond
     */
    MS
}
