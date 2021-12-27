package syncer.replica.register;


import syncer.replica.datatype.command.CommandName;
import syncer.replica.parser.command.common.*;
import syncer.replica.parser.command.defaults.DefaultCommandParser;
import syncer.replica.parser.command.jimdb.JimDbDefaultCommandParser;
import syncer.replica.parser.command.set.SetCommandParser;
import syncer.replica.parser.command.string.AppendCommandParser;
import syncer.replica.replication.Replication;

public class TikvCommandRegister {
    public synchronized static Replication addCommandParser(Replication replication) {
        addStringCommandParser(replication);

//        addTransactionsCommandParser(replication);
//        addListsCommandParser(replication);
//        addSetsCommandParser(replication);
//        addSortSetsCommandParser(replication);
//        addHashCommandParser(replication);
//        addStreamCommandParser(replication);
//        addGeoCommandParser(replication);
//        addHyperLogLogCommandParser(replication);
        addKeysCommandParser(replication);
//        addPubSubCommandParser(replication);
//        addLuaCommandParser(replication);
//        addSysCommandParser(replication);
//        addJimdbCommandParser(replication);
        return replication;
    }

    /**
     * String 相关的命令
     * @param replication
     */
    static void addStringCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.APPEND), new AppendCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SET), new SetCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETEX), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETNX), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.BITCOUNT), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.BITFIELD), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.BITOP), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.BITPOS), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.DECR), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.DECRBY), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.GETBIT), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.GETDEL), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.GETEX), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.GETSET), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.INCR), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.INCRBY), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.INCRBYFLOAT), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.MSET), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.MSETNX), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.PSETEX), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETBIT), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETEX), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETNX), new DefaultCommandParser());
//        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETRANGE), new DefaultCommandParser());
    }

    /**
     * 事务相关
      * @param replication
     */
    static void addTransactionsCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.MULTI), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.EXEC), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.DISCARD), new DefaultCommandParser());
    }

    /**
     * Lists
     * @param replication
     */
    static void addListsCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BLPOP), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BRPOP), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BRPOPLPUSH), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LINSERT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LPOP), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LPUSH), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BLMOVE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LPUSHX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LREM), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LRem),new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LSET), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LTRIM), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RPOP), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.LMOVE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RPOPLPUSH), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RPUSH), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RPUSHX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SORT), new DefaultCommandParser());
    }

    /**
     * Sets
     * @param replication
     */
    static void addSetsCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SADD), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SDIFFSTORE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SINTERSTORE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SMOVE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SPOP), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SREM), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SUNIONSTORE), new DefaultCommandParser());
    }

    /**
     * ZSets
     * @param replication
     */
    static void addSortSetsCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BZPOPMAX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BZPOPMIN), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZADD), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZDIFFSTORE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZINCRBY), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZINTER), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZINTERSTORE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZPOPMAX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZPOPMIN), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZRANGESTORE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZREM), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZREMRANGEBYLEX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZREMRANGEBYRANK), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZREMRANGEBYSCORE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.ZUNIONSTORE), new DefaultCommandParser());

    }

    /**
     * HASH
     * @param replication
     */
    static void addHashCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.HDEL), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.HINCRBY), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.HINCRBYFLOAT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.HMSET), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.HSET), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.HSETNX), new DefaultCommandParser());


    }

    /**
     * Stream
     * @param replication
     */
    static void addStreamCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XACK), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XADD), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XAUTOCLAIM), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XCLAIM), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XDEL), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XGROUP), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XTRIM), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XPENDING), new DefaultCommandParser());
    }

    /**
     * GEO
     * @param replication
     */
    static void addGeoCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.GEOADD), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.GEOSEARCHSTORE), new DefaultCommandParser());
    }

    /**
     * HyperLogLog
     * @param replication
     */
    static void addHyperLogLogCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PFADD), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PFCOUNT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PFMERGE), new DefaultCommandParser());
    }

    /**
     * Keys
     * @param replication
     */
    static void addKeysCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.COPY), new CopyCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.DEL), new DelCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.EXPIRE), new ExpireCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.EXPIREAT), new ExpireAtCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PING),new PingCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SELECT),new SelectCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.FLUSHDB),new FlushDBCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.FLUSHALL),new FlushAllCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.REPLCONF),new ReplConfParser());

        /**
         * TODO
         * 实现响应parser
         */
        replication.addCommandParser(CommandName.name(DefaultCommandNames.MOVE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PERSIST), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PEXPIRE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PEXPIREAT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RENAME), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RENAMENX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RESTORE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.UNLINK), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.XSETID), new DefaultCommandParser());
    }

    /**
     * pubsub
     * @param replication
     */
    static void addPubSubCommandParser(Replication replication){
        //PSUBSCRIBE

        replication.addCommandParser(CommandName.name(DefaultCommandNames.PUBLISH), new DefaultCommandParser());
    }

    /**
     * script
     * @param replication
     */
    static void addLuaCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.EVAL), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.EVALSHA), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SCRIPT_FLUSH), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SCRIPT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SCRIPT_KILL), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SCRIPT_LOAD), new DefaultCommandParser());
    }


    static void addSysCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SWAPDB),new DefaultCommandParser());
    }

    /**
     * jimdb
     * @param replication
     */
    static void addJimdbCommandParser(Replication replication){
        //jimdb解析
        replication.addCommandParser(CommandName.name(DefaultCommandNames.JIMDB_PREFIX),new JimDbDefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.JIMDB_PREFIX2),new JimDbDefaultCommandParser());
    }
}
