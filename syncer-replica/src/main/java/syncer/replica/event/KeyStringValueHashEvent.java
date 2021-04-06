package syncer.replica.event;

import syncer.replica.kv.KeyValuePairEvent;

import java.util.Map;

/**
 * Hash事件
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class KeyStringValueHashEvent extends KeyValuePairEvent<byte[], Map<byte[], byte[]>> {
    private static final long serialVersionUID = 1L;
}
