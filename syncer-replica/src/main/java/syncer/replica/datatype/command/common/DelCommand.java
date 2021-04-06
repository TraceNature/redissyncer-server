package syncer.replica.datatype.command.common;

import syncer.replica.datatype.command.AbstractBaseCommand;

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
public class DelCommand extends AbstractBaseCommand {

    private static final long serialVersionUID = 1L;

    private byte[][] keys;

    public DelCommand() {
    }

    public DelCommand(byte[][] keys) {
        this.keys = keys;
    }

    public byte[][] getKeys() {
        return keys;
    }

    public void setKeys(byte[][] keys) {
        this.keys = keys;
    }
}
