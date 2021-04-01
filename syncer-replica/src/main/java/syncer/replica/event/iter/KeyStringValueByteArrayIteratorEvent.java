package syncer.replica.event.iter;

import syncer.replica.kv.KeyValuePairEvent;

import java.util.Iterator;

public class KeyStringValueByteArrayIteratorEvent  extends KeyValuePairEvent<byte[], Iterator<byte[]>> {
    private static final long serialVersionUID = 1L;
}