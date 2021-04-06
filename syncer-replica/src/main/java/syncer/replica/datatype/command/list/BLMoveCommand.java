package syncer.replica.datatype.command.list;

import syncer.replica.datatype.command.AbstractBaseCommand;

/**
 * https://redis.io/commands/blmove
 *
 * BLMOVE source destination LEFT|RIGHT LEFT|RIGHT timeout
 *
 * Available since 6.2.0.
 *
 * Time complexity: O(1)
 *
 * BLMOVE is the blocking variant of LMOVE. When source contains elements, this command behaves exactly like LMOVE. When used inside a MULTI/EXEC block, this command behaves exactly like LMOVE. When source is empty, Redis will block the connection until another client pushes to it or until timeout is reached. A timeout of zero can be used to block indefinitely.
 *
 * This command comes in place of the now deprecated BRPOPLPUSH. Doing BLMOVE RIGHT LEFT is equivalent.
 *
 * See LMOVE for more information.
 *
 * Return value
 * Bulk string reply: the element being popped from source and pushed to destination. If timeout is reached, a Null reply is returned.
 *
 */
public class BLMoveCommand extends AbstractBaseCommand {
    private static final long serialVersionUID = 1L;

    private byte[] source;
    private byte[] destination;
    private DirectionType from;
    private DirectionType to;
    private long timeout;

    public BLMoveCommand() {
    }

    public BLMoveCommand(byte[] source, byte[] destination, DirectionType from, DirectionType to) {
        this.source = source;
        this.destination = destination;
        this.from = from;
        this.to = to;
    }

    public BLMoveCommand(byte[] source, byte[] destination, DirectionType from, DirectionType to, long timeout) {
        this.source = source;
        this.destination = destination;
        this.from = from;
        this.to = to;
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

    public DirectionType getFrom() {
        return from;
    }

    public void setFrom(DirectionType from) {
        this.from = from;
    }

    public DirectionType getTo() {
        return to;
    }

    public void setTo(DirectionType to) {
        this.to = to;
    }
}
