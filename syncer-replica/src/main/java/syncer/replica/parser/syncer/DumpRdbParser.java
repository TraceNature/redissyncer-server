package syncer.replica.parser.syncer;

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

import java.io.IOException;

import syncer.replica.context.ContextKeyValue;
import syncer.replica.event.Event;
import syncer.replica.io.RedisInputStream;
import syncer.replica.kv.KeyValuePairEvent;
import syncer.replica.parser.BaseRdbParser;
import syncer.replica.parser.DefaultRedisRdbParser;
import syncer.replica.parser.IRdbValueParser;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;

/**
 * 将rdb内容转成 dump格式 https://redis.io/commands/dump 
 * 后续restore https://redis.io/commands/restore 
 */
public class DumpRdbParser extends DefaultRedisRdbParser {
    protected int version = -1;

    public DumpRdbParser(Replication replicator) {
        this(replicator, -1);
    }

    /**
     * @param replicator the replicator
     * @param version    dumped version : redis 2.8.x = 6, redis 3.x = 7, redis 4.0.x = 8, redis 5.0+ = 9. 
     *                   -1 means dumped version = rdb version
     */
    public DumpRdbParser(Replication replicator, int version) {
        this(replicator, version, 8192);
    }

    public DumpRdbParser(Replication replicator, int version, int size) {
        super(replicator, new DumpRdbValueParser(replicator, version, size));
        this.version = version;
    }
    
    /**
     * @param replicator the replicator
     * @param rdbValueParser rdb value visitor
     */
    public DumpRdbParser(Replication replicator, IRdbValueParser rdbValueParser) {
        super(replicator, rdbValueParser);
    }

    @Override
    public Event parseString(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o0 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o0.setValueRdbType(RDB_TYPE_STRING);
        o0.setKey(key);
        o0.setValue(rdbValueParser.parseString(in, version));
        return context.valueOf(o0);
    }

    @Override
    public Event parseList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o1 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o1.setValueRdbType(RDB_TYPE_LIST);
        o1.setKey(key);
        o1.setValue(rdbValueParser.parseList(in, version));
        return context.valueOf(o1);
    }

    @Override
    public Event parseSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o2 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o2.setValueRdbType(RDB_TYPE_SET);
        o2.setKey(key);
        o2.setValue(rdbValueParser.parseSet(in, version));
        return context.valueOf(o2);
    }

    @Override
    public Event parseZSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o3 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o3.setValueRdbType(RDB_TYPE_ZSET);
        o3.setKey(key);
        o3.setValue(rdbValueParser.parseZSet(in, version));
        return context.valueOf(o3);
    }

    @Override
    public Event parseZSet2(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o5 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();
        if (this.version != -1 && this.version < 8 /* since redis rdb version 8 */) {
            o5.setValueRdbType(RDB_TYPE_ZSET);
        } else {
            o5.setValueRdbType(RDB_TYPE_ZSET_2);
        }
        o5.setKey(key);
        o5.setValue(rdbValueParser.parseZSet2(in, version));
        return context.valueOf(o5);
    }

    @Override
    public Event parseHash(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o4 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o4.setValueRdbType(RDB_TYPE_HASH);
        o4.setKey(key);
        o4.setValue(rdbValueParser.parseHash(in, version));
        return context.valueOf(o4);
    }

    @Override
    public Event parseHashZipMap(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o9 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o9.setValueRdbType(RDB_TYPE_HASH_ZIPMAP);
        o9.setKey(key);
        o9.setValue(rdbValueParser.parseHashZipMap(in, version));
        return context.valueOf(o9);
    }

    @Override
    public Event parseListZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o10 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o10.setValueRdbType(RDB_TYPE_LIST_ZIPLIST);
        o10.setKey(key);
        o10.setValue(rdbValueParser.parseListZipList(in, version));
        return context.valueOf(o10);
    }

    @Override
    public Event parseSetIntSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o11 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o11.setValueRdbType(RDB_TYPE_SET_INTSET);
        o11.setKey(key);
        o11.setValue(rdbValueParser.parseSetIntSet(in, version));
        return context.valueOf(o11);
    }

    @Override
    public Event parseZSetZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o12 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o12.setValueRdbType(RDB_TYPE_ZSET_ZIPLIST);
        o12.setKey(key);
        o12.setValue(rdbValueParser.parseZSetZipList(in, version));
        return context.valueOf(o12);
    }

    @Override
    public Event parseHashZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o13 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o13.setValueRdbType(RDB_TYPE_HASH_ZIPLIST);
        o13.setKey(key);
        o13.setValue(rdbValueParser.parseHashZipList(in, version));
        return context.valueOf(o13);
    }

    @Override
    public Event parseListQuickList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o14 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();
        if (this.version != -1 && this.version < 7 /* since redis rdb version 7 */) {
            o14.setValueRdbType(RDB_TYPE_LIST);
        } else {
            o14.setValueRdbType(RDB_TYPE_LIST_QUICKLIST);
        }
        o14.setKey(key);
        o14.setValue(rdbValueParser.parseListQuickList(in, version));
        return context.valueOf(o14);
    }

    @Override
    public Event parseModule(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o6 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o6.setValueRdbType(RDB_TYPE_MODULE);
        o6.setKey(key);
        o6.setValue(rdbValueParser.parseModule(in, version));
        return context.valueOf(o6);
    }

    @Override
    public Event parseModule2(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o7 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o7.setValueRdbType(RDB_TYPE_MODULE_2);
        o7.setKey(key);
        o7.setValue(rdbValueParser.parseModule2(in, version));
        return context.valueOf(o7);
    }

    @Override
    public Event parseStreamListPacks(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o15 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o15.setValueRdbType(RDB_TYPE_STREAM_LISTPACKS);
        o15.setKey(key);
        o15.setValue(rdbValueParser.parseStreamListPacks(in, version));
        return context.valueOf(o15);
    } 
}
