package syncer.replica.parser;

import syncer.replica.context.ContextKeyValue;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.Event;
import syncer.replica.io.RedisInputStream;

import java.io.IOException;

/**
 * https://github.com/sripathikrishnan/redis-rdb-tools/wiki/Redis-RDB-Dump-File-Format
 * @author: Eq Zhan
 * @create: 2021-03-15
 **/
public interface IRdbParser {
    /**
     * Magic Number   以字符串 "REDIS" 开头
     * 52 45 44 49 53  # Magic String "REDIS"
     * @param in
     * @return
     */
    String parseMagic(RedisInputStream in) throws IOException;


    /**
     * RDB 的版本号，用了4个字节存储版本号 大端存储
     * 30 30 30 37 # 4 digit ASCCII RDB Version Number. In this case, version = "0007" = 7
     * @param in
     * @return
     */
    int parseRdbVersion(RedisInputStream in) throws IOException;

    /**
     * FE 00   # FE = FE表示数据库编号，Redis支持多个库，以数字编号，这里00表示第0个数据库
     * FE 00   # FE = code that indicates database selector. db number = 00
     * @param in
     * @return
     */
    RedisDB parseCurrentDb(RedisInputStream in, int version) throws IOException;


    /**
     *
     * @param in
     * @param version
     * @param context
     * @return
     */
    RedisDB parseResizeDB(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    /**
     * $value-type # 1 byte flag indicating the type of value - set, map, sorted set etc.
     * @param in
     * @return
     */
    int parseValueType(RedisInputStream in) throws IOException;



    /**
     *
     * FD $unsigned int     # FD indicates "expiry time in seconds". After that, expiry time is read as a 4 byte unsigned int
     * FC $unsigned long    # FC indicates "expiry time in ms". After that, expiry time is read as a 8 byte unsigned long
     * $value-type          # This key value pair doesn't have an expiry. $value_type guaranteed != to FD, FC, FE and FF
     */
    Event parseExpireTime(RedisInputStream in, int version, ContextKeyValue context) throws IOException;


    Event parseExpireTimeMs(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    Event parseFreq(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    Event parseIdle(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    /*
     * aux
     */
    Event parseAux(RedisInputStream in, int version) throws IOException;

    Event parseModuleAux(RedisInputStream in, int version) throws IOException;

    /*
     * checksum
     */
    long parseEof(RedisInputStream in, int version) throws IOException;

    /**
     * String
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */
    Event parseString(RedisInputStream in, int version, ContextKeyValue context) throws IOException ;

    /**
     * List
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */
    Event parseList(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    /**
     * Set
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */

    Event parseSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    /**
     * ZSet
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */

    Event parseZSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException ;

    /**
     * ZSET2
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */
    Event parseZSet2(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    /**
     * HASH
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */
    Event parseHash(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    Event parseHashZipMap(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    /**
     * ZipList
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */
    Event parseListZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    Event parseSetIntSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    Event parseZSetZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    Event parseHashZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException ;

    Event parseListQuickList(RedisInputStream in, int version, ContextKeyValue context) throws IOException ;

    Event parseModule(RedisInputStream in, int version, ContextKeyValue context) throws IOException;

    Event parseModule2(RedisInputStream in, int version, ContextKeyValue context) throws IOException ;

    Event parseStreamListPacks(RedisInputStream in, int version, ContextKeyValue context) throws IOException ;

}
