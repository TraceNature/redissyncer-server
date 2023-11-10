package syncer.replica.parser.iterable;

import syncer.replica.constant.Constants;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.datatype.rdb.stream.Stream;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.event.*;
import syncer.replica.event.iter.KeyStringValueByteArrayIteratorEvent;
import syncer.replica.event.iter.KeyStringValueMapEntryIteratorEvent;
import syncer.replica.event.iter.KeyStringValueZSetEntryIteratorEvent;
import syncer.replica.kv.KeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.replica.listener.EventListener;
import syncer.replica.util.list.ByteArrayList;
import syncer.replica.util.map.ByteArrayMap;
import syncer.replica.util.set.ByteArraySet;

import java.util.*;

public class IterableEventListener implements EventListener {
    private final int batchSize;
    private final boolean order;
    private final EventListener listener;

    public IterableEventListener(EventListener listener) {
        this(64, listener);
    }

    public IterableEventListener(int batchSize, EventListener listener) {
        this(true, batchSize, listener);
    }



    @Override
    public String eventListenerName() {
        return "IterableEventListener";
    }

    public IterableEventListener(boolean order, int batchSize, EventListener listener) {
        if (batchSize <= 0){
            throw new IllegalArgumentException("batchSize不能小于0"+String.valueOf(batchSize));
        }
        this.order = order;
        this.batchSize = batchSize;
        this.listener = listener;
    }


    @Override
    public void onEvent(Replication replication, Event event) {
        //不为set list hash结构时
        if (!(event instanceof KeyValuePairEvent<?, ?>)) {
            listener.onEvent(replication, event);
            return;
        }

        KeyValuePairEvent<?, ?> kv = (KeyValuePairEvent<?, ?>) event;
        // Note that:
        // Every Iterator MUST be consumed.
        // Before every it.next() MUST check precondition it.hasNext()
        int batch = 0;
        final int type = kv.getValueRdbType();

        //String
        if (kv instanceof KeyStringValueStringEvent) {
            System.out.println("------");
            KeyStringValueStringEvent ksvs = (KeyStringValueStringEvent) kv;
            listener.onEvent(replication, KeyValuePairs.string(ksvs, ksvs.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueByteArrayIteratorEvent) {
            if (type == Constants.RDB_TYPE_SET || type == Constants.RDB_TYPE_SET_INTSET) {
                KeyStringValueByteArrayIteratorEvent skv = (KeyStringValueByteArrayIteratorEvent) kv;
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
                if (!last) {
                    listener.onEvent(replication, KeyValuePairs.set(skv, next, batch++, true));
                }
            }else {
                //list
                KeyStringValueByteArrayIteratorEvent lkv = (KeyStringValueByteArrayIteratorEvent) kv;
                Iterator<byte[]> it = lkv.getValue();
                List<byte[]> prev = null, next = new ByteArrayList(batchSize);
                while (it.hasNext()) {
                    try {
                        next.add(it.next());
                        if (next.size() == batchSize) {
                            if (prev != null)
                            {
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
        }else if(kv instanceof KeyStringValueMapEntryIteratorEvent){
            KeyStringValueMapEntryIteratorEvent mkv = (KeyStringValueMapEntryIteratorEvent) kv;
            Iterator<Map.Entry<byte[], byte[]>> it = mkv.getValue();
            Map<byte[], byte[]> prev = null, next = new ByteArrayMap(order, batchSize);
            while (it.hasNext()) {
                Map.Entry<byte[], byte[]> entry = it.next();
                next.put(entry.getKey(), entry.getValue());
                if (next.size() == batchSize) {
                    if (prev != null)
                    {
                        listener.onEvent(replication, KeyValuePairs.hash(mkv, prev, batch++, false));
                    }
                    prev = next;
                    next = new ByteArrayMap(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replication, KeyValuePairs.hash(mkv, prev, batch++, last));
            if (!last) {
                listener.onEvent(replication, KeyValuePairs.hash(mkv, next, batch++, true));
            }
        }else if(kv instanceof KeyStringValueZSetEntryIteratorEvent){
            KeyStringValueZSetEntryIteratorEvent zkv = (KeyStringValueZSetEntryIteratorEvent) kv;
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
            if (!last) {
                listener.onEvent(replication, KeyValuePairs.zset(zkv, next, batch++, true));
            }
        }else if (kv instanceof KeyStringValueModuleEvent) {
            listener.onEvent(replication, KeyValuePairs.module((KeyStringValueModuleEvent) kv, (Module) kv.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueStreamEvent) {
            listener.onEvent(replication, KeyValuePairs.stream((KeyStringValueStreamEvent) kv, (Stream) kv.getValue(), batch, true));
        }

    }

    private <T> Set<T> create(boolean order, int batchSize) {
        return order ? new LinkedHashSet<T>(batchSize) : new HashSet<T>(batchSize);
    }
}
