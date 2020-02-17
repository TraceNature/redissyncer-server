package syncer.syncerplusredis.extend.replicator.visitor;

import com.alibaba.fastjson.JSON;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.io.RedisInputStream;
import syncer.syncerplusredis.rdb.BaseRdbParser;
import syncer.syncerplusredis.rdb.DefaultRdbVisitor;
import syncer.syncerplusredis.rdb.RdbValueVisitor;
import syncer.syncerplusredis.rdb.datatype.*;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.KeyStringValueByteArrayIterator;
import syncer.syncerplusredis.rdb.iterable.datatype.KeyStringValueMapEntryIterator;
import syncer.syncerplusredis.rdb.iterable.datatype.KeyStringValueZSetEntryIterator;
import syncer.syncerplusredis.rdb.skip.SkipRdbParser;
import syncer.syncerplusredis.replicator.Replicator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static syncer.syncerplusredis.replicator.Constants.*;

/**
 * 除LIST HASH SET ZSET 命令外其他基于dump restore的方式
 */

public class ValueDumpIterableRdbVisitor  extends DefaultRdbVisitor {

    public ValueDumpIterableRdbVisitor(Replicator replicator) {
        this(replicator, new ValueIterableDumpRdbValueVisitor(replicator));
    }

    public ValueDumpIterableRdbVisitor(Replicator replicator, RdbValueVisitor valueParser) {
        super(replicator, valueParser);
    }

    public ValueDumpIterableRdbVisitor(Replicator replicator, int version) {
        this(replicator, version, 8192);
    }


    public ValueDumpIterableRdbVisitor(Replicator replicator, int version, int size) {
        super(replicator, new ValueIterableDumpRdbValueVisitor(replicator, version, size));
    }

    @Override
    public Event applyList(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<byte[]>> o1 = new KeyStringValueByteArrayIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o1.setValueRdbType(RDB_TYPE_LIST);
        o1.setKey(key);
        o1.setValue(valueVisitor.applyList(in, version));
        o1.setDataType(DataType.LIST);


        return context.valueOf(o1);
    }

    @Override
    public Event applySet(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<byte[]>> o2 = new KeyStringValueByteArrayIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o2.setValueRdbType(RDB_TYPE_SET);
        o2.setKey(key);
        o2.setValue(valueVisitor.applySet(in, version));
        o2.setDataType(DataType.SET);

//        o2.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o2);
    }

    @Override
    public Event applyZSet(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<ZSetEntry>> o3 = new KeyStringValueZSetEntryIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o3.setValueRdbType(RDB_TYPE_ZSET);
        o3.setKey(key);
        o3.setValue(valueVisitor.applyZSet(in, version));
        o3.setDataType(DataType.ZSET);

//        o3.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o3);
    }

    @Override
    public Event applyZSet2(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<ZSetEntry>> o5 = new KeyStringValueZSetEntryIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o5.setValueRdbType(RDB_TYPE_ZSET_2);
        o5.setKey(key);
        o5.setValue(valueVisitor.applyZSet2(in, version));
        o5.setDataType(DataType.ZSET);

//        o5.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o5);
    }

    @Override
    public Event applyHash(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<Map.Entry<byte[], byte[]>>> o4 = new KeyStringValueMapEntryIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o4.setValueRdbType(RDB_TYPE_HASH);
        o4.setKey(key);
        o4.setValue(valueVisitor.applyHash(in, version));
        o4.setDataType(DataType.HASH);

//        o4.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o4);
    }

    @Override
    public Event applyHashZipMap(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<Map.Entry<byte[], byte[]>>> o9 = new KeyStringValueMapEntryIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o9.setValueRdbType(RDB_TYPE_HASH_ZIPMAP);
        o9.setKey(key);
        o9.setValue(valueVisitor.applyHashZipMap(in, version));
        o9.setDataType(DataType.HASH);
//        o9.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o9);
    }

    @Override
    public Event applyListZipList(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<byte[]>> o10 = new KeyStringValueByteArrayIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o10.setValueRdbType(RDB_TYPE_LIST_ZIPLIST);
        o10.setKey(key);
        o10.setValue(valueVisitor.applyListZipList(in, version));
        o10.setDataType(DataType.LIST);

//        o10.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o10);
    }

    @Override
    public Event applySetIntSet(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<byte[]>> o11 = new KeyStringValueByteArrayIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o11.setValueRdbType(RDB_TYPE_SET_INTSET);
        o11.setKey(key);
        o11.setValue(valueVisitor.applySetIntSet(in, version));
        o11.setDataType(DataType.SET);

//        o11.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o11);
    }

    @Override
    public Event applyZSetZipList(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<ZSetEntry>> o12 = new KeyStringValueZSetEntryIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o12.setValueRdbType(RDB_TYPE_ZSET_ZIPLIST);
        o12.setKey(key);
        o12.setValue(valueVisitor.applyZSetZipList(in, version));
        o12.setDataType(DataType.ZSET);

//        o12.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o12);
    }

    @Override
    public Event applyHashZipList(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<Map.Entry<byte[], byte[]>>> o13 = new KeyStringValueMapEntryIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o13.setValueRdbType(RDB_TYPE_HASH_ZIPLIST);
        o13.setKey(key);
        o13.setValue(valueVisitor.applyHashZipList(in, version));
        o13.setDataType(DataType.HASH);

//        o13.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o13);
    }

    @Override
    public Event applyListQuickList(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], Iterator<byte[]>> o14 = new KeyStringValueByteArrayIterator();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o14.setValueRdbType(RDB_TYPE_LIST_QUICKLIST);
        o14.setKey(key);
        o14.setValue(valueVisitor.applyListQuickList(in, version));
        o14.setDataType(DataType.LIST);

//        o14.setSize(parser.rdbLoadLen().len);
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
    public Event applyString(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], byte[]> o0 = new DumpKeyValuePair();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o0.setValueRdbType(RDB_TYPE_STRING);
        o0.setKey(key);
        o0.setValue(valueVisitor.applyString(in, version));
        o0.setDataType(DataType.STRING);
//        System.out.println("++"+parser.rdbLoadLen().len);
//        o0.setSize(Long.valueOf(o0.getValue().length+key.length));
        return context.valueOf(o0);
    }

    @Override
    public Event applyModule(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], byte[]> o6 = new DumpKeyValuePair();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o6.setValueRdbType(RDB_TYPE_MODULE);
        o6.setKey(key);
        o6.setValue(valueVisitor.applyModule(in, version));
        o6.setDataType(DataType.MODULE);

//        o6.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o6);
    }

    @Override
    public Event applyModule2(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], byte[]> o7 = new DumpKeyValuePair();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o7.setValueRdbType(RDB_TYPE_MODULE_2);
        o7.setKey(key);
        o7.setValue(valueVisitor.applyModule2(in, version));
        o7.setDataType(DataType.MODULE);

//        o7.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o7);
    }

    @Override
    public Event applyStreamListPacks(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], byte[]> o15 = new DumpKeyValuePair();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        o15.setValueRdbType(RDB_TYPE_STREAM_LISTPACKS);
        o15.setKey(key);
        o15.setValue(valueVisitor.applyStreamListPacks(in, version));
        o15.setDataType(DataType.STREAM);
//        o15.setSize(parser.rdbLoadLen().len);
        return context.valueOf(o15);
    }



}