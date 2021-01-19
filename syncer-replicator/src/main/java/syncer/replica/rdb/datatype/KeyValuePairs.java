package syncer.replica.rdb.datatype;

import syncer.replica.rdb.iterable.datatype.*;
import syncer.replica.rdb.sync.datatype.DataType;

import javax.xml.crypto.Data;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public class KeyValuePairs {

    /*
     * Base
     */
    public static KeyValuePair<byte[], byte[]> string(KeyValuePair<byte[], ?> raw, byte[] value) {
        KeyStringValueString kv = new KeyStringValueString();
        copy(raw, kv);
        kv.setValue(value);
        return kv;
    }

    public static KeyValuePair<byte[], Module> module(KeyValuePair<byte[], ?> raw, Module value) {
        KeyStringValueModule kv = new KeyStringValueModule();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.MODULE);
        return kv;
    }

    public static KeyValuePair<byte[], Map<byte[], byte[]>> hash(KeyValuePair<byte[], ?> raw, Map<byte[], byte[]> value) {
        KeyStringValueHash kv = new KeyStringValueHash();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.HASH);
        return kv;
    }

    public static KeyValuePair<byte[], List<byte[]>> list(KeyValuePair<byte[], ?> raw, List<byte[]> value) {
        KeyStringValueList kv = new KeyStringValueList();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.LIST);
        return kv;
    }

    public static KeyValuePair<byte[], Set<byte[]>> set(KeyValuePair<byte[], ?> raw, Set<byte[]> value) {
        KeyStringValueSet kv = new KeyStringValueSet();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.SET);
        return kv;
    }

    public static KeyValuePair<byte[], Set<ZSetEntry>> zset(KeyValuePair<byte[], ?> raw, Set<ZSetEntry> value) {
        KeyStringValueZSet kv = new KeyStringValueZSet();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.ZSET);
        return kv;
    }

    public static KeyValuePair<byte[], Stream> stream(KeyValuePair<byte[], ?> raw, Stream value) {
        KeyStringValueStream kv = new KeyStringValueStream();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.STREAM);
        return kv;
    }

    /*
     * Iterator
     */
    public static KeyStringValueMapEntryIterator iterHash(KeyValuePair<byte[], ?> raw, Iterator<Map.Entry<byte[], byte[]>> value) {
        KeyStringValueMapEntryIterator kv = new KeyStringValueMapEntryIterator();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.HASH);
        return kv;
    }

    public static KeyStringValueByteArrayIterator iterList(KeyValuePair<byte[], ?> raw, Iterator<byte[]> value) {
        KeyStringValueByteArrayIterator kv = new KeyStringValueByteArrayIterator();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.LIST);
        return kv;
    }

    public static KeyStringValueByteArrayIterator iterSet(KeyValuePair<byte[], ?> raw, Iterator<byte[]> value) {
        KeyStringValueByteArrayIterator kv = new KeyStringValueByteArrayIterator();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.SET);
        return kv;
    }

    public static KeyStringValueZSetEntryIterator iterZset(KeyValuePair<byte[], ?> raw, Iterator<ZSetEntry> value) {
        KeyStringValueZSetEntryIterator kv = new KeyStringValueZSetEntryIterator();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(DataType.ZSET);
        return kv;
    }

    /*
     * Batched
     */
    public static BatchedKeyStringValueString string(KeyValuePair<byte[], ?> raw, byte[] value, int batch, boolean last) {
        BatchedKeyStringValueString kv = new BatchedKeyStringValueString();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(DataType.STRING);
        return kv;
    }

    public static BatchedKeyStringValueModule module(KeyValuePair<byte[], ?> raw, Module value, int batch, boolean last) {
        BatchedKeyStringValueModule kv = new BatchedKeyStringValueModule();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(DataType.MODULE);
        return kv;
    }

    public static BatchedKeyStringValueHash hash(KeyValuePair<byte[], ?> raw, Map<byte[], byte[]> value, int batch, boolean last) {
        BatchedKeyStringValueHash kv = new BatchedKeyStringValueHash();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(DataType.HASH);
        return kv;
    }

    public static BatchedKeyStringValueList list(KeyValuePair<byte[], ?> raw, List<byte[]> value, int batch, boolean last) {
        BatchedKeyStringValueList kv = new BatchedKeyStringValueList();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(DataType.LIST);
        return kv;
    }

    public static BatchedKeyStringValueSet set(KeyValuePair<byte[], ?> raw, Set<byte[]> value, int batch, boolean last) {
        BatchedKeyStringValueSet kv = new BatchedKeyStringValueSet();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(DataType.SET);
        return kv;
    }

    public static BatchedKeyStringValueZSet zset(KeyValuePair<byte[], ?> raw, Set<ZSetEntry> value, int batch, boolean last) {
        BatchedKeyStringValueZSet kv = new BatchedKeyStringValueZSet();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(DataType.ZSET);
        return kv;
    }

    public static BatchedKeyStringValueStream stream(KeyValuePair<byte[], ?> raw, Stream value, int batch, boolean last) {
        BatchedKeyStringValueStream kv = new BatchedKeyStringValueStream();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(DataType.STREAM);
        return kv;
    }

    /*
     * Helper
     */
    private static void copy(KeyValuePair<byte[], ?> source, KeyValuePair<byte[], ?> target) {
        target.setContext(source.getContext());
        target.setDb(source.getDb());
        target.setExpiredType(source.getExpiredType());
        target.setExpiredValue(source.getExpiredValue());
        target.setEvictType(source.getEvictType());
        target.setEvictValue(source.getEvictValue());
        target.setValueRdbType(source.getValueRdbType());
        target.setKey(source.getKey());
    }

    private static void copy(KeyValuePair<byte[], ?> source, BatchedKeyValuePair<byte[], ?> target, int batch, boolean last) {
        target.setContext(source.getContext());
        target.setDb(source.getDb());
        target.setExpiredType(source.getExpiredType());
        target.setExpiredValue(source.getExpiredValue());
        target.setEvictType(source.getEvictType());
        target.setEvictValue(source.getEvictValue());
        target.setValueRdbType(source.getValueRdbType());
        target.setKey(source.getKey());
        target.setBatch(batch);
        target.setLast(last);
    }
}
