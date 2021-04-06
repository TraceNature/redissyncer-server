package syncer.replica.datatype.command.list;

import syncer.replica.datatype.command.AbstractBaseCommand;

/**
 * BRPOPLPUSH source destination timeout
 * Available since 2.2.0.
 *
 * Time complexity: O(1)
 *
 * BRPOPLPUSH is the blocking variant of RPOPLPUSH. When source contains elements, this command behaves exactly like RPOPLPUSH. When used inside a MULTI/EXEC block, this command behaves exactly like RPOPLPUSH. When source is empty, Redis will block the connection until another client pushes to it or until timeout is reached. A timeout of zero can be used to block indefinitely.
 *
 * As per Redis 6.2.0, BRPOPLPUSH is considered deprecated. Please prefer BLMOVE in new code.
 *
 * See RPOPLPUSH for more information.
 *
 * Return value
 * Bulk string reply: the element being popped from source and pushed to destination. If timeout is reached, a Null reply is returned.
 */
public class BRPopLPushCommand extends AbstractBaseCommand {

    private static final long serialVersionUID = 1L;

    private byte[] source;
    private byte[] destination;
    private int timeout;

    public BRPopLPushCommand() {
    }

    public BRPopLPushCommand(byte[] source, byte[] destination, int timeout) {
        this.source = source;
        this.destination = destination;
        this.timeout = timeout;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }

    public byte[] getDestination() {
        return destination;
    }

    public void setDestination(byte[] destination) {
        this.destination = destination;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
