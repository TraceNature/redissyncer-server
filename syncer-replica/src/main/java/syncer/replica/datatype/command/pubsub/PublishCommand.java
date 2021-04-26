package syncer.replica.datatype.command.pubsub;

import syncer.replica.datatype.command.AbstractBaseCommand;


/**
 *  https://redis.io/commands/publish
 *  Available since 2.0.0.
 *
 * Time complexity: O(N+M) where N is the number of clients subscribed to the receiving channel and
 * M is the total number of subscribed patterns (by any client).
 */
public class PublishCommand extends AbstractBaseCommand {
    private static final long serialVersionUID = 1L;

    private byte[] channel;
    private byte[] message;

    public PublishCommand() {
    }

    public PublishCommand(byte[] channel, byte[] message) {
        this.channel = channel;
        this.message = message;
    }

    public byte[] getChannel() {
        return channel;
    }

    public void setChannel(byte[] channel) {
        this.channel = channel;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }
}
