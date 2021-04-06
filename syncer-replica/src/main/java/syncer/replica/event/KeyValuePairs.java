package syncer.replica.event;


import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.datatype.rdb.stream.Stream;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.event.iter.KeyStringValueByteArrayIteratorEvent;
import syncer.replica.event.iter.KeyStringValueMapEntryIteratorEvent;
import syncer.replica.event.iter.datatype.*;
import syncer.replica.kv.KeyValuePairEvent;
import syncer.replica.util.type.KvDataType;

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
    public static KeyValuePairEvent<byte[], byte[]> string(KeyValuePairEvent<byte[], ?> raw, byte[] value) {
        KeyStringValueStringEvent kv = new KeyStringValueStringEvent();
        copy(raw, kv);
        kv.setValue(value);
        return kv;
    }

    public static KeyValuePairEvent<byte[], Module> module(KeyValuePairEvent<byte[], ?> raw, Module value) {
        KeyStringValueModuleEvent kv = new KeyStringValueModuleEvent();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(KvDataType.MODULE);
        return kv;
    }

    public static KeyValuePairEvent<byte[], Map<byte[], byte[]>> hash(KeyValuePairEvent<byte[], ?> raw, Map<byte[], byte[]> value) {
        KeyStringValueHashEvent kv = new KeyStringValueHashEvent();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(KvDataType.HASH);
        return kv;
    }



    public static KeyValuePairEvent<byte[], Set<byte[]>> set(KeyValuePairEvent<byte[], ?> raw, Set<byte[]> value) {
        KeyStringValueSetEvent kv = new KeyStringValueSetEvent();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(KvDataType.SET);
        return kv;
    }

    public static KeyValuePairEvent<byte[], Set<ZSetEntry>> zset(KeyValuePairEvent<byte[], ?> raw, Set<ZSetEntry> value) {
        KeyStringValueZSetEvent kv = new KeyStringValueZSetEvent();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(KvDataType.ZSET);
        return kv;
    }

    public static KeyValuePairEvent<byte[], Stream> stream(KeyValuePairEvent<byte[], ?> raw, Stream value) {
        KeyStringValueStreamEvent kv = new KeyStringValueStreamEvent();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(KvDataType.STREAM);
        return kv;
    }

    /*
     * Iterator
     */
    public static KeyStringValueMapEntryIteratorEvent iterHash(KeyValuePairEvent<byte[], ?> raw, Iterator<Map.Entry<byte[], byte[]>> value) {
        KeyStringValueMapEntryIteratorEvent kv = new KeyStringValueMapEntryIteratorEvent();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(KvDataType.HASH);
        return kv;
    }

    public static KeyStringValueByteArrayIteratorEvent iterList(KeyValuePairEvent<byte[], ?> raw, Iterator<byte[]> value) {
        KeyStringValueByteArrayIteratorEvent kv = new KeyStringValueByteArrayIteratorEvent();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(KvDataType.LIST);
        return kv;
    }

    public static KeyStringValueByteArrayIteratorEvent iterSet(KeyValuePairEvent<byte[], ?> raw, Iterator<byte[]> value) {
        KeyStringValueByteArrayIteratorEvent kv = new KeyStringValueByteArrayIteratorEvent();
        copy(raw, kv);
        kv.setValue(value);
        kv.setDataType(KvDataType.SET);
        return kv;
    }



    /*
     * Batched
     */
    public static BatchedKeyStringValueStringEvent string(KeyValuePairEvent<byte[], ?> raw, byte[] value, int batch, boolean last) {
        BatchedKeyStringValueStringEvent kv = new BatchedKeyStringValueStringEvent();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(KvDataType.STRING);
        return kv;
    }

    public static BatchedKeyStringValueModuleEvent module(KeyValuePairEvent<byte[], ?> raw, Module value, int batch, boolean last) {
        BatchedKeyStringValueModuleEvent kv = new BatchedKeyStringValueModuleEvent();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(KvDataType.MODULE);
        return kv;
    }

    public static BatchedKeyStringValueHashEvent hash(KeyValuePairEvent<byte[], ?> raw, Map<byte[], byte[]> value, int batch, boolean last) {
        BatchedKeyStringValueHashEvent kv = new BatchedKeyStringValueHashEvent();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(KvDataType.HASH);
        return kv;
    }

    public static BatchedKeyStringValueListEvent list(KeyValuePairEvent<byte[], ?> raw, List<byte[]> value, int batch, boolean last) {
        BatchedKeyStringValueListEvent kv = new BatchedKeyStringValueListEvent();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(KvDataType.LIST);
        return kv;
    }

    public static BatchedKeyStringValueSetEvent set(KeyValuePairEvent<byte[], ?> raw, Set<byte[]> value, int batch, boolean last) {
        BatchedKeyStringValueSetEvent kv = new BatchedKeyStringValueSetEvent();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(KvDataType.SET);
        return kv;
    }

    public static BatchedKeyStringValueZSetEvent zset(KeyValuePairEvent<byte[], ?> raw, Set<ZSetEntry> value, int batch, boolean last) {
        BatchedKeyStringValueZSetEvent kv = new BatchedKeyStringValueZSetEvent();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(KvDataType.ZSET);
        return kv;
    }

    public static BatchedKeyStringValueStreamEvent stream(KeyValuePairEvent<byte[], ?> raw, Stream value, int batch, boolean last) {
        BatchedKeyStringValueStreamEvent kv = new BatchedKeyStringValueStreamEvent();
        copy(raw, kv, batch, last);
        kv.setValue(value);
        kv.setDataType(KvDataType.STREAM);
        return kv;
    }

    /*
     * Helper
     */
    private static void copy(KeyValuePairEvent<byte[], ?> source, KeyValuePairEvent<byte[], ?> target) {
        target.setContext(source.getContext());
        target.setDb(source.getDb());
        target.setExpiredType(source.getExpiredType());
        target.setExpiredValue(source.getExpiredValue());
        target.setEvictType(source.getEvictType());
        target.setEvictValue(source.getEvictValue());
        target.setValueRdbType(source.getValueRdbType());
        target.setKey(source.getKey());
    }

    private static void copy(KeyValuePairEvent<byte[], ?> source, BatchedKeyValuePairEvent<byte[], ?> target, int batch, boolean last) {
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
