package syncer.replica.parser.command.common;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.PingCommand;

public class PingCommandParser implements CommandParser<PingCommand> {
    @Override
    public PingCommand parse(Object[] command) {
        byte[] message = command.length == 1 ? null : CommandParsers.toBytes(command[1]);
        return new PingCommand(message);
    }
}
