package syncer.replica.parser.command.hash;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;

/**
 * HSETNX key field value
 * Available since 2.0.0.
 *
 * Time complexity: O(1)
 *
 * Sets field in the hash stored at key to value, only if field does not yet exist. If key does not exist, a new key holding a hash is created. If field already exists, this operation has no effect.
 *
 * Return value
 * Integer reply, specifically:
 *
 * 1 if field is a new field in the hash and value was set.
 * 0 if field already exists in the hash and no operation was performed.
 * Examples
 * redis> HSETNX myhash field "Hello"
 * (integer) 1
 * redis> HSETNX myhash field "World"
 * (integer) 0
 * redis> HGET myhash field
 * "Hello"
 * redis>
 */
public class HSetNxCommandParser implements CommandParser<HSetNxCommand> {

    @Override
    public HSetNxCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] field = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] value = CommandParsers.toBytes(command[idx]);
        idx++;
        return new HSetNxCommand(key, field, value);
    }

}