package syncer.replica.event;

import syncer.replica.kv.KeyValuePairEvent;

import java.util.List;

/**
 * List 事件
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class KeyStringValueListEvent extends KeyValuePairEvent<byte[], List<byte[]>> {
    private static final long serialVersionUID = 1L;
}
