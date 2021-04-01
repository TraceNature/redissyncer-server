package syncer.replica.event;

import syncer.replica.kv.KeyValuePairEvent;

import java.util.Set;

/**
 * Set 事件
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class KeyStringValueSetEvent extends KeyValuePairEvent<byte[], Set<byte[]>> {
    private static final long serialVersionUID = 1L;
}
