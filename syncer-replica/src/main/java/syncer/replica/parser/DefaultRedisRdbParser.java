package syncer.replica.parser;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.context.ContextKeyValue;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.datatype.rdb.stream.Stream;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.*;
import syncer.replica.io.RedisInputStream;
import syncer.replica.kv.KeyValuePairEvent;
import syncer.replica.parser.skip.SkipRdbParser;
import syncer.replica.replication.Replication;
import syncer.replica.util.map.ByteArrayMap;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.type.EvictType;
import syncer.replica.util.type.ExpiredType;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static syncer.replica.constant.Constants.RDB_OPCODE_FREQ;
import static syncer.replica.constant.Constants.RDB_OPCODE_IDLE;
import static syncer.replica.constant.Constants.RDB_TYPE_HASH;
import static syncer.replica.constant.Constants.RDB_TYPE_HASH_ZIPLIST;
import static syncer.replica.constant.Constants.RDB_TYPE_HASH_ZIPMAP;
import static syncer.replica.constant.Constants.RDB_TYPE_LIST;
import static syncer.replica.constant.Constants.RDB_TYPE_LIST_QUICKLIST;
import static syncer.replica.constant.Constants.RDB_TYPE_LIST_ZIPLIST;
import static syncer.replica.constant.Constants.RDB_TYPE_MODULE;
import static syncer.replica.constant.Constants.RDB_TYPE_MODULE_2;
import static syncer.replica.constant.Constants.RDB_TYPE_SET;
import static syncer.replica.constant.Constants.RDB_TYPE_SET_INTSET;
import static syncer.replica.constant.Constants.RDB_TYPE_STREAM_LISTPACKS;
import static syncer.replica.constant.Constants.RDB_TYPE_STRING;
import static syncer.replica.constant.Constants.RDB_TYPE_ZSET;
import static syncer.replica.constant.Constants.RDB_TYPE_ZSET_2;
import static syncer.replica.constant.Constants.RDB_TYPE_ZSET_ZIPLIST;

/**
 * https://github.com/sripathikrishnan/redis-rdb-tools/wiki/Redis-RDB-Dump-File-Format
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
@Slf4j
public class DefaultRedisRdbParser implements IRdbParser {

    private Replication replication;
    protected IRdbValueParser rdbValueParser;
    private final static String REDIS_Flag="REDIS";

    public DefaultRedisRdbParser(Replication replication) {
        this(replication, new DefaultRedisRdbValueParser(replication));
    }

    public DefaultRedisRdbParser(Replication replication, IRdbValueParser rdbValueParser) {
        this.replication = replication;
        this.rdbValueParser = rdbValueParser;
    }

    /**
     * Magic Number 以字符串 "REDIS" 开头
     * The file starts off with the magic string "REDIS". This is a quick sanity check to know we are dealing with a redis rdb file.
     * 52 45 44 49 53  # Magic String "REDIS"
     * @param in
     * @return
     */
    @Override
    public String parseMagic(RedisInputStream in) throws IOException {
        //REDIS
        String magic = BaseRdbParser.StringHelper.str(in, 5);
        if (!REDIS_Flag.equals(magic)) {
            throw new UnsupportedOperationException("can't read MAGIC STRING [REDIS] ,value:" + magic);
        }
        return magic;
    }



    /**
     * RDB 的版本号，用了4个字节存储版本号 大端存储
     * 30 30 30 37 # 4 digit ASCCII RDB Version Number. In this case, version = "0007" = 7
     * @param in
     * @return
     */
    @Override
    public int parseRdbVersion(RedisInputStream in) throws IOException {
        int version = parseInt(BaseRdbParser.StringHelper.str(in, 4));
        if (version < 2 || version > 9) {
            throw new UnsupportedOperationException(String.valueOf("can't handle RDB format version " + version));
        }
        return version;
    }

    /**
     * FE 00   # FE = FE表示数据库编号，Redis支持多个库，以数字编号，这里00表示第0个数据库
     * FE 00   # FE = code that indicates database selector. db number = 00
     *
     * A Redis instance can have multiple databases.
     *
     * A single byte 0xFE flags the start of the database selector.
     * After this byte, a variable length field indicates the database number.
     * See the section "Length Encoding" to understand how to read this database number.
     *
     * @param in
     * @return
     */
    @Override
    public RedisDB parseCurrentDb(RedisInputStream in, int version) throws IOException {
        /*
         * ----------------------------
         * FE $length-encoding         # Previous db ends, next db starts. Database number read using length encoding.
         * ----------------------------
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        long dbNumber = parser.rdbLoadLen().len;
        return new RedisDB(dbNumber);
    }


    @Override
    public RedisDB parseResizeDB(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        long dbsize = parser.rdbLoadLen().len;
        long expiresSize = parser.rdbLoadLen().len;
        RedisDB db = context.getDb();
        if (Objects.nonNull(db)) {
            db.setDbSize(dbsize);
            db.setExpires(expiresSize);
        }
        return db;
    }

    /**
     * $value-type # 1 byte flag indicating the type of value - set, map, sorted set etc.
     * @param in
     * @return
     */
    @Override
    public int parseValueType(RedisInputStream in) throws IOException {
        return in.read();
    }



    /**
     * ----------------------------
     * FD $unsigned int            # FD indicates "expiry time in seconds". After that, expiry time is read as a 4 byte unsigned int
     * $value-type                 # 1 byte flag indicating the type of value - set, map, sorted set etc.
     * $string-encoded-name         # The name, encoded as a redis string
     * $encoded-value              # The value. Encoding depends on $value-type
     * ----------------------------
     */
    @Override
    public Event parseExpireTime(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        long expiredSec = parser.rdbLoadTime();
        int type = parseValueType(in);
        context.setExpiredType(ExpiredType.SECOND);
        context.setExpiredValue(expiredSec);
        context.setValueRdbType(type);
        KeyValuePairEvent<?, ?> kv;
        if (type == RDB_OPCODE_FREQ) {
            kv = (KeyValuePairEvent<?, ?>) parseFreq(in, version, context);
        } else if (type == RDB_OPCODE_IDLE) {
            kv = (KeyValuePairEvent<?, ?>) parseIdle(in, version, context);
        } else {
            kv = rdbLoadObject(in, version, context);
        }
        return kv;
    }

    /**
     * ----------------------------
     * FC $unsigned long           # FC indicates "expiry time in ms". After that, expiry time is read as a 8 byte unsigned long
     * $value-type                 # 1 byte flag indicating the type of value - set, map, sorted set etc.
     * $string-encoded-name         # The name, encoded as a redis string
     * $encoded-value              # The value. Encoding depends on $value-type
     * ----------------------------
     */
    @Override
    public Event parseExpireTimeMs(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        long expiredMs = parser.rdbLoadMillisecondTime();
        int type = parseValueType(in);
        context.setExpiredType(ExpiredType.MS);
        context.setExpiredValue(expiredMs);
        context.setValueRdbType(type);
        KeyValuePairEvent<?, ?> kv;
        if (type == RDB_OPCODE_FREQ) {
            kv = (KeyValuePairEvent<?, ?>) parseFreq(in, version, context);
        } else if (type == RDB_OPCODE_IDLE) {
            kv = (KeyValuePairEvent<?, ?>) parseIdle(in, version, context);
        } else {
            kv = rdbLoadObject(in, version, context);
        }
        return kv;
    }

    @Override
    public Event parseFreq(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        long lfuFreq = in.read();
        int valueType = parseValueType(in);
        context.setValueRdbType(valueType);
        context.setEvictType(EvictType.LFU);
        context.setEvictValue(lfuFreq);
        KeyValuePairEvent<?, ?> kv = rdbLoadObject(in, version, context);
        return kv;
    }

    @Override
    public Event parseIdle(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        long lruIdle = parser.rdbLoadLen().len;
        int valueType = parseValueType(in);
        context.setValueRdbType(valueType);
        context.setEvictType(EvictType.LRU);
        context.setEvictValue(lruIdle);
        KeyValuePairEvent<?, ?> kv = rdbLoadObject(in, version, context);
        return kv;
    }

    @Override
    public Event parseAux(RedisInputStream in, int version) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        String auxKey = Strings.toString(parser.rdbLoadEncodedStringObject().first());
        String auxValue = Strings.toString(parser.rdbLoadEncodedStringObject().first());
        if (!auxKey.startsWith("%")) {
            if (log.isInfoEnabled()) {
                log.info("RDB {}: {}", auxKey, auxValue);
            }
            if ("repl-id".equals(auxKey)) {
                replication.getConfig().setReplId(auxValue);
            }
            if ("repl-offset".equals(auxKey)){
                replication.getConfig().setReplOffset(parseLong(auxValue));
            }
            if ("repl-stream-db".equals(auxKey)){
                replication.getConfig().setReplStreamDB(parseInt(auxValue));
            }
            return new AuxField(auxKey, auxValue);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("unrecognized RDB AUX field: {}, value: {}", auxKey, auxValue);
            }
            return null;
        }
    }

    @Override
    public Event parseModuleAux(RedisInputStream in, int version) throws IOException {
        SkipRdbParser parser = new SkipRdbParser(in);
        parser.rdbLoadLen();
        parser.rdbLoadCheckModuleValue();
        return null;
    }

    /**
     * ----------------------------
     * ...                         # Key value pairs for this database, additonal database
     * FF                          ## End of RDB file indicator
     * 8 byte checksum             ## CRC 64 checksum of the entire file.
     * ----------------------------
     */
    @Override
    public long parseEof(RedisInputStream in, int version) throws IOException {
        if (version >= 5){
            return in.readLong(8);
        }
        return 0L;
    }

    @Override
    public Event parseString(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o0 = new KeyStringValueStringEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        byte[] val = rdbValueParser.parseString(in, version);
        o0.setValueRdbType(RDB_TYPE_STRING);
        o0.setValue(val);
        o0.setKey(key);
        return context.valueOf(o0);
    }

    @Override
    public Event parseList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], List<byte[]>> o1 = new KeyStringValueListEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        List<byte[]> list = rdbValueParser.parseList(in, version);
        o1.setValueRdbType(RDB_TYPE_LIST);
        o1.setValue(list);
        o1.setKey(key);
        return context.valueOf(o1);
    }

    @Override
    public Event parseSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Set<byte[]>> o2 = new KeyStringValueSetEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        Set<byte[]> set = rdbValueParser.parseSet(in, version);
        o2.setValueRdbType(RDB_TYPE_SET);
        o2.setValue(set);
        o2.setKey(key);
        return context.valueOf(o2);
    }

    @Override
    public Event parseZSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Set<ZSetEntry>> o3 = new KeyStringValueZSetEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        Set<ZSetEntry> zset = rdbValueParser.parseZSet(in, version);
        o3.setValueRdbType(RDB_TYPE_ZSET);
        o3.setValue(zset);
        o3.setKey(key);
        return context.valueOf(o3);
    }

    @Override
    public Event parseZSet2(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Set<ZSetEntry>> o5 = new KeyStringValueZSetEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        Set<ZSetEntry> zset = rdbValueParser.parseZSet2(in, version);
        o5.setValueRdbType(RDB_TYPE_ZSET_2);
        o5.setValue(zset);
        o5.setKey(key);
        return context.valueOf(o5);
    }

    @Override
    public Event parseHash(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Map<byte[], byte[]>> o4 = new KeyStringValueHashEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        ByteArrayMap map = rdbValueParser.parseHash(in, version);
        o4.setValueRdbType(RDB_TYPE_HASH);
        o4.setValue(map);
        o4.setKey(key);
        return context.valueOf(o4);
    }

    @Override
    public Event parseHashZipMap(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Map<byte[], byte[]>> o9 = new KeyStringValueHashEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        ByteArrayMap map = rdbValueParser.parseHashZipMap(in, version);
        o9.setValueRdbType(RDB_TYPE_HASH_ZIPMAP);
        o9.setValue(map);
        o9.setKey(key);
        return context.valueOf(o9);
    }

    @Override
    public Event parseListZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], List<byte[]>> o10 = new KeyStringValueListEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        List<byte[]> list = rdbValueParser.parseListZipList(in, version);
        o10.setValueRdbType(RDB_TYPE_LIST_ZIPLIST);
        o10.setValue(list);
        o10.setKey(key);
        return context.valueOf(o10);
    }

    @Override
    public Event parseSetIntSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Set<byte[]>> o11 = new KeyStringValueSetEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        Set<byte[]> set = rdbValueParser.parseSetIntSet(in, version);
        o11.setValueRdbType(RDB_TYPE_SET_INTSET);
        o11.setValue(set);
        o11.setKey(key);
        return context.valueOf(o11);
    }

    @Override
    public Event parseZSetZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Set<ZSetEntry>> o12 = new KeyStringValueZSetEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        Set<ZSetEntry> zset = rdbValueParser.parseZSetZipList(in, version);
        o12.setValueRdbType(RDB_TYPE_ZSET_ZIPLIST);
        o12.setValue(zset);
        o12.setKey(key);
        return context.valueOf(o12);
    }

    @Override
    public Event parseHashZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Map<byte[], byte[]>> o13 = new KeyStringValueHashEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        ByteArrayMap map = rdbValueParser.parseHashZipList(in, version);
        o13.setValueRdbType(RDB_TYPE_HASH_ZIPLIST);
        o13.setValue(map);
        o13.setKey(key);
        return context.valueOf(o13);
    }

    @Override
    public Event parseListQuickList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], List<byte[]>> o14 = new KeyStringValueListEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        List<byte[]> list = rdbValueParser.parseListQuickList(in, version);
        o14.setValueRdbType(RDB_TYPE_LIST_QUICKLIST);
        o14.setValue(list);
        o14.setKey(key);
        return context.valueOf(o14);
    }

    @Override
    public Event parseModule(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Module> o6 = new KeyStringValueModuleEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        Module module = rdbValueParser.parseModule(in, version);
        o6.setValueRdbType(RDB_TYPE_MODULE);
        o6.setValue(module);
        o6.setKey(key);
        return context.valueOf(o6);
    }

    @Override
    public Event parseModule2(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Module> o7 = new KeyStringValueModuleEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        Module module = rdbValueParser.parseModule2(in, version);
        o7.setValueRdbType(RDB_TYPE_MODULE_2);
        o7.setValue(module);
        o7.setKey(key);
        return context.valueOf(o7);
    }

    @Override
    public Event parseStreamListPacks(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Stream> o15 = new KeyStringValueStreamEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        Stream stream = rdbValueParser.parseStreamListPacks(in, version);
        o15.setValueRdbType(RDB_TYPE_STREAM_LISTPACKS);
        o15.setValue(stream);
        o15.setKey(key);
        return context.valueOf(o15);
    }


    /**
     *
     * 获取不同数据结构解析器
     * ----------------------------
     * $value-type                 # This name value pair doesn't have an expiry. $value_type guaranteed != to FD, FC, FE and FF
     * $string-encoded-name
     * $encoded-value
     * ----------------------------
     */
    private KeyValuePairEvent<?, ?> rdbLoadObject(RedisInputStream in, int version, ContextKeyValue context) throws IOException {

        int valueType = context.getValueRdbType();
        switch (valueType) {
            case RDB_TYPE_STRING:
                return (KeyValuePairEvent<?, ?>) parseString(in, version, context);
            case RDB_TYPE_LIST:
                return (KeyValuePairEvent<?, ?>) parseList(in, version, context);
            case RDB_TYPE_SET:
                return (KeyValuePairEvent<?, ?>) parseSet(in, version, context);
            case RDB_TYPE_ZSET:
                return (KeyValuePairEvent<?, ?>) parseZSet(in, version, context);
            case RDB_TYPE_ZSET_2:
                return (KeyValuePairEvent<?, ?>) parseZSet2(in, version, context);
            case RDB_TYPE_HASH:
                return (KeyValuePairEvent<?, ?>) parseHash(in, version, context);
            case RDB_TYPE_HASH_ZIPMAP:
                return (KeyValuePairEvent<?, ?>) parseHashZipMap(in, version, context);
            case RDB_TYPE_LIST_ZIPLIST:
                return (KeyValuePairEvent<?, ?>) parseListZipList(in, version, context);
            case RDB_TYPE_SET_INTSET:
                return (KeyValuePairEvent<?, ?>) parseSetIntSet(in, version, context);
            case RDB_TYPE_ZSET_ZIPLIST:
                return (KeyValuePairEvent<?, ?>) parseZSetZipList(in, version, context);
            case RDB_TYPE_HASH_ZIPLIST:
                return (KeyValuePairEvent<?, ?>) parseHashZipList(in, version, context);
            case RDB_TYPE_LIST_QUICKLIST:
                return (KeyValuePairEvent<?, ?>) parseListQuickList(in, version, context);
            case RDB_TYPE_MODULE:
                return (KeyValuePairEvent<?, ?>) parseModule(in, version, context);
            case RDB_TYPE_MODULE_2:
                return (KeyValuePairEvent<?, ?>) parseModule2(in, version, context);
            case RDB_TYPE_STREAM_LISTPACKS:
                return (KeyValuePairEvent<?, ?>) parseStreamListPacks(in, version, context);
            default:
                throw new AssertionError("unexpected value type:" + valueType);
        }
    }
}
