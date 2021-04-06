package syncer.replica.parser.command.hash;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.hash.HDelCommand;

/**
 * https://redis.io/commands/hdel
 * HDEL key field [field ...]
 * Available since 2.0.0.
 *
 * Time complexity: O(N) where N is the number of fields to be removed.
 *
 * Removes the specified fields from the hash stored at key. Specified fields that do not exist within this hash are ignored. If key does not exist, it is treated as an empty hash and this command returns 0.
 *
 * Return value
 * Integer reply: the number of fields that were removed from the hash, not including specified but non existing fields.
 *
 * History
 * >= 2.4: Accepts multiple field arguments. Redis versions older than 2.4 can only remove a field per call.
 *
 * To remove multiple fields from a hash in an atomic fashion in earlier versions, use a MULTI / EXEC block.
 *
 * Examples
 * redis> HSET myhash field1 "foo"
 * (integer) 1
 * redis> HDEL myhash field1
 * (integer) 1
 * redis> HDEL myhash field2
 * (integer) 0
 * redis>
 */
public class HDelCommandParser implements CommandParser<HDelCommand> {
    @Override
    public HDelCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[][] fields = new byte[command.length - 2][];
        for (int i = idx, j = 0; i < command.length; i++, j++) {
            fields[j] = CommandParsers.toBytes(command[i]);
        }
        return new HDelCommand(key, fields);
    }

}