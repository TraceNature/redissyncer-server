package syncer.replica.datatype.command.common;

import syncer.replica.datatype.command.AbstractBaseCommand;

/**
 * https://redis.io/commands/copy
 *
 * COPY source destination [DB destination-db] [REPLACE]
 * Available since 6.2.0.
 *
 * Time complexity: O(N) worst case for collections, where N is the number of nested items. O(1) for string values.
 *
 * This command copies the value stored at the source key to the destination key.
 *
 * By default, the destination key is created in the logical database used by the connection.
 * The DB option allows specifying an alternative logical database index for the destination key.
 *
 * The command returns an error when the destination key already exists. The REPLACE option removes the destination key before copying the value to it.
 *
 * Return value
 * Integer reply, specifically:
 *
 * 1 if source was copied.
 * 0 if source was not copied.
 * Examples
 * SET dolly "sheep"
 * COPY dolly clone
 * GET clone
 */
public class CopyCommand extends AbstractBaseCommand {
    private static final long serialVersionUID = 1L;

    private byte[] source;
    private byte[] destination;
    private Integer db;
    private boolean replace;

    public CopyCommand() {
    }

    public CopyCommand(byte[] source, byte[] destination, Integer db, boolean replace) {
        this.source = source;
        this.destination = destination;
        this.db = db;
        this.replace = replace;
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

    public Integer getDb() {
        return db;
    }

    public void setDb(Integer db) {
        this.db = db;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

}
