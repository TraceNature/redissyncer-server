package com.i1314i.syncerplusservice.replicator.listener;

import com.i1314i.syncerplusredis.event.Event;
import com.i1314i.syncerplusredis.rdb.datatype.*;
import com.i1314i.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import com.i1314i.syncerplusredis.rdb.iterable.datatype.KeyStringValueByteArrayIterator;
import com.i1314i.syncerplusredis.rdb.iterable.datatype.KeyStringValueMapEntryIterator;
import com.i1314i.syncerplusredis.rdb.iterable.datatype.KeyStringValueZSetEntryIterator;
import com.i1314i.syncerplusredis.replicator.Replicator;
import com.i1314i.syncerplusredis.util.objectutil.ByteArrayList;
import com.i1314i.syncerplusredis.util.objectutil.ByteArrayMap;
import com.i1314i.syncerplusredis.util.objectutil.ByteArraySet;

import com.i1314i.syncerplusredis.event.EventListener;


import java.util.*;

import static com.i1314i.syncerplusredis.rdb.datatype.KeyValuePairs.zset;
import static com.i1314i.syncerplusredis.replicator.Constants.RDB_TYPE_SET;
import static com.i1314i.syncerplusredis.replicator.Constants.RDB_TYPE_SET_INTSET;
import static com.i1314i.syncerplusredis.rdb.datatype.KeyValuePairs.*;

public class ValueDumpIterableEventListener implements EventListener {

    private final int batchSize;
    private final boolean order;
    private final EventListener listener;

    public ValueDumpIterableEventListener(EventListener listener) {
        this(64, listener);
    }

    public ValueDumpIterableEventListener(int batchSize, EventListener listener) {
        this(true, batchSize, listener);
    }

    public ValueDumpIterableEventListener(boolean order, int batchSize, EventListener listener) {
        if (batchSize <= 0) throw new IllegalArgumentException(String.valueOf(batchSize));
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
            listener.onEvent(replicator, string(ksvs, ksvs.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueByteArrayIterator) {
            if (type == RDB_TYPE_SET || type == RDB_TYPE_SET_INTSET) {
                KeyStringValueByteArrayIterator skv = (KeyStringValueByteArrayIterator) kv;
                Iterator<byte[]> it = skv.getValue();
                Set<byte[]> prev = null, next = new ByteArraySet(order, batchSize);
                while (it.hasNext()) {
                    next.add(it.next());
                    if (next.size() == batchSize) {
                        if (prev != null)
                            listener.onEvent(replicator, set(skv, prev, batch++, false));
                        prev = next;
                        next = create(order, batchSize);
                    }
                }
                final boolean last = next.isEmpty();
                listener.onEvent(replicator, set(skv, prev, batch++, last));
                if (!last) listener.onEvent(replicator, set(skv, next, batch++, true));
            } else {
                KeyStringValueByteArrayIterator lkv = (KeyStringValueByteArrayIterator) kv;
                Iterator<byte[]> it = lkv.getValue();
                List<byte[]> prev = null, next = new ByteArrayList(batchSize);
                while (it.hasNext()) {
                    try {
                        next.add(it.next());
                        if (next.size() == batchSize) {
                            if (prev != null)
                                listener.onEvent(replicator, list(lkv, prev, batch++, false));
                            prev = next;
                            next = new ByteArrayList(batchSize);
                        }
                    } catch (IllegalStateException ignore) {
                        // see ValueIterableRdbVisitor.QuickListIter.next().
                    }
                }
                final boolean last = next.isEmpty();
                listener.onEvent(replicator, list(lkv, prev, batch++, last));
                if (!last) listener.onEvent(replicator, list(lkv, next, batch++, true));
            }
        } else if (kv instanceof KeyStringValueMapEntryIterator) {
            KeyStringValueMapEntryIterator mkv = (KeyStringValueMapEntryIterator) kv;
            Iterator<Map.Entry<byte[], byte[]>> it = mkv.getValue();
            Map<byte[], byte[]> prev = null, next = new ByteArrayMap(order, batchSize);
            while (it.hasNext()) {
                Map.Entry<byte[], byte[]> entry = it.next();
                next.put(entry.getKey(), entry.getValue());
                if (next.size() == batchSize) {
                    if (prev != null)
                        listener.onEvent(replicator, hash(mkv, prev, batch++, false));
                    prev = next;
                    next = new ByteArrayMap(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replicator, hash(mkv, prev, batch++, last));
            if (!last) listener.onEvent(replicator, hash(mkv, next, batch++, true));
        } else if (kv instanceof KeyStringValueZSetEntryIterator) {
            KeyStringValueZSetEntryIterator zkv = (KeyStringValueZSetEntryIterator) kv;
            Iterator<ZSetEntry> it = zkv.getValue();
            Set<ZSetEntry> prev = null, next = create(order, batchSize);
            while (it.hasNext()) {
                next.add(it.next());
                if (next.size() == batchSize) {
                    if (prev != null)
                        listener.onEvent(replicator, zset(zkv, prev, batch++, false));
                    prev = next;
                    next = create(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replicator, zset(zkv, prev, batch++, last));
            if (!last) listener.onEvent(replicator, zset(zkv, next, batch++, true));
        } else if (kv instanceof KeyStringValueModule) {
            listener.onEvent(replicator, module((KeyStringValueModule) kv, (Module) kv.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueStream) {
            listener.onEvent(replicator, stream((KeyStringValueStream) kv, (Stream) kv.getValue(), batch, true));
        }else if(kv instanceof DumpKeyValuePair){
            listener.onEvent(replicator,kv);
        }
    }

    private <T> Set<T> create(boolean order, int batchSize) {
        return order ? new LinkedHashSet<T>(batchSize) : new HashSet<T>(batchSize);
    }

}