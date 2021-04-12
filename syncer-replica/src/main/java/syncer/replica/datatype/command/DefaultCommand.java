package syncer.replica.datatype.command;

/**
 * 默认命令解析器
 */
public class DefaultCommand extends AbstractBaseCommand{

    private static final long serialVersionUID = 1L;

    private byte[] command;
    private byte[][] args = new byte[0][];

    private String replid;
    private long offset;
    public DefaultCommand() {
    }

    public DefaultCommand(byte[] command, byte[][] args) {
        this.command = command;
        this.args = args;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public byte[][] getArgs() {
        return args;
    }

    public void setArgs(byte[][] args) {
        this.args = args;
    }
}
