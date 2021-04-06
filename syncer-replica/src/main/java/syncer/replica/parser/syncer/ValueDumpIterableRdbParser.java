package syncer.replica.parser.syncer;



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
import syncer.replica.parser.IRdbValueParser;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.replica.util.type.KvDataType;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static syncer.replica.constant.Constants.*;


public class ValueDumpIterableRdbParser extends DefaultRedisRdbParser {


    public ValueDumpIterableRdbParser(Replication replication) {
        this(replication, new ValueIterableDumpRdbValueParser(replication));
    }

    public ValueDumpIterableRdbParser(Replication replication, IRdbValueParser valueParser) {
        super(replication, valueParser);
    }

    public ValueDumpIterableRdbParser(Replication replication, int version) {
        this(replication, version, 8192);
    }


    public ValueDumpIterableRdbParser(Replication replication, int version, int size) {
        super(replication, new ValueIterableDumpRdbValueParser(replication, version, size));
    }


    @Override
    public Event parseList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o1 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o1.setValueRdbType(RDB_TYPE_LIST);
        o1.setKey(key);
        o1.setValue(rdbValueParser.parseList(in, version));
        o1.setDataType(KvDataType.LIST);

//        o1.setSize(parser.rdbLoadLen().len);

        return context.valueOf(o1);
    }

    @Override
    public Event parseSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o2 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o2.setValueRdbType(RDB_TYPE_SET);
        o2.setKey(key);
        o2.setValue(rdbValueParser.parseSet(in, version));
        o2.setDataType(KvDataType.SET);

//        o2.setSize(parser.rdbLoadLen().len);

        return context.valueOf(o2);
    }

    @Override
    public Event parseZSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<ZSetEntry>> o3 = new KeyStringValueZSetEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o3.setValueRdbType(RDB_TYPE_ZSET);
        o3.setKey(key);
        o3.setValue(rdbValueParser.parseZSet(in, version));
        o3.setDataType(KvDataType.ZSET);

        //    o3.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o3);
    }

    @Override
    public Event parseZSet2(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<ZSetEntry>> o5 = new KeyStringValueZSetEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o5.setValueRdbType(RDB_TYPE_ZSET_2);
        o5.setKey(key);
        o5.setValue(rdbValueParser.parseZSet2(in, version));
        o5.setDataType(KvDataType.ZSET);

        //    o5.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o5);
    }

    @Override
    public Event parseHash(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<Map.Entry<byte[], byte[]>>> o4 = new KeyStringValueMapEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o4.setValueRdbType(RDB_TYPE_HASH);
        o4.setKey(key);
        o4.setValue(rdbValueParser.parseHash(in, version));
        o4.setDataType(KvDataType.HASH);

        //     o4.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o4);
    }

    @Override
    public Event parseHashZipMap(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<Map.Entry<byte[], byte[]>>> o9 = new KeyStringValueMapEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o9.setValueRdbType(RDB_TYPE_HASH_ZIPMAP);
        o9.setKey(key);
        o9.setValue(rdbValueParser.parseHashZipMap(in, version));
        o9.setDataType(KvDataType.HASH);
        //      o9.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o9);
    }

    @Override
    public Event parseListZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o10 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o10.setValueRdbType(RDB_TYPE_LIST_ZIPLIST);
        o10.setKey(key);
        o10.setValue(rdbValueParser.parseListZipList(in, version));
        o10.setDataType(KvDataType.LIST);

        //   o10.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o10);
    }

    @Override
    public Event parseSetIntSet(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o11 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o11.setValueRdbType(RDB_TYPE_SET_INTSET);
        o11.setKey(key);
        o11.setValue(rdbValueParser.parseSetIntSet(in, version));
        o11.setDataType(KvDataType.SET);

        //  o11.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o11);
    }

    @Override
    public Event parseZSetZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<ZSetEntry>> o12 = new KeyStringValueZSetEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o12.setValueRdbType(RDB_TYPE_ZSET_ZIPLIST);
        o12.setKey(key);
        o12.setValue(rdbValueParser.parseZSetZipList(in, version));
        o12.setDataType(KvDataType.ZSET);

        //  o12.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o12);
    }

    @Override
    public Event parseHashZipList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<Map.Entry<byte[], byte[]>>> o13 = new KeyStringValueMapEntryIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o13.setValueRdbType(RDB_TYPE_HASH_ZIPLIST);
        o13.setKey(key);
        o13.setValue(rdbValueParser.parseHashZipList(in, version));
        o13.setDataType(KvDataType.HASH);

        //    o13.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o13);
    }

    @Override
    public Event parseListQuickList(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], Iterator<byte[]>> o14 = new KeyStringValueByteArrayIteratorEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o14.setValueRdbType(RDB_TYPE_LIST_QUICKLIST);
        o14.setKey(key);
        o14.setValue(rdbValueParser.parseListQuickList(in, version));
        o14.setDataType(KvDataType.LIST);

        // o14.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o14);
    }



    /**
     * 基于dump序列化方式
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */
    @Override
    public Event parseString(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o0 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o0.setValueRdbType(RDB_TYPE_STRING);
        o0.setKey(key);
        o0.setValue(rdbValueParser.parseString(in, version));
        o0.setDataType(KvDataType.STRING);
//        System.out.println("++"+parser.rdbLoadLen().len);
        //      o0.setSize(Long.valueOf(o0.getValue().length+key.length));

        return context.valueOf(o0);
    }

    @Override
    public Event parseModule(RedisInputStream in, int version, ContextKeyValue context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePairEvent<byte[], byte[]> o6 = new DumpKeyValuePairEvent();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o6.setValueRdbType(RDB_TYPE_MODULE);
        o6.setKey(key);
        o6.setValue(rdbValueParser.parseModule(in, version));
        o6.setDataType(KvDataType.MODULE);

        //     o6.setSize(parser.rdbLoadLen().len);
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
        o7.setDataType(KvDataType.MODULE);

        //     o7.setSize(parser.rdbLoadLen().len);
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
        o15.setDataType(KvDataType.STREAM);
        //  o15.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o15);
    }
}
