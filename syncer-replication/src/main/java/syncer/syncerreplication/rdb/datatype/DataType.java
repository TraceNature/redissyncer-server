package syncer.syncerreplication.rdb.datatype;

/**
 * @author zhanenqiang
 * @Description 数据类型
 * @Date 2020/4/7
 */
public enum  DataType {
    /**
     * String
     */
    STRING,

    LIST,

    HASH,

    SET,

    ZSET,

    MODULE,

    STREAM,

    //分片
    FRAGMENTATION,

    FRAGMENTATION_NUM

}
