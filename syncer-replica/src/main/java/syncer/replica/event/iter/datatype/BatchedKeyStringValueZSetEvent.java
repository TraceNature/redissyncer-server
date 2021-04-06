package syncer.replica.event.iter.datatype;

import syncer.replica.datatype.rdb.zset.ZSetEntry;

import java.util.Set;

public class BatchedKeyStringValueZSetEvent extends BatchedKeyValuePairEvent<byte[], Set<ZSetEntry>> {
    private static final long serialVersionUID = 1L;
}
