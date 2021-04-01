package syncer.replica.datatype.command.hash;

import syncer.replica.datatype.command.GenericKeyCommand;

/**
 * HINCRBY key field increment
 * Available since 2.0.0.
 *
 * Time complexity: O(1)
 *
 * Increments the number stored at field in the hash stored at key by increment. If key does not exist, a new key holding a hash is created. If field does not exist the value is set to 0 before the operation is performed.
 *
 * The range of values supported by HINCRBY is limited to 64 bit signed integers.
 *
 * Return value
 * Integer reply: the value at field after the increment operation.
 *
 * Examples
 * Since the increment argument is signed, both increment and decrement operations can be performed:
 *
 * redis> HSET myhash field 5
 * (integer) 1
 * redis> HINCRBY myhash field 1
 * (integer) 6
 * redis> HINCRBY myhash field -1
 * (integer) 5
 * redis> HINCRBY myhash field -10
 * (integer) -5
 * redis>
 */
public class HIncrByCommand extends GenericKeyCommand {

    private static final long serialVersionUID = 1L;

    private byte[] field;
    private long increment;

    public HIncrByCommand() {
    }

    public HIncrByCommand(byte[] key, byte[] field, long increment) {
        super(key);
        this.field = field;
        this.increment = increment;
    }

    public byte[] getField() {
        return field;
    }

    public void setField(byte[] field) {
        this.field = field;
    }

    public long getIncrement() {
        return increment;
    }

    public void setIncrement(long increment) {
        this.increment = increment;
    }
}
