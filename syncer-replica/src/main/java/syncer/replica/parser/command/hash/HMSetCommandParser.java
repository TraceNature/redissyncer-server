package syncer.replica.parser.command.hash;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.hash.HMSetCommand;
import syncer.replica.util.map.ByteArrayMap;

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
public class HMSetCommandParser implements CommandParser<HMSetCommand> {

    @Override
    public HMSetCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        ByteArrayMap fields = new ByteArrayMap();
        while (idx < command.length) {
            byte[] field = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] value = idx == command.length ? null : CommandParsers.toBytes(command[idx]);
            idx++;
            fields.put(field, value);
        }
        return new HMSetCommand(key, fields);
    }

}
