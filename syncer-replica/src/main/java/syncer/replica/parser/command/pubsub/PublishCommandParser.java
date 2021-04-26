package syncer.replica.parser.command.pubsub;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.pubsub.PublishCommand;


/**
 * https://redis.io/commands/publish
 */
public class PublishCommandParser implements CommandParser<PublishCommand> {
    @Override
    public PublishCommand parse(Object[] command) {
        int idx = 1;
        byte[] channel = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] message = CommandParsers.toBytes(command[idx]);
        idx++;
        return new PublishCommand(channel, message);
    }

    public PublishCommand parse(byte[]...commands) {
        int idx = 0;
        byte[] channel = commands[idx];
        idx++;
        byte[] message =commands[idx];
        idx++;
        return new PublishCommand(channel, message);
    }

}
