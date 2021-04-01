package syncer.replica.parser.command.hash;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.hash.HIncrByCommand;


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
public class HIncrByCommandParser implements CommandParser<HIncrByCommand> {

    @Override
    public HIncrByCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] field = CommandParsers.toBytes(command[idx]);
        idx++;
        long increment = CommandParsers.toLong(command[idx++]);
        return new HIncrByCommand(key, field, increment);
    }

}
