package syncer.replica.parser.command.hash;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.hash.HSetCommand;
import syncer.replica.util.map.ByteArrayMap;

/**
 *HSET key field value [field value ...]
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
public class HSetCommandParser implements CommandParser<HSetCommand> {

    @Override
    @SuppressWarnings("deprecation")
    public HSetCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        ByteArrayMap fields = new ByteArrayMap();
        byte[] firstField = null;
        byte[] firstValue = null;
        while (idx < command.length) {
            byte[] field = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] value = idx == command.length ? null : CommandParsers.toBytes(command[idx]);
            idx++;
            if (firstField == null) {
                firstField = field;
            }
            if (firstValue == null) {
                firstValue = value;
            }
            fields.put(field, value);
        }
        HSetCommand hSetCommand =  new HSetCommand(key, fields);
        hSetCommand.setField(firstField);
        hSetCommand.setValue(firstValue);
        return hSetCommand;
    }
}