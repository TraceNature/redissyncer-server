package syncer.replica.event;

import syncer.replica.datatype.rdb.stream.Stream;
import syncer.replica.kv.KeyValuePairEvent;

/**
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class KeyStringValueStreamEvent extends KeyValuePairEvent<byte[], Stream> {
    private static final long serialVersionUID = 1L;
}
