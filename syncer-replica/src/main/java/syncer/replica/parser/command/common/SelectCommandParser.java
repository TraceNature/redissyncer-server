package syncer.replica.parser.command.common;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.SelectCommand;


public class SelectCommandParser implements CommandParser<SelectCommand> {
    @Override
    public SelectCommand parse(Object[] command) {
        int index = CommandParsers.toInt(command[1]);
        return new SelectCommand(index);
    }
}
