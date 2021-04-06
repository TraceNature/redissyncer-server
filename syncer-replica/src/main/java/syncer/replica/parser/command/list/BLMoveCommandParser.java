package syncer.replica.parser.command.list;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.list.BLMoveCommand;
import syncer.replica.datatype.command.list.DirectionType;
import syncer.replica.util.strings.Strings;

import java.util.Objects;

/**
 * https://redis.io/commands/blmove
 *
 * BLMOVE source destination LEFT|RIGHT LEFT|RIGHT timeout
 *
 * Available since 6.2.0.
 *
 * Time complexity: O(1)
 *
 * BLMOVE is the blocking variant of LMOVE. When source contains elements, this command behaves exactly like LMOVE. When used inside a MULTI/EXEC block, this command behaves exactly like LMOVE.
 *
 * When source is empty, Redis will block the connection until another client pushes to it or until timeout is reached. A timeout of zero can be used to block indefinitely.
 *
 * This command comes in place of the now deprecated BRPOPLPUSH. Doing BLMOVE RIGHT LEFT is equivalent.
 *
 * See LMOVE for more information.
 *
 * Return value
 * Bulk string reply: the element being popped from source and pushed to destination. If timeout is reached, a Null reply is returned.
 *
 */
public class BLMoveCommandParser implements CommandParser<BLMoveCommand> {

    @Override
    public BLMoveCommand parse(Object[] command) {
        int idx = 1;
        byte[] source = CommandParsers.toBytes(command[idx++]);
        byte[] destination =  CommandParsers.toBytes(command[idx++]);
        DirectionType from = parseDirection(CommandParsers.toRune(command[idx++]));
        DirectionType to = parseDirection(CommandParsers.toRune(command[idx++]));
        int t=idx++;
        if(t<command.length&&Objects.nonNull(command[t])){
            long timeout=CommandParsers.toLong(command[t]);
            return new BLMoveCommand(source, destination, from, to,timeout);
        }
        return new BLMoveCommand(source, destination, from, to);
    }

    private DirectionType parseDirection(String direction) {
        if (Strings.isEquals(direction, "LEFT")) {
            return DirectionType.LEFT;
        } else if (Strings.isEquals(direction, "RIGHT")) {
            return DirectionType.RIGHT;
        } else {
            throw new AssertionError("parse [BLMOVE] command error." + direction);
        }
    }
}

