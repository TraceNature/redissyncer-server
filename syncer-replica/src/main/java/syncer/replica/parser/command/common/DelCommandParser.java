package syncer.replica.parser.command.common;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.DelCommand;

/**
 * DEL key [key ...]
 * Available since 1.0.0.
 *
 * Time complexity: O(N) where N is the number of keys that will be removed. When a key to remove holds a value other than a string, the individual complexity for this key is O(M) where M is the number of elements in the list, set, sorted set or hash. Removing a single key that holds a string value is O(1).
 *
 * Removes the specified keys. A key is ignored if it does not exist.
 *
 * Return value
 * Integer reply: The number of keys that were removed.
 *
 * Examples
 * redis> SET key1 "Hello"
 * "OK"
 * redis> SET key2 "World"
 * "OK"
 * redis> DEL key1 key2 key3
 * (integer) 2
 * redis>
 *
 */
public class DelCommandParser implements CommandParser<DelCommand> {
    @Override
    public DelCommand parse(Object[] command) {
        int idx = 1;
        byte[][] keys = new byte[command.length - 1][];
        for (int i = idx, j = 0; i < command.length; i++, j++) {
            keys[j] = CommandParsers.toBytes(command[i]);
        }
        return new DelCommand(keys);
    }

}
