
package syncer.syncerreplication.rdb.iterable;
import syncer.syncerreplication.event.Event;
import syncer.syncerreplication.event.EventListener;
import syncer.syncerreplication.rdb.datatype.*;
import syncer.syncerreplication.rdb.iterable.datatype.KeyStringValueByteArrayIterator;
import syncer.syncerreplication.rdb.iterable.datatype.KeyStringValueMapEntryIterator;
import syncer.syncerreplication.rdb.iterable.datatype.KeyStringValueZSetEntryIterator;
import syncer.syncerreplication.replicator.Replicator;
import syncer.syncerreplication.util.objectUtils.ByteArrayList;
import syncer.syncerreplication.util.objectUtils.ByteArrayMap;
import syncer.syncerreplication.util.objectUtils.ByteArraySet;

import java.util.*;

import static syncer.syncerreplication.constant.Constants.RDB_TYPE_SET;
import static syncer.syncerreplication.constant.Constants.RDB_TYPE_SET_INTSET;

/**
 * @author Leon Chen
 * @since 2.5.0
 */
public class ValueIterableEventListener implements EventListener {
    
    private final int batchSize;
    private final boolean order;
    private final EventListener listener;
    
    public ValueIterableEventListener(EventListener listener) {
        this(64, listener);
    }
    
    public ValueIterableEventListener(int batchSize, EventListener listener) {
        this(true, batchSize, listener);
    }
    
    public ValueIterableEventListener(boolean order, int batchSize, EventListener listener) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException(String.valueOf(batchSize));
        }
        this.order = order;
        this.batchSize = batchSize;
        this.listener = listener;
    }
    
    @Override
    public void onEvent(Replicator replicator, Event event) {
        if (!(event instanceof KeyValuePair<?, ?>)) {
            listener.onEvent(replicator, event);
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
            listener.onEvent(replicator, KeyValuePairs.string(ksvs, ksvs.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueByteArrayIterator) {
            if (type == RDB_TYPE_SET || type == RDB_TYPE_SET_INTSET) {
                KeyStringValueByteArrayIterator skv = (KeyStringValueByteArrayIterator) kv;
                Iterator<byte[]> it = skv.getValue();
                Set<byte[]> prev = null, next = new ByteArraySet(order, batchSize);
                while (it.hasNext()) {
                    next.add(it.next());
                    if (next.size() == batchSize) {
                        if (prev != null) {
                            listener.onEvent(replicator, KeyValuePairs.set(skv, prev, batch++, false));
                        }
                        prev = next;
                        next = create(order, batchSize);
                    }
                }
                final boolean last = next.isEmpty();
                listener.onEvent(replicator, KeyValuePairs.set(skv, prev, batch++, last));
                if (!last){
                    listener.onEvent(replicator, KeyValuePairs.set(skv, next, batch++, true));
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
                                listener.onEvent(replicator, KeyValuePairs.list(lkv, prev, batch++, false));
                            }
                            prev = next;
                            next = new ByteArrayList(batchSize);
                        }
                    } catch (IllegalStateException ignore) {
                        // see ValueIterableRdbVisitor.QuickListIter.next().
                    }
                }
                final boolean last = next.isEmpty();
                listener.onEvent(replicator, KeyValuePairs.list(lkv, prev, batch++, last));
                if (!last) {
                    listener.onEvent(replicator, KeyValuePairs.list(lkv, next, batch++, true));
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
                        listener.onEvent(replicator, KeyValuePairs.hash(mkv, prev, batch++, false));
                    }
                    prev = next;
                    next = new ByteArrayMap(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replicator, KeyValuePairs.hash(mkv, prev, batch++, last));
            if (!last){
                listener.onEvent(replicator, KeyValuePairs.hash(mkv, next, batch++, true));
            }
        } else if (kv instanceof KeyStringValueZSetEntryIterator) {
            KeyStringValueZSetEntryIterator zkv = (KeyStringValueZSetEntryIterator) kv;
            Iterator<ZSetEntry> it = zkv.getValue();
            Set<ZSetEntry> prev = null, next = create(order, batchSize);
            while (it.hasNext()) {
                next.add(it.next());
                if (next.size() == batchSize) {
                    if (prev != null) {
                        listener.onEvent(replicator, KeyValuePairs.zset(zkv, prev, batch++, false));
                    }
                    prev = next;
                    next = create(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replicator, KeyValuePairs.zset(zkv, prev, batch++, last));
            if (!last){
                listener.onEvent(replicator, KeyValuePairs.zset(zkv, next, batch++, true));
            }
        } else if (kv instanceof KeyStringValueModule) {
            listener.onEvent(replicator, KeyValuePairs.module((KeyStringValueModule) kv, (Module) kv.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueStream) {
            listener.onEvent(replicator, KeyValuePairs.stream((KeyStringValueStream) kv, (Stream) kv.getValue(), batch, true));
        }
    }
    
    private <T> Set<T> create(boolean order, int batchSize) {
        return order ? new LinkedHashSet<T>(batchSize) : new HashSet<T>(batchSize);
    }
}
