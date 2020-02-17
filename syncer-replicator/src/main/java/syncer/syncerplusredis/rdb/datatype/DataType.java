package syncer.syncerplusredis.rdb.datatype;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/7
 */
public enum  DataType {
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
