package syncer.replica.datatype.command.string;

import syncer.replica.datatype.command.AbstractBaseCommand;

public class BitOpCommand extends AbstractBaseCommand {

    private static final long serialVersionUID = 1L;

    private Op op;
    private byte[] destkey;
    private byte[][] keys;

    public BitOpCommand() {
    }

    public BitOpCommand(Op op, byte[] destkey, byte[][] keys) {
        this.op = op;
        this.destkey = destkey;
        this.keys = keys;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public byte[] getDestkey() {
        return destkey;
    }

    public void setDestkey(byte[] destkey) {
        this.destkey = destkey;
    }

    public byte[][] getKeys() {
        return keys;
    }

    public void setKeys(byte[][] keys) {
        this.keys = keys;
    }
}
