package syncer.replica.datatype.command.common;

import syncer.replica.datatype.command.AbstractBaseCommand;

public class PingCommand extends AbstractBaseCommand {
    private static final long serialVersionUID = 1L;

    private byte[] message;

    public PingCommand() {
    }

    public PingCommand(byte[] message) {
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }



    @Override
    public String toString() {
        return "PingCommand{" +
                "message='" + message + '\'' +
                '}';
    }
}
