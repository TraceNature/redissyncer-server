package syncer.replica.rdb.sync.datatype;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/7
 */
public enum  DataType {
    /**
     * String类型
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

    FRAGMENTATION_NUM,

    /**
     * 淘汰规则  Key_Discarded_By_Dbmapper_Rule, 因dbmapper被抛弃
     */

    KEY_DISCARDED_BY_DBMAPPER_RULE,


    /**
     * 被抛弃
     */
    ABANDONED



}
