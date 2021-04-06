package syncer.replica.datatype.command.hash;

import syncer.replica.datatype.command.GenericKeyCommand;

import java.util.Map;

/**
 *HMSET key field value [field value ...]
 * Available since 2.0.0.
 *
 * Time complexity: O(N) where N is the number of fields being set.
 *
 * Sets the specified fields to their respective values in the hash stored at key. This command overwrites any specified fields already existing in the hash. If key does not exist, a new key holding a hash is created.
 *
 * As per Redis 4.0.0, HMSET is considered deprecated. Please prefer HSET in new code.
 *
 * Return value
 * Simple string reply
 *
 * Examples
 * redis> HMSET myhash field1 "Hello" field2 "World"
 * "OK"
 * redis> HGET myhash field1
 * "Hello"
 * redis> HGET myhash field2
 * "World"
 * redis>
 */
public class HMSetCommand extends GenericKeyCommand {

    private static final long serialVersionUID = 1L;

    private Map<byte[], byte[]> fields;

    public HMSetCommand() {
    }

    public HMSetCommand(byte[] key, Map<byte[], byte[]> fields) {
        super(key);
        this.fields = fields;
    }

    public Map<byte[], byte[]> getFields() {
        return fields;
    }

    public void setFields(Map<byte[], byte[]> fields) {
        this.fields = fields;
    }
}
