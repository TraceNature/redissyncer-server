package syncer.replica.datatype.rdb.stream;

import syncer.replica.util.strings.Strings;

import java.io.Serializable;
import java.util.NavigableMap;

/**
 * https://github.com/redis/redis/blob/5.0.0/src/stream.h
 *
 * A specific consumer in a consumer group.
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class StreamConsumer implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     *  Consumer name. This is how the consumer will be identified in the consumer group
     *   protocol. Case sensitive.
     */
    private byte[] name;
    /**
     * Last time this consumer was active.
     */
    private long seenTime;

    /**
     * Consumer specific pending entries list: all the pending messages delivered to this consumer
     * not yet acknowledged. Keys are big endian message IDs, while values are the same streamNACK
     * structure referenced in the "pel" of the conumser group structure itself, so the value is shared.
     */
    private NavigableMap<StreamID, StreamNack> pendingEntries;

    public StreamConsumer() {

    }

    public StreamConsumer(byte[] name, long seenTime, NavigableMap<StreamID, StreamNack> pendingEntries) {
        this.name = name;
        this.seenTime = seenTime;
        this.pendingEntries = pendingEntries;
    }

    public byte[] getName() {
        return name;
    }

    public void setName(byte[] name) {
        this.name = name;
    }

    public long getSeenTime() {
        return seenTime;
    }

    public void setSeenTime(long seenTime) {
        this.seenTime = seenTime;
    }

    public NavigableMap<StreamID, StreamNack> getPendingEntries() {
        return pendingEntries;
    }

    public void setPendingEntries(NavigableMap<StreamID, StreamNack> pendingEntries) {
        this.pendingEntries = pendingEntries;
    }

    @Override
    public String toString() {
        String r = "StreamConsumer{" + "name='" + Strings.toString(name) + '\'' + ", seenTime=" + seenTime;
        if (pendingEntries != null && !pendingEntries.isEmpty()){
            r += ", cpel=" + pendingEntries.size();
        }
        return r + '}';
    }
}
