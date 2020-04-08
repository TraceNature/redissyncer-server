/*
 * Copyright 2016-2017 Leon Chen
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

package syncer.syncerreplication.rdb.dump.parser;


import syncer.syncerplusredis.event.EventListener;
import syncer.syncerplusredis.io.RedisInputStream;
import syncer.syncerplusredis.rdb.RdbValueVisitor;
import syncer.syncerplusredis.rdb.datatype.KeyValuePair;
import syncer.syncerplusredis.rdb.datatype.KeyValuePairs;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.ValueIterableEventListener;
import syncer.syncerplusredis.rdb.iterable.ValueIterableRdbValueVisitor;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.objectutil.ByteArray;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import static syncer.syncerplusredis.replicator.Constants.*;

/**
 * @author Leon Chen
 * @since 3.1.0
 */
public class IterableDumpValueParser implements DumpValueParser {

    protected final int batchSize;
    protected final boolean order;
    protected final Replicator replicator;
    protected final RdbValueVisitor valueVisitor;

    public IterableDumpValueParser(Replicator replicator) {
        this(64, replicator);
    }

    public IterableDumpValueParser(int batchSize, Replicator replicator) {
        this(true, batchSize, replicator);
    }

    public IterableDumpValueParser(boolean order, int batchSize, Replicator replicator) {
        Objects.requireNonNull(replicator);
        this.order = order;
        this.batchSize = batchSize;
        this.replicator = replicator;
        this.valueVisitor = new ValueIterableRdbValueVisitor(replicator);
    }
    @Override
    public void parse(DumpKeyValuePair kv, EventListener listener) {
        Objects.requireNonNull(listener);
        new ValueIterableEventListener(order, batchSize, listener).onEvent(replicator, parse(kv));
    }
    @Override
    public KeyValuePair<?, ?> parse(DumpKeyValuePair kv) {
        Objects.requireNonNull(kv);
        try (RedisInputStream in = new RedisInputStream(new ByteArray(kv.getValue()))) {
            int valueType = in.read();
            switch (valueType) {
                case RDB_TYPE_STRING:
                    return KeyValuePairs.string(kv, valueVisitor.applyString(in, 0));
                case RDB_TYPE_LIST:
                    return KeyValuePairs.iterList(kv, valueVisitor.applyList(in, 0));
                case RDB_TYPE_SET:
                    return KeyValuePairs.iterSet(kv, valueVisitor.applySet(in, 0));
                case RDB_TYPE_ZSET:
                    return KeyValuePairs.iterZset(kv, valueVisitor.applyZSet(in, 0));
                case RDB_TYPE_ZSET_2:
                    return KeyValuePairs.iterZset(kv, valueVisitor.applyZSet2(in, 0));
                case RDB_TYPE_HASH:
                    return KeyValuePairs.iterHash(kv, valueVisitor.applyHash(in, 0));
                case RDB_TYPE_HASH_ZIPMAP:
                    return KeyValuePairs.iterHash(kv, valueVisitor.applyHashZipMap(in, 0));
                case RDB_TYPE_LIST_ZIPLIST:
                    return KeyValuePairs.iterList(kv, valueVisitor.applyListZipList(in, 0));
                case RDB_TYPE_SET_INTSET:
                    return KeyValuePairs.iterSet(kv, valueVisitor.applySetIntSet(in, 0));
                case RDB_TYPE_ZSET_ZIPLIST:
                    return KeyValuePairs.iterZset(kv, valueVisitor.applyZSetZipList(in, 0));
                case RDB_TYPE_HASH_ZIPLIST:
                    return KeyValuePairs.iterHash(kv, valueVisitor.applyHashZipList(in, 0));
                case RDB_TYPE_LIST_QUICKLIST:
                    return KeyValuePairs.iterList(kv, valueVisitor.applyListQuickList(in, 0));
                case RDB_TYPE_MODULE:
                    return KeyValuePairs.module(kv, valueVisitor.applyModule(in, 0));
                case RDB_TYPE_MODULE_2:
                    return KeyValuePairs.module(kv, valueVisitor.applyModule2(in, 0));
                case RDB_TYPE_STREAM_LISTPACKS:
                    return KeyValuePairs.stream(kv, valueVisitor.applyStreamListPacks(in, 0));
                default:
                    throw new AssertionError("unexpected value type:" + valueType);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
