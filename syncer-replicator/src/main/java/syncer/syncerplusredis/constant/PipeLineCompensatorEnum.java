package syncer.syncerplusredis.constant;

/**
 * pipeline补偿机制支持命令集合
 * @author zhanenqiang
 * @Description 描述
 * @Date 2019/12/30
 */
public enum PipeLineCompensatorEnum {
    /**
     * String no time
     */
    SET,

    /**
     * String with time
     */
    SET_WITH_TIME,



    LPUSH,

    LPUSH_WITH_TIME,

    LPUSH_LIST,

    LPUSH_WITH_TIME_LIST,

    SADD,

    SADD_WITH_TIME,

    SADD_SET,

    SADD_WITH_TIME_SET,

    ZADD,

    ZADD_WITH_TIME,

    HMSET,

    HMSET_WITH_TIME,

    RESTORE,

    RESTORREPLCE,

    DEL,
    COMMAND,
    SELECT,
    PEXPIRE,


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
