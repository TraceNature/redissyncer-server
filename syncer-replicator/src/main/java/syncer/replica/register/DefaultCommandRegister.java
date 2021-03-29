package syncer.replica.register;

import syncer.replica.cmd.CommandName;
import syncer.replica.cmd.jimdb.JimDbFirstCommandParser;
import syncer.replica.cmd.parser.*;
import syncer.replica.replication.Replication;

/**
 * @author zhanenqiang
 * @Description 默认命令注册器
 * @Date 2020/12/22
 */
public class DefaultCommandRegister {
    public synchronized static Replication addCommandParser(Replication replication) {
        /**
        r.addCommandParser(CommandName.name("PING"), new PingParser());
        r.addCommandParser(CommandName.name("REPLCONF"), new ReplConfParser());
        //
        r.addCommandParser(CommandName.name("APPEND"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SETEX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("MSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("DEL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HMSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EXPIRE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EXPIREAT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("GETSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HSETNX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("MSETNX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PSETEX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SETNX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SETRANGE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HDEL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LPOP"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LPUSH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LPUSHX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LREM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LRem"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RPOP"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RPUSH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RPUSHX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZREM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RENAME"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("INCR"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("DECR"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("INCRBY"), new DefaultCommandParser());


        r.addCommandParser(CommandName.name("INCRBYFLOAT"), new DefaultCommandParser());
//        r.addCommandParser(CommandName.name("INCRBYFLOAT"), new IncrByFloatParser());

        r.addCommandParser(CommandName.name("DECRBY"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PERSIST"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SELECT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("FLUSHALL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("FLUSHDB"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HINCRBY"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZINCRBY"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("MOVE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SMOVE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PFADD"), new DefaultCommandParser());


//        r.addCommandParser(CommandName.name("PFADD"), new PFAddParser());
        r.addCommandParser(CommandName.name("PFCOUNT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PFMERGE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SDIFFSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SINTERSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SUNIONSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZINTERSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZUNIONSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("BRPOPLPUSH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LINSERT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RENAMENX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RESTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PEXPIRE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PEXPIREAT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("GEOADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EVAL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EVALSHA"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SCRIPT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PUBLISH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("BITOP"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("BITFIELD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SETBIT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SREM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("UNLINK"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SWAPDB"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("MULTI"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EXEC"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZREMRANGEBYSCORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZREMRANGEBYRANK"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZREMRANGEBYLEX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LTRIM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SORT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RPOPLPUSH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZPOPMIN"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZPOPMAX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XACK"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XCLAIM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XDEL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XGROUP"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XTRIM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XSETID"), new DefaultCommandParser());

        //jimdb解析
        r.addCommandParser(CommandName.name("TRANSMIT"),new JimDbDefaultCommandParser());
        r.addCommandParser(CommandName.name("TRANSMITFIRSTPARSER"),new JimDbFirstCommandParser());

        // since redis 6.2
        r.addCommandParser(CommandName.name("COPY"), new CopyParser());
        r.addCommandParser(CommandName.name("LMOVE"), new LMoveParser());
        r.addCommandParser(CommandName.name("BLMOVE"), new BLMoveParser());
        r.addCommandParser(CommandName.name("ZDIFFSTORE"), new ZDiffStoreParser());
        r.addCommandParser(CommandName.name("GEOSEARCHSTORE"), new GeoSearchStoreParser());
        return r;
        **/
        addStringCommandParser(replication);
        addTransactionsCommandParser(replication);
        addListsCommandParser(replication);
        addSetsCommandParser(replication);
        addSortSetsCommandParser(replication);
        addHashCommandParser(replication);
        addStreamCommandParser(replication);
        addGeoCommandParser(replication);
        addHyperLogLogCommandParser(replication);
        addKeysCommandParser(replication);
        addPubSubCommandParser(replication);
        addLuaCommandParser(replication);
        addSysCommandParser(replication);
        addJimdbCommandParser(replication);
        return replication;
    }



    /**
     * String 相关的命令
     * @param replication
     */
    static void addStringCommandParser(Replication replication){
        replication.addCommandParser(CommandName.name(DefaultCommandNames.APPEND), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SET), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETEX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETNX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BITCOUNT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BITFIELD), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BITOP), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.BITPOS), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.DECR), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.DECRBY), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.GETBIT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.GETDEL), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.GETEX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.GETSET), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.INCR), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.INCRBY), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.INCRBYFLOAT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.MSET), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.MSETNX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PSETEX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETBIT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETEX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETNX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SETRANGE), new DefaultCommandParser());
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
        replication.addCommandParser(CommandName.name(DefaultCommandNames.COPY), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.DEL), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.EXPIRE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.EXPIREAT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.MOVE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PERSIST), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PEXPIRE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PEXPIREAT), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RENAME), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RENAMENX), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.RESTORE), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.UNLINK), new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.FLUSHDB),new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.FLUSHALL),new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.SELECT),new DefaultCommandParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.PING),new PingParser());
        replication.addCommandParser(CommandName.name(DefaultCommandNames.REPLCONF),new ReplConfParser());
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
