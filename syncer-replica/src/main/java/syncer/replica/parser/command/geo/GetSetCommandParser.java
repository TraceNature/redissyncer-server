package syncer.replica.parser.command.geo;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;

public class GetSetCommandParser implements CommandParser<GetSetCommand> {
    @Override
    public GetSetCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] value = CommandParsers.toBytes(command[idx]);
        idx++;
        return new GetSetCommand(key, value);
    }

}
