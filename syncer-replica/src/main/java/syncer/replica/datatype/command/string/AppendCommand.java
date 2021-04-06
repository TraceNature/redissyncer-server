package syncer.replica.datatype.command.string;

import syncer.replica.datatype.command.GenericKeyValueCommand;


/**
 * Append  String
 */
public class AppendCommand extends GenericKeyValueCommand {
    private static final long serialVersionUID = 1L;

    public AppendCommand() {
    }

    public AppendCommand(byte[] key, byte[] value) {
        super(key, value);
    }
}
