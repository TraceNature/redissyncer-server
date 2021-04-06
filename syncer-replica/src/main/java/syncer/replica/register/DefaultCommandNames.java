package syncer.replica.register;

public class DefaultCommandNames {
    /**
     * String
     */

    public static final String SET="SET";
    public static final String APPEND="APPEND";
    public static final String BITCOUNT="BITCOUNT";
    public static final String BITFIELD="BITFIELD";
    public static final String BITOP="BITOP";
    public static final String BITPOS="BITPOS";
    public static final String DECR="DECR";
    public static final String DECRBY="DECRBY";
    public static final String GETBIT="GETBIT";
    public static final String GETDEL="GETDEL";
    public static final String GETEX="GETEX";
    public static final String GETSET="GETSET";

    public static final String INCR="INCR";
    public static final String INCRBY="INCRBY";
    public static final String INCRBYFLOAT="INCRBYFLOAT";
    /**
     * MSET key value [key value ...]
     *Available since 1.0.1.
     * Time complexity: O(N) where N is the number of keys to set.
     */
    public static final String MSET="MSET";
    /**
     * MSETNX key value [key value ...]
     * Available since 1.0.1.
     *
     * Time complexity: O(N) where N is the number of keys to set.
     */
    public static final String MSETNX="MSETNX";
    /**
     * PSETEX key milliseconds value
     * Available since 2.6.0.
     *
     * Time complexity: O(1)
     */
    public static final String PSETEX="PSETEX";
    /**
     * SETBIT key offset value
     * Available since 2.2.0.
     *
     * Time complexity: O(1)
     */
    public static final String SETBIT="SETBIT";
    /**
     * SETEX key seconds value
     * Available since 2.0.0.
     *
     * Time complexity: O(1)
     */
    public static final String SETEX="SETEX";
    /**
     * SETNX key value
     * Available since 1.0.0.
     *
     * Time complexity: O(1)
     */
    public static final String SETNX="SETNX";
    /**
     *SETRANGE key offset value
     * Available since 2.2.0.
     *
     * Time complexity: O(1), not counting the time taken to copy the new string in place. Usually,
     * this string is very small so the amortized complexity is O(1).
     * Otherwise, complexity is O(M) with M being the length of the value argument.
     */
    public static final String SETRANGE="SETRANGE";


    /**
     *  transactions
     */

    /**
     * MULTI
     * Available since 1.2.0.
     */
    public static final String MULTI="MULTI";

    public static final String EXEC="EXEC";

    /**
     * DISCARD
     * Available since 2.0.0.
     * 取消事务
     */
    public static final String DISCARD="DISCARD";


    /**
     * Lists
     */

    public static final String BLPOP="BLPOP";
    public static final String BRPOP="BRPOP";
    public static final String BRPOPLPUSH="BRPOPLPUSH";
    public static final String BLMOVE="BLMOVE";
    public static final String LMOVE="LMOVE";
    public static final String LINSERT="LINSERT";
    public static final String LPOP="LPOP";
    public static final String LPUSH="LPUSH";
    public static final String LPUSHX="LPUSHX";
    public static final String LREM="LREM";
    public static final String LRem="LRem";
    public static final String LSET="LSET";
    public static final String LTRIM="LTRIM";
    public static final String RPOP="RPOP";
    public static final String RPOPLPUSH="RPOPLPUSH";
    public static final String RPUSH="RPUSH";
    public static final String RPUSHX="RPUSHX";
    public static final String SORT="SORT";

    /**
     * Sets
     */
    public static final String SADD="SADD";
    public static final String SDIFFSTORE="SDIFFSTORE";
    public static final String SINTERSTORE="SINTERSTORE";
    public static final String SMOVE="SMOVE";
    public static final String SPOP="SPOP";
    public static final String SREM="SREM";
    public static final String SUNIONSTORE="SUNIONSTORE";

    /**
     * SortedSets
     */
    public static final String BZPOPMAX="BZPOPMAX";
    public static final String BZPOPMIN="BZPOPMIN";
    public static final String ZADD="ZADD";
    public static final String ZDIFFSTORE="ZDIFFSTORE";
    public static final String ZINCRBY="ZINCRBY";
    public static final String ZINTER="ZINTER";
    public static final String ZINTERSTORE="ZINTERSTORE";
    public static final String ZPOPMAX="ZPOPMAX";
    public static final String ZPOPMIN="ZPOPMIN";
    public static final String ZRANGESTORE="ZRANGESTORE";
    public static final String ZREM="ZREM";
    public static final String ZREMRANGEBYLEX="ZREMRANGEBYLEX";
    public static final String ZREMRANGEBYRANK="ZREMRANGEBYRANK";
    public static final String ZREMRANGEBYSCORE="ZREMRANGEBYSCORE";
    public static final String ZUNIONSTORE="ZUNIONSTORE";

    /**
     * HASH
     */
    public static final String HDEL="HDEL";
    public static final String HINCRBY="HINCRBY";
    public static final String HINCRBYFLOAT="HINCRBYFLOAT";
    public static final String HMSET="HMSET";
    public static final String HSET="HSET";
    public static final String HSETNX="HSETNX";

    /**
     * Stream
     */
    public static final String XACK="XACK";
    public static final String XADD="XADD";
    public static final String XAUTOCLAIM="XAUTOCLAIM";
    public static final String XCLAIM="XCLAIM";
    public static final String XDEL="XDEL";
    public static final String XGROUP="XGROUP";
    public static final String XTRIM="XTRIM";
    public static final String XPENDING="XPENDING";

    /**
     * GEO
     */
    public static final String GEOADD="GEOADD";
    public static final String GEOSEARCHSTORE="GEOSEARCHSTORE";

    /**
     * HyperLogLog
     */
    public static final String PFADD="PFADD";
    public static final String PFCOUNT="PFCOUNT";
    public static final String PFMERGE="PFMERGE";

    /**
     * Keys
     */
    public static final String COPY="COPY";
    public static final String DEL="DEL";
    public static final String EXPIRE="EXPIRE";
    public static final String EXPIREAT="EXPIREAT";
    public static final String MOVE="MOVE";
    public static final String PERSIST="PERSIST";
    public static final String PEXPIRE="PEXPIRE";
    public static final String PEXPIREAT="PEXPIREAT";
    public static final String RENAME="RENAME";
    public static final String RENAMENX="RENAMENX";
    public static final String RESTORE="RESTORE";
    public static final String UNLINK="UNLINK";

    /**
     * PUBSUB
     */
    public static final String PUBLISH="PUBLISH";

    /**
     * lua
     */
    public static final String EVAL="EVAL";
    public static final String EVALSHA="EVALSHA";
    public static final String SCRIPT_FLUSH="SCRIPT FLUSH";
    public static final String SCRIPT="SCRIPT";
    public static final String SCRIPT_KILL="SCRIPT KILL";
    public static final String SCRIPT_LOAD="SCRIPT LOAD";

    /**
     * jimdb
     */
    public static final String JIMDB_PREFIX="TRANSMIT";
    public static final String JIMDB_PREFIX2="TRANSMITFIRSTPARSER";



    public static final String SWAPDB="SWAPDB";
    public static final String FLUSHDB="FLUSHDB";
    public static final String FLUSHALL="FLUSHALL";
    public static final String SELECT="SELECT";
    public static final String PING="PING";
    public static final String REPLCONF="REPLCONF";
    public static final String XSETID="XSETID";
}
