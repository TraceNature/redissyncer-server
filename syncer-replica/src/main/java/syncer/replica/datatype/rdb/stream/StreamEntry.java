package syncer.replica.datatype.rdb.stream;

import java.io.Serializable;
import java.util.Map;

/**
 * 类似于java map
 * Redis里的streams中的数据也称为entry
 * @author: Eq Zhan
 * @create: 2021-03-17
 **/
public class StreamEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private StreamID id;
    private boolean deleted;
    private Map<byte[], byte[]> fields;

    public StreamEntry() {

    }

    public StreamEntry(StreamID id, boolean deleted, Map<byte[], byte[]> fields) {
        this.id = id;
        this.deleted = deleted;
        this.fields = fields;
    }

    public StreamID getId() {
        return id;
    }

    public void setId(StreamID id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Map<byte[], byte[]> getFields() {
        return fields;
    }

    public void setFields(Map<byte[], byte[]> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "StreamEntry{" +
                "id=" + id +
                ", deleted=" + deleted +
                ", fields=" + fields +
                '}';
    }
}
