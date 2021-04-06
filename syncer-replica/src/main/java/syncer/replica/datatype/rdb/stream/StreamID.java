package syncer.replica.datatype.rdb.stream;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * https://github.com/redis/redis/blob/5.0.0/src/stream.h
 *
 * Stream item ID: a 128 bit number composed of a milliseconds time and
 * a sequence counter. IDs generated in the same millisecond (or in a past
 * millisecond if the clock jumped backward) will use the millisecond time
 * of the latest generated ID and an incremented sequence.
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/

public class StreamID implements Serializable, Comparable<StreamID> {
    private static final long serialVersionUID = 1L;
    public static Comparator<StreamID> COMPARATOR = comparator();

    /**
     * Unix time in milliseconds.
     */
    private long ms;
    /**
     *  Sequence number.
     */
    private long seq;

    public StreamID() {

    }

    public StreamID(long ms, long seq) {
        this.ms = ms;
        this.seq = seq;
    }

    public long getMs() {
        return ms;
    }

    public void setMs(long ms) {
        this.ms = ms;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public StreamID delta(long ms, long seq) {
        return new StreamID(this.ms + ms, this.seq + seq);
    }

    @Override
    public String toString() {
        return ms + "-" + seq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StreamID id = (StreamID) o;
        return ms == id.ms && seq == id.seq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ms, seq);
    }

    @Override
    public int compareTo(StreamID that) {
        int r = Long.compare(this.ms, that.ms);
        if (r == 0){
            return Long.compare(this.seq, that.seq);
        }
        return r;
    }

    public static StreamID valueOf(String id) {
        int idx = id.indexOf('-');
        long ms = Long.parseLong(id.substring(0, idx));
        long seq = Long.parseLong(id.substring(idx + 1, id.length()));
        return new StreamID(ms, seq);
    }

    public static StreamID valueOf(String strMs, String strSeq) {
        long ms = Long.parseLong(strMs);
        long seq = Long.parseLong(strSeq);
        return new StreamID(ms, seq);
    }

    public static Comparator<StreamID> comparator() {
        return new Comparator<StreamID>() {
            @Override
            public int compare(StreamID o1, StreamID o2) {
                return o1.compareTo(o2);
            }
        };
    }
}
