package syncer.replica.parser.command.string;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.string.IncrByFloatCommand;

/**
 *INCRBYFLOAT key increment
 * Available since 2.6.0.
 *
 * Time complexity: O(1)
 *
 * Increment the string representing a floating point number stored at key by the specified increment. By using a negative increment value, the result is that the value stored at the key is decremented (by the obvious properties of addition). If the key does not exist, it is set to 0 before performing the operation. An error is returned if one of the following conditions occur:
 *
 * The key contains a value of the wrong type (not a string).
 * The current key content or the specified increment are not parsable as a double precision floating point number.
 * If the command is successful the new incremented value is stored as the new value of the key (replacing the old one), and returned to the caller as a string.
 *
 * Both the value already contained in the string key and the increment argument can be optionally provided in exponential notation, however the value computed after the increment is stored consistently in the same format, that is, an integer number followed (if needed) by a dot, and a variable number of digits representing the decimal part of the number. Trailing zeroes are always removed.
 *
 * The precision of the output is fixed at 17 digits after the decimal point regardless of the actual internal precision of the computation.
 *
 * Return value
 * Bulk string reply: the value of key after the increment.
 *
 * Examples
 * redis> SET mykey 10.50
 * "OK"
 * redis> INCRBYFLOAT mykey 0.1
 * "10.6"
 * redis> INCRBYFLOAT mykey -5
 * "5.6"
 * redis> SET mykey 5.0e3
 * "OK"
 * redis> INCRBYFLOAT mykey 2.0e2
 * "5200"
 * redis>
 */
public class IncrByFloatCommandParser implements CommandParser<IncrByFloatCommand> {
    @Override
    public IncrByFloatCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        float value = CommandParsers.toFloat(command[idx++]);
        return new IncrByFloatCommand(key, value);
    }

}
