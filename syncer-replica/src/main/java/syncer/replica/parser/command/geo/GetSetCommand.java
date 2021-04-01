package syncer.replica.parser.command.geo;

import syncer.replica.datatype.command.GenericKeyValueCommand;

/**
 *
 */
public class GetSetCommand  extends GenericKeyValueCommand {

    public GetSetCommand() {
    }

    public GetSetCommand(byte[] key, byte[] value) {
        super(key, value);
    }
}
