package syncer.transmission.constants;

/**
 * 补偿命令集合
 */
public enum CmdEnum {
    ////////////////////////////////////幂等性命令
    /**
     * String
     */
    SET,

    /**
     * List类型
     */
    LPUSH,
    /**
     *Set类型
     */
    SADD,
    /**
     *ZSet类型
     */
    ZADD,
    /**
     *Hash类型
     */
    HMSET,

    /**
     * dump
     */
    RESTORE,

    /**
     * dump替换
     */
    RESOREREPLACE,

    /**
     * command命令格式
     */
    SEND,


    //////////////////////////////////////////非幂等性命令

    /**
     * String类型
     */
    APPEND,

    /**
     *自增
     */
    INCR,

    INCRBY,

    INCRBYFLOAT,

    /**
     * 自减
     */

    DECR,

    DECRBY
}