package syncer.replica.event.iter.datatype;

import syncer.replica.kv.KeyValuePairEvent;

public class BatchedKeyValuePairEvent<K, V> extends KeyValuePairEvent<K, V> {
    private static final long serialVersionUID = 1L;
    private int batch;
    private boolean last;

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
