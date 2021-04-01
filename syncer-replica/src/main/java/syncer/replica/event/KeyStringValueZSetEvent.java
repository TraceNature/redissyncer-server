package syncer.replica.event;

import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.kv.KeyValuePairEvent;

import java.util.Set;

/**
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class KeyStringValueZSetEvent extends KeyValuePairEvent<byte[], Set<ZSetEntry>> {
    private static final long serialVersionUID = 1L;
}
