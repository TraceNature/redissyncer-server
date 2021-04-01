package syncer.replica.parser.command.string;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.string.DecrByCommand;

/**
 * https://redis.io/commands/decrby
 *
 *DECRBY key decrement
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
public class DecrByCommandParser implements CommandParser<DecrByCommand> {

    @Override
    public DecrByCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        long value = CommandParsers.toLong(command[idx++]);
        return new DecrByCommand(key, value);
    }
}
