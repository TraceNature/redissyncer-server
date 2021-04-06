package syncer.replica.datatype.command.string;

import syncer.replica.datatype.command.GenericKeyCommand;

/**
 * DECR key
 * Available since 1.0.0.
 *
 * Time complexity: O(1)
 *
 * Decrements the number stored at key by one. If the key does not exist, it is set to 0 before performing the operation. An error is returned if the key contains a value of the wrong type or contains a string that can not be represented as integer. This operation is limited to 64 bit signed integers.
 *
 * See INCR for extra information on increment/decrement operations.
 *
 * Return value
 * Integer reply: the value of key after the decrement
 *
 * Examples
 * redis> SET mykey "10"
 * "OK"
 * redis> DECR mykey
 * (integer) 9
 * redis> SET mykey "234293482390480948029348230948"
 * "OK"
 * redis> DECR mykey
 * ERR ERR value is not an integer or out of range
 * redis>
 */
public class DecrCommand extends GenericKeyCommand {

    private static final long serialVersionUID = 1L;

    public DecrCommand() {
    }

    public DecrCommand(byte[] key) {
        super(key);
    }

}

