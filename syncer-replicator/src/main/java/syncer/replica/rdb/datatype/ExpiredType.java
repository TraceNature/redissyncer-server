package syncer.replica.rdb.datatype;

/**
 * @author zhanenqiang
 * @Description KV过期类型
 * @Date 2020/8/7
 */
public enum  ExpiredType {
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
