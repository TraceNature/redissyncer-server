package syncer.replica.event;

import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.kv.KeyValuePairEvent;

/**
 * @author: Eq Zhan
 * @create: 2021-03-17
 **/
public class KeyStringValueModuleEvent extends KeyValuePairEvent<byte[], Module> {
    private static final long serialVersionUID = 1L;
}
