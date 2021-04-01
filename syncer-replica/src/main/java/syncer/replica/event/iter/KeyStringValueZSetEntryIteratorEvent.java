package syncer.replica.event.iter;

import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.kv.KeyValuePairEvent;

import java.util.Iterator;

public class KeyStringValueZSetEntryIteratorEvent  extends KeyValuePairEvent<byte[], Iterator<ZSetEntry>> {
    private static final long serialVersionUID = 1L;
}
