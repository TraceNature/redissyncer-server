package syncer.replica.datatype.command.string;

import syncer.replica.datatype.command.GenericKeyCommand;

/**
 * https://redis.io/commands/decrby
 *
 * DECRBY key decrement
 *
 * Available since 1.0.0.
 *
 * Time complexity: O(1)
 *
 * Decrements the number stored at key by decrement. If the key does not exist, it is set to 0 before performing the operation. An error is returned if the key contains a value of the wrong type or contains a string that can not be represented as integer. This operation is limited to 64 bit signed integers.
 *
 * See INCR for extra information on increment/decrement operations.
 *
 * Return value
 * Integer reply: the value of key after the decrement
 *
 * Examples
 * redis> SET mykey "10"
 * "OK"
 * redis> DECRBY mykey 3
 * (integer) 7
 * redis>
 */
public class DecrByCommand extends GenericKeyCommand {
    private static final long serialVersionUID = 1L;

    private long value;

    public DecrByCommand() {
    }

    public DecrByCommand(byte[] key, long value) {
        super(key);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
