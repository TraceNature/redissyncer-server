package syncer.replica.parser.command.jimdb;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.util.strings.Strings;

/**
 * [transmit bj 3 set y y]
 * JimDb 增量命令解析
 */
public class JimDbDefaultCommandParser implements CommandParser<DefaultCommand> {
    @Override
    public DefaultCommand parse(Object[] command) {
        byte[][] args = new byte[command.length - 4][];
        for (int i = 4, j = 0; i < command.length; i++) {
            if (command[i] instanceof Long) {
                args[j++] = String.valueOf(command[i]).getBytes();
            } else if (command[i] instanceof byte[]) {
                args[j++] = (byte[]) command[i];
            } else if (command[i] instanceof Object[]) {
                throw new UnsupportedOperationException(Strings.format(command));
            }
        }
        return new DefaultCommand((byte[]) command[3], args);
    }
}
