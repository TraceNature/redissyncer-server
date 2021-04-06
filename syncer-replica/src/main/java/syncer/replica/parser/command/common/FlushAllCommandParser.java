package syncer.replica.parser.command.common;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.FlushAllCommand;
import syncer.replica.util.strings.Strings;

public class FlushAllCommandParser implements CommandParser<FlushAllCommand> {
    @Override
    public FlushAllCommand parse(Object[] command) {
        boolean isAsync = false;
        boolean isSync = false;
        if (command.length == 2 && Strings.isEquals(CommandParsers.toRune(command[1]), "ASYNC")) {
            isAsync = true;
        } else if (command.length == 2 && Strings.isEquals(CommandParsers.toRune(command[1]), "SYNC")) {
            isSync = true;
        }
        return new FlushAllCommand(isAsync, isSync);
    }

}