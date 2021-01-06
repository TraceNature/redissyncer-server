package syncer.replica.listener;

import syncer.replica.event.Event;
import syncer.replica.rdb.datatype.*;
import syncer.replica.rdb.iterable.datatype.KeyStringValueByteArrayIterator;
import syncer.replica.rdb.iterable.datatype.KeyStringValueMapEntryIterator;
import syncer.replica.rdb.iterable.datatype.KeyStringValueZSetEntryIterator;
import syncer.replica.rdb.sync.datatype.DumpKeyValuePair;
import syncer.replica.replication.Replication;
import syncer.replica.util.objectutil.ByteArrayList;
import syncer.replica.util.objectutil.ByteArrayMap;
import syncer.replica.util.objectutil.ByteArraySet;

import java.util.*;

import static syncer.replica.constant.Constants.RDB_TYPE_SET;
import static syncer.replica.constant.Constants.RDB_TYPE_SET_INTSET;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/24
 */
public class ValueDumpIterableEventListener implements EventListener{
    private final int batchSize;
    private final boolean order;
    private final EventListener listener;

    public ValueDumpIterableEventListener(EventListener listener) {
        this(64, listener);
    }

    public ValueDumpIterableEventListener(int batchSize,EventListener listener) {
        this(true, batchSize, listener);
    }

    public ValueDumpIterableEventListener(boolean order, int batchSize, EventListener listener) {
        if (batchSize <= 0){
            throw new IllegalArgumentException(String.valueOf(batchSize));
        }
        this.order = order;
        this.batchSize = batchSize;
        this.listener = listener;
    }



    @Override
    public void onEvent(Replication replication, Event event) {
        if (!(event instanceof KeyValuePair<?, ?>)) {
            listener.onEvent(replication, event);
            return;
        }
        KeyValuePair<?, ?> kv = (KeyValuePair<?, ?>) event;
        // Note that:
        // Every Iterator MUST be consumed.
        // Before every it.next() MUST check precondition it.hasNext()
        int batch = 0;
        final int type = kv.getValueRdbType();
        if (kv instanceof KeyStringValueString) {
            KeyStringValueString ksvs = (KeyStringValueString) kv;
            listener.onEvent(replication, KeyValuePairs.string(ksvs, ksvs.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueByteArrayIterator) {
            if (type == RDB_TYPE_SET || type == RDB_TYPE_SET_INTSET) {
                KeyStringValueByteArrayIterator skv = (KeyStringValueByteArrayIterator) kv;
                Iterator<byte[]> it = skv.getValue();
                Set<byte[]> prev = null, next = new ByteArraySet(order, batchSize);
                while (it.hasNext()) {
                    next.add(it.next());
                    if (next.size() == batchSize) {
                        if (prev != null) {
                            listener.onEvent(replication, KeyValuePairs.set(skv, prev, batch++, false));
                        }
                        prev = next;
                        next = create(order, batchSize);
                    }
                }
                final boolean last = next.isEmpty();
                listener.onEvent(replication, KeyValuePairs.set(skv, prev, batch++, last));
                if (!last){
                    listener.onEvent(replication, KeyValuePairs.set(skv, next, batch++, true));
                }
            } else {
                KeyStringValueByteArrayIterator lkv = (KeyStringValueByteArrayIterator) kv;
                Iterator<byte[]> it = lkv.getValue();
                List<byte[]> prev = null, next = new ByteArrayList(batchSize);
                while (it.hasNext()) {
                    try {
                        next.add(it.next());
                        if (next.size() == batchSize) {
                            if (prev != null) {
                                listener.onEvent(replication, KeyValuePairs.list(lkv, prev, batch++, false));
                            }
                            prev = next;
                            next = new ByteArrayList(batchSize);
                        }
                    } catch (IllegalStateException ignore) {
                        // see ValueIterableRdbVisitor.QuickListIter.next().
                    }
                }
                final boolean last = next.isEmpty();
                listener.onEvent(replication, KeyValuePairs.list(lkv, prev, batch++, last));
                if (!last){
                    listener.onEvent(replication, KeyValuePairs.list(lkv, next, batch++, true));
                }
            }
        } else if (kv instanceof KeyStringValueMapEntryIterator) {
            KeyStringValueMapEntryIterator mkv = (KeyStringValueMapEntryIterator) kv;
            Iterator<Map.Entry<byte[], byte[]>> it = mkv.getValue();
            Map<byte[], byte[]> prev = null, next = new ByteArrayMap(order, batchSize);
            while (it.hasNext()) {
                Map.Entry<byte[], byte[]> entry = it.next();
                next.put(entry.getKey(), entry.getValue());
                if (next.size() == batchSize) {
                    if (prev != null) {
                        listener.onEvent(replication, KeyValuePairs.hash(mkv, prev, batch++, false));
                    }
                    prev = next;
                    next = new ByteArrayMap(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replication, KeyValuePairs.hash(mkv, prev, batch++, last));
            if (!last){
                listener.onEvent(replication, KeyValuePairs.hash(mkv, next, batch++, true));
            }
        } else if (kv instanceof KeyStringValueZSetEntryIterator) {
            KeyStringValueZSetEntryIterator zkv = (KeyStringValueZSetEntryIterator) kv;
            Iterator<ZSetEntry> it = zkv.getValue();
            Set<ZSetEntry> prev = null, next = create(order, batchSize);
            while (it.hasNext()) {
                next.add(it.next());
                if (next.size() == batchSize) {
                    if (prev != null) {
                        listener.onEvent(replication, KeyValuePairs.zset(zkv, prev, batch++, false));
                    }
                    prev = next;
                    next = create(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replication, KeyValuePairs.zset(zkv, prev, batch++, last));
            if (!last){
                listener.onEvent(replication, KeyValuePairs.zset(zkv, next, batch++, true));
            }
        } else if (kv instanceof KeyStringValueModule) {
            listener.onEvent(replication, KeyValuePairs.module((KeyStringValueModule) kv, (Module) kv.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueStream) {
            listener.onEvent(replication, KeyValuePairs.stream((KeyStringValueStream) kv, (Stream) kv.getValue(), batch, true));
        }else if(kv instanceof DumpKeyValuePair){
            listener.onEvent(replication,kv);
        }
    }

    private <T> Set<T> create(boolean order, int batchSize) {
        return order ? new LinkedHashSet<T>(batchSize) : new HashSet<T>(batchSize);
    }
}
