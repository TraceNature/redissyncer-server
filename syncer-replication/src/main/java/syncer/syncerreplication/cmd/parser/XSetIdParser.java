package syncer.syncerreplication.cmd.parser;


import syncer.syncerreplication.cmd.CommandParser;
import syncer.syncerreplication.cmd.CommandParsers;
import syncer.syncerreplication.cmd.impl.XSetIdCommand;

/**
 * @author Leon Chen
 * @since 2.6.1
 */
public class XSetIdParser implements CommandParser<XSetIdCommand> {
    
    @Override
    public XSetIdCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] id = CommandParsers.toBytes(command[idx]);
        idx++;
        return new XSetIdCommand(key, id);
    }
}
