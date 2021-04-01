package syncer.replica.datatype.command.hash;

import syncer.replica.datatype.command.GenericKeyCommand;
import syncer.replica.util.map.ByteArrayMap;

import java.util.Map;

/**
 * HSET key field value [field value ...]
 * Available since 2.0.0.
 *
 * Time complexity: O(1) for each field/value pair added, so O(N) to add N field/value pairs when the command is called with multiple field/value pairs.
 *
 * Sets field in the hash stored at key to value. If key does not exist, a new key holding a hash is created. If field already exists in the hash, it is overwritten.
 *
 * As of Redis 4.0.0, HSET is variadic and allows for multiple field/value pairs.
 *
 * Return value
 * Integer reply: The number of fields that were added.
 *
 * Examples
 * redis> HSET myhash field1 "Hello"
 * (integer) 1
 * redis> HGET myhash field1
 * "Hello"
 * redis>
 */
public class HSetCommand extends GenericKeyCommand {


    private static final long serialVersionUID = 1L;

    private Map<byte[], byte[]> fields = new ByteArrayMap();
    private byte[] field;
    private byte[] value;

    public HSetCommand() {
    }

    public HSetCommand(byte[] key, byte[] field, byte[] value) {
        super(key);
        this.field = field;
        this.value = value;
        this.fields.put(field, value);
    }

    public HSetCommand(byte[] key, Map<byte[], byte[]> fields) {
        super(key);
        this.fields = fields;
    }

    /**
     * @deprecated Use {@link #getFields()} instead. will remove this method in 4.0.0
     */
    @Deprecated
    public byte[] getField() {
        return field;
    }

    /**
     * @deprecated Use {@link #setFields(Map)} instead. will remove this method in 4.0.0
     */
    @Deprecated
    public void setField(byte[] field) {
        this.field = field;
    }

    /**
     * @deprecated Use {@link #getFields()} instead. will remove this method in 4.0.0
     */
    @Deprecated
    public byte[] getValue() {
        return value;
    }

    /**
     * @deprecated Use {@link #setFields(Map)} instead. will remove this method in 4.0.0
     */
    @Deprecated
    public void setValue(byte[] value) {
        this.value = value;
    }

    public Map<byte[], byte[]> getFields() {
        return fields;
    }

    public void setFields(Map<byte[], byte[]> fields) {
        this.fields = fields;
    }
}
