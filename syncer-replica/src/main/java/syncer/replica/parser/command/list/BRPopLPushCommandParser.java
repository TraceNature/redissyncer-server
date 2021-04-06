package syncer.replica.parser.command.list;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.list.BRPopLPushCommand;

/**
 * BRPOPLPUSH source destination timeout
 * Available since 2.2.0.
 *
 * Time complexity: O(1)
 *
 * BRPOPLPUSH is the blocking variant of RPOPLPUSH. When source contains elements, this command behaves exactly like RPOPLPUSH. When used inside a MULTI/EXEC block, this command behaves exactly like RPOPLPUSH. When source is empty, Redis will block the connection until another client pushes to it or until timeout is reached. A timeout of zero can be used to block indefinitely.
 *
 * As per Redis 6.2.0, BRPOPLPUSH is considered deprecated. Please prefer BLMOVE in new code.
 *
 * See RPOPLPUSH for more information.
 *
 * Return value
 * Bulk string reply: the element being popped from source and pushed to destination. If timeout is reached, a Null reply is returned.
 */
public class BRPopLPushCommandParser implements CommandParser<BRPopLPushCommand> {
    @Override
    public BRPopLPushCommand parse(Object[] command) {
        int idx = 1;
        byte[] source = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] destination = CommandParsers.toBytes(command[idx]);
        idx++;
        int timeout = CommandParsers.toInt(command[idx++]);
        return new BRPopLPushCommand(source, destination, timeout);
    }

}
