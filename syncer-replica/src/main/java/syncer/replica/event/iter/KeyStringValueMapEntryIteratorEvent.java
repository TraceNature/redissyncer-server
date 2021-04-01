package syncer.replica.event.iter;

import syncer.replica.kv.KeyValuePairEvent;

import java.util.Iterator;
import java.util.Map;

public class KeyStringValueMapEntryIteratorEvent  extends KeyValuePairEvent<byte[], Iterator<Map.Entry<byte[], byte[]>>> {
    private static final long serialVersionUID = 1L;
}
