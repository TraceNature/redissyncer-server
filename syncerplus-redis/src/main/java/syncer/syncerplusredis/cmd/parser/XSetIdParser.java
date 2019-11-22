package syncer.syncerplusredis.cmd.parser;

import syncer.syncerplusredis.cmd.CommandParser;
import syncer.syncerplusredis.cmd.impl.XSetIdCommand;
import syncer.syncerplusredis.cmd.CommandParsers;

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
