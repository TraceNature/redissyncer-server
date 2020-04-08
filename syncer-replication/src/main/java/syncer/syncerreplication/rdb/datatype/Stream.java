package syncer.syncerreplication.rdb.datatype;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import syncer.syncerreplication.util.objectUtils.Strings;

import java.io.Serializable;
import java.util.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/8
 */
@NoArgsConstructor
@ToString
public class Stream implements Serializable {
    private static final long serialVersionUID = 1L;
    @Getter @Setter
    private ID lastId;
    @Getter @Setter
    private NavigableMap<ID, Entry> entries;
    @Getter @Setter
    private long length;
    @Getter @Setter
    private List<Group> groups;



    public Stream(ID lastId, NavigableMap<ID, Entry> entries, long length, List<Group> groups) {
        this.lastId = lastId;
        this.entries = entries;
        this.length = length;
        this.groups = groups;
    }



    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class Entry implements Serializable {
        private static final long serialVersionUID = 1L;
        private ID id;
        private boolean deleted;
        private Map<byte[], byte[]> fields;

        public Entry(ID id, boolean deleted, Map<byte[], byte[]> fields) {
            this.id = id;
            this.deleted = deleted;
            this.fields = fields;
        }

    }

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class Group implements Serializable {
        private static final long serialVersionUID = 1L;
        private byte[] name;
        private ID lastId;
        private NavigableMap<ID, Nack> pendingEntries;
        private List<Consumer> consumers;

        public Group(byte[] name, ID lastId, NavigableMap<ID, Nack> pendingEntries, List<Consumer> consumers) {
            this.name = name;
            this.lastId = lastId;
            this.pendingEntries = pendingEntries;
            this.consumers = consumers;
        }
    }

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class Consumer implements Serializable {
        private static final long serialVersionUID = 1L;
        private byte[] name;
        private long seenTime;
        private NavigableMap<ID, Nack> pendingEntries;

        public Consumer(byte[] name, long seenTime, NavigableMap<ID, Nack> pendingEntries) {
            this.name = name;
            this.seenTime = seenTime;
            this.pendingEntries = pendingEntries;
        }


    }

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class Nack implements Serializable {
        private static final long serialVersionUID = 1L;
        private ID id;
        private Consumer consumer;
        private long deliveryTime;
        private long deliveryCount;

        public Nack(ID id, Consumer consumer, long deliveryTime, long deliveryCount) {
            this.id = id;
            this.consumer = consumer;
            this.deliveryTime = deliveryTime;
            this.deliveryCount = deliveryCount;
        }
    }

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class ID implements Serializable, Comparable<ID> {
        private static final long serialVersionUID = 1L;
        public static Comparator<ID> COMPARATOR = comparator();

        private long ms;
        private long seq;



        public ID(long ms, long seq) {
            this.ms = ms;
            this.seq = seq;
        }



        public ID delta(long ms, long seq) {
            return new ID(this.ms + ms, this.seq + seq);
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ID id = (ID) o;
            return ms == id.ms && seq == id.seq;
        }

        @Override
        public int hashCode() {
            return Objects.hash(ms, seq);
        }

        @Override
        public int compareTo(ID that) {
            int r = Long.compare(this.ms, that.ms);
            if (r == 0) {
                return Long.compare(this.seq, that.seq);
            }
            return r;
        }

        public static ID valueOf(String id) {
            int idx = id.indexOf('-');
            long ms = Long.parseLong(id.substring(0, idx));
            long seq = Long.parseLong(id.substring(idx + 1, id.length()));
            return new ID(ms, seq);
        }

        public static ID valueOf(String strMs, String strSeq) {
            long ms = Long.parseLong(strMs);
            long seq = Long.parseLong(strSeq);
            return new ID(ms, seq);
        }

        public static Comparator<ID> comparator() {
            return new Comparator<ID>() {
                @Override
                public int compare(ID o1, ID o2) {
                    return o1.compareTo(o2);
                }
            };
        }
    }
}

