/*
 * Copyright 2016-2018 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package syncer.replica.rdb.iterable;


import syncer.replica.event.Event;
import syncer.replica.rdb.datatype.*;
import  syncer.replica.listener.EventListener;
import syncer.replica.rdb.iterable.datatype.KeyStringValueByteArrayIterator;
import syncer.replica.rdb.iterable.datatype.KeyStringValueMapEntryIterator;
import syncer.replica.rdb.iterable.datatype.KeyStringValueZSetEntryIterator;
import syncer.replica.replication.Replication;
import syncer.replica.util.objectutil.ByteArrayList;
import syncer.replica.util.objectutil.ByteArrayMap;
import syncer.replica.util.objectutil.ByteArraySet;

import java.util.*;

import static syncer.replica.constant.Constants.*;
import static syncer.replica.rdb.datatype.KeyValuePairs.*;


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
        if (batchSize <= 0){
            throw new IllegalArgumentException(String.valueOf(batchSize));
        }
        this.order = order;
        this.batchSize = batchSize;
        this.listener = listener;
    }
    
    @Override
    public void onEvent(Replication replicator, Event event) {
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
                        {
                            listener.onEvent(replicator, set(skv, prev, batch++, false));
                        }
                        prev = next;
                        next = create(order, batchSize);
                    }
                }
                final boolean last = next.isEmpty();
                listener.onEvent(replicator, set(skv, prev, batch++, last));
                if (!last) {
                    listener.onEvent(replicator, set(skv, next, batch++, true));
                }
            } else {
                KeyStringValueByteArrayIterator lkv = (KeyStringValueByteArrayIterator) kv;
                Iterator<byte[]> it = lkv.getValue();
                List<byte[]> prev = null, next = new ByteArrayList(batchSize);
                while (it.hasNext()) {
                    try {
                        next.add(it.next());
                        if (next.size() == batchSize) {
                            if (prev != null)
                            {
                                listener.onEvent(replicator, list(lkv, prev, batch++, false));
                            }
                            prev = next;
                            next = new ByteArrayList(batchSize);
                        }
                    } catch (IllegalStateException ignore) {
                        // see ValueIterableRdbVisitor.QuickListIter.next().
                    }
                }
                final boolean last = next.isEmpty();
                listener.onEvent(replicator, list(lkv, prev, batch++, last));
                if (!last){
                    listener.onEvent(replicator, list(lkv, next, batch++, true));
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
                    if (prev != null)
                    {
                        listener.onEvent(replicator, hash(mkv, prev, batch++, false));
                    }
                    prev = next;
                    next = new ByteArrayMap(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replicator, hash(mkv, prev, batch++, last));
            if (!last) {
                listener.onEvent(replicator, hash(mkv, next, batch++, true));
            }
        } else if (kv instanceof KeyStringValueZSetEntryIterator) {
            KeyStringValueZSetEntryIterator zkv = (KeyStringValueZSetEntryIterator) kv;
            Iterator<ZSetEntry> it = zkv.getValue();
            Set<ZSetEntry> prev = null, next = create(order, batchSize);
            while (it.hasNext()) {
                next.add(it.next());
                if (next.size() == batchSize) {
                    if (prev != null)
                    {
                        listener.onEvent(replicator, zset(zkv, prev, batch++, false));
                    }
                    prev = next;
                    next = create(order, batchSize);
                }
            }
            final boolean last = next.isEmpty();
            listener.onEvent(replicator, zset(zkv, prev, batch++, last));
            if (!last) {
                listener.onEvent(replicator, zset(zkv, next, batch++, true));
            }
        } else if (kv instanceof KeyStringValueModule) {
            listener.onEvent(replicator, module((KeyStringValueModule) kv, (Module) kv.getValue(), batch, true));
        } else if (kv instanceof KeyStringValueStream) {
            listener.onEvent(replicator, stream((KeyStringValueStream) kv, (Stream) kv.getValue(), batch, true));
        }
    }
    
    private <T> Set<T> create(boolean order, int batchSize) {
        return order ? new LinkedHashSet<T>(batchSize) : new HashSet<T>(batchSize);
    }
}
