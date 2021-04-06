package syncer.replica.parser.command.common;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.FlushDBCommand;
import syncer.replica.util.strings.Strings;

public class FlushDBCommandParser  implements CommandParser<FlushDBCommand> {
    @Override
    public FlushDBCommand parse(Object[] command) {
        boolean isAsync = false;
        boolean isSync = false;
        if (command.length == 2 && Strings.isEquals(CommandParsers.toRune(command[1]), "ASYNC")) {
            isAsync = true;
        } else if (command.length == 2 && Strings.isEquals(CommandParsers.toRune(command[1]), "SYNC")) {
            isSync = true;
        }
        return new FlushDBCommand(isAsync, isSync);
    }

}