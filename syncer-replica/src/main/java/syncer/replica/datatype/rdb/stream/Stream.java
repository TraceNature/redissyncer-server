package syncer.replica.datatype.rdb.stream;


import java.util.List;
import java.util.NavigableMap;

/**
 * https://github.com/antirez/redis/blob/5.0.0/src/stream.h
 *
 *  radix tree的实现
 *       https://github.com/redis/redis/blob/5.0.0/src/rax.c
 *       https://github.com/antirez/redis/blob/5.0.0/src/rax.h
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class Stream {
    private static final long serialVersionUID = 1L;
    /**
     *  Zero if there are yet no items.
     */
    private StreamID lastId;
    private NavigableMap<StreamID, StreamEntry> entries;
    /**
     *Number of elements inside this stream.
     */
    private long length;
    /**
     *  Consumer groups dictionary: name -> streamCG
     */
    private List<StreamGroup> groups;


    /**
     *  "1601372563177-0"
     *
     *  redis> XADD mystream * name Sara surname OConnor
     * "1601372323627-0"
     * redis> XADD mystream * field1 value1 field2 value2 field3 value3
     * "1601372323627-1"
     * redis> XLEN mystream
     * (integer) 2
     * redis> XRANGE mystream - +
     * 1) 1) "1601372323627-0"
     *    2) 1) "name"
     *       2) "Sara"
     *       3) "surname"
     *       4) "OConnor"
     * 2) 1) "1601372323627-1"
     *    2) 1) "field1"
     *       2) "value1"
     *       3) "field2"
     *       4) "value2"
     *       5) "field3"
     *       6) "value3"
     * redis>
     */



    public Stream() {

    }

    public Stream(StreamID lastId, NavigableMap<StreamID, StreamEntry> entries, long length, List<StreamGroup> groups) {
        this.lastId = lastId;
        this.entries = entries;
        this.length = length;
        this.groups = groups;
    }

    public StreamID getLastId() {
        return lastId;
    }

    public void setLastId(StreamID lastId) {
        this.lastId = lastId;
    }

    public NavigableMap<StreamID, StreamEntry> getEntries() {
        return entries;
    }

    public void setEntries(NavigableMap<StreamID, StreamEntry> entries) {
        this.entries = entries;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public List<StreamGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<StreamGroup> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        String r = "Stream{" + "lastId=" + lastId + ", length=" + length;
        if (groups != null && !groups.isEmpty()){
            r += ", groups=" + groups;
        }
        if (entries != null && !entries.isEmpty()){
            r += ", entries=" + entries.size();
        }
        return r + '}';
    }
}
