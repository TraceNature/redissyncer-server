package syncer.replica.parser.iterable;

import syncer.replica.constant.Constants;
import syncer.replica.context.ContextKeyValue;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.event.Event;
import syncer.replica.event.iter.KeyStringValueByteArrayIteratorEvent;
import syncer.replica.event.iter.KeyStringValueMapEntryIteratorEvent;
import syncer.replica.event.iter.KeyStringValueZSetEntryIteratorEvent;
import syncer.replica.io.RedisInputStream;
import syncer.replica.kv.KeyValuePairEvent;
import syncer.replica.parser.BaseRdbParser;
import syncer.replica.parser.DefaultRedisRdbParser;
import syncer.replica.replication.Replication;
import syncer.replica.parser.IRdbValueParser;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * 迭代切分解析器
 */
public class SyncerIterableRdbParser extends DefaultRedisRdbParser {
    public SyncerIterableRdbParser(Replication replication) {
        super(replication,new SyncerIterableRdbValueParser(replication));
    }

    public SyncerIterableRdbParser(Replication replication, IRdbValueParser rdbValueParser) {
        super(replication, rdbValueParser);
    }


    @Override
    public Event parseList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o1 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o1.setValueRdbType(Constants.RDB_TYPE_LIST);
        o1.setKey(key);
        o1.setValue(rdbValueParser.parseList(in, version));
        return context.valueOf(o1);
    }

    @Override
    public Event parseSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o2 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o2.setValueRdbType(Constants.RDB_TYPE_SET);
        o2.setKey(key);
        o2.setValue(rdbValueParser.parseSet(in, version));
        return context.valueOf(o2);
    }

    @Override
    public Event parseZSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<ZSetEntry>> o3 = new KeyStringValueZSetEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o3.setValueRdbType(Constants.RDB_TYPE_ZSET);
        o3.setKey(key);
        o3.setValue(rdbValueParser.parseZSet(in, version));
        return context.valueOf(o3);
    }

    @Override
    public Event parseZSet2(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<ZSetEntry>> o5 = new KeyStringValueZSetEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o5.setValueRdbType(Constants.RDB_TYPE_ZSET_2);
        o5.setKey(key);
        o5.setValue(rdbValueParser.parseZSet2(in, version));
        return context.valueOf(o5);
    }

    @Override
    public Event parseHash(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<Map.Entry<byte[], byte[]>>> o4 = new KeyStringValueMapEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o4.setValueRdbType(Constants.RDB_TYPE_HASH);
        o4.setKey(key);
        o4.setValue(rdbValueParser.parseHash(in, version));
        return context.valueOf(o4);
    }

    @Override
    public Event parseHashZipMap(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<Map.Entry<byte[], byte[]>>> o9 = new KeyStringValueMapEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o9.setValueRdbType(Constants.RDB_TYPE_HASH_ZIPMAP);
        o9.setKey(key);
        o9.setValue(rdbValueParser.parseHashZipMap(in, version));
        return context.valueOf(o9);
    }

    @Override
    public Event parseListZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o10 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o10.setValueRdbType(Constants.RDB_TYPE_LIST_ZIPLIST);
        o10.setKey(key);
        o10.setValue(rdbValueParser.parseListZipList(in, version));
        return context.valueOf(o10);
    }

    @Override
    public Event parseSetIntSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o11 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o11.setValueRdbType(Constants.RDB_TYPE_SET_INTSET);
        o11.setKey(key);
        o11.setValue(rdbValueParser.parseSetIntSet(in, version));
        return context.valueOf(o11);
    }

    @Override
    public Event parseZSetZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<ZSetEntry>> o12 = new KeyStringValueZSetEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o12.setValueRdbType(Constants.RDB_TYPE_ZSET_ZIPLIST);
        o12.setKey(key);
        o12.setValue(rdbValueParser.parseZSetZipList(in, version));
        return context.valueOf(o12);
    }

    @Override
    public Event parseHashZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<Map.Entry<byte[], byte[]>>> o13 = new KeyStringValueMapEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o13.setValueRdbType(Constants.RDB_TYPE_HASH_ZIPLIST);
        o13.setKey(key);
        o13.setValue(rdbValueParser.parseHashZipList(in, version));
        return context.valueOf(o13);
    }

    @Override
    public Event parseListQuickList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o14 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o14.setValueRdbType(Constants.RDB_TYPE_LIST_QUICKLIST);
        o14.setKey(key);
        o14.setValue(rdbValueParser.parseListQuickList(in, version));
        return context.valueOf(o14);
    }
}
