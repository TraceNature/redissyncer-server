package syncer.replica.parser.command.common;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.ExpireAtCommand;

/**
 * EXPIREAT key timestamp
 * Available since 1.2.0.
 *
 * Time complexity: O(1)
 *
 * EXPIREAT has the same effect and semantic as EXPIRE, but instead of specifying the number of seconds representing the TTL (time to live), it takes an absolute Unix timestamp (seconds since January 1, 1970). A timestamp in the past will delete the key immediately.
 *
 * Please for the specific semantics of the command refer to the documentation of EXPIRE.
 *
 * Background
 * EXPIREAT was introduced in order to convert relative timeouts to absolute timeouts for the AOF persistence mode. Of course, it can be used directly to specify that a given key should expire at a given time in the future.
 *
 * Return value
 * Integer reply, specifically:
 *
 * 1 if the timeout was set.
 * 0 if key does not exist.
 * Examples
 * redis> SET mykey "Hello"
 * "OK"
 * redis> EXISTS mykey
 * (integer) 1
 * redis> EXPIREAT mykey 1293840000
 * (integer) 1
 * redis> EXISTS mykey
 * (integer) 0
 */
public class ExpireAtCommandParser implements CommandParser<ExpireAtCommand> {
    @Override
    public ExpireAtCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        long ex = CommandParsers.toLong(command[idx++]);
        return new ExpireAtCommand(key, ex);
    }

}
