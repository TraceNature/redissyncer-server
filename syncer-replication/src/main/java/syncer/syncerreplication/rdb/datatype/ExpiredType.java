package syncer.syncerreplication.rdb.datatype;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description kv过期时间类型
 * @Date 2020/4/7
 */
public enum ExpiredType implements Serializable {
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
