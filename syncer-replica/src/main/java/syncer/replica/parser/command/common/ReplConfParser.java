package syncer.replica.parser.command.common;


import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.ReplConfCommand;
import syncer.replica.datatype.command.common.ReplConfGetAckCommand;
import syncer.replica.util.strings.Strings;

/**
 * @author Leon Chen
 * @since 2.6.0
 */
public class ReplConfParser implements CommandParser<ReplConfCommand> {
    @Override
    public ReplConfCommand parse(Object[] command) {
        int idx = 1;
        String type = CommandParsers.toRune(command[idx]);
        idx++;
        if (Strings.isEquals(type, "GETACK")) {
            return new ReplConfGetAckCommand();
        } else {
            throw new AssertionError("parse [REPLCONF] command error." + type);
        }
    }
}
