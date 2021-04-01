package syncer.replica.datatype.rdb.stream;

import syncer.replica.util.strings.Strings;

import java.io.Serializable;
import java.util.List;
import java.util.NavigableMap;

/**
 * Consumer Group ：消费组  typedef struct streamCG
 * https://github.com/redis/redis/blob/5.0.0/src/stream.h
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class StreamGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] name;
    /**
     * last_delivered_id
     * 游标，每个消费组会有个游标 last_delivered_id，任意一个消费者读取了消息都会使游标 last_delivered_id 往前移动。
     * Last delivered (not acknowledged) ID for this group. Consumers that will just ask for more messages will served with IDs > than this.
     */
    private StreamID lastId;
    /**
     * 消费者(Consumer)的状态变量，作用是维护消费者的未确认的 id。
     * pending_ids 记录了当前已经被客户端读取的消息，但是还没有 ack (Acknowledge character：确认字符）。
     *
     * Pending entries list. This is a radix tree that has every message delivered to consumers (without the NOACK option)
     * that was yet not acknowledged as processed. The key of the radix tree is the ID as a 64 bit big endian number,
     * while the associated value is a streamNACK structure.
     */
    private NavigableMap<StreamID, StreamNack> pendingEntries;

    /**
     * A radix tree representing the consumers by name
     * and their associated representation in the form of streamConsumer structures.
     */
    private List<StreamConsumer> consumers;

    public StreamGroup() {

    }

    public StreamGroup(byte[] name, StreamID lastId, NavigableMap<StreamID, StreamNack> pendingEntries, List<StreamConsumer> consumers) {
        this.name = name;
        this.lastId = lastId;
        this.pendingEntries = pendingEntries;
        this.consumers = consumers;
    }

    public byte[] getName() {
        return name;
    }

    public void setName(byte[] name) {
        this.name = name;
    }

    public StreamID getLastId() {
        return lastId;
    }

    public void setLastId(StreamID lastId) {
        this.lastId = lastId;
    }

    public NavigableMap<StreamID, StreamNack> getPendingEntries() {
        return pendingEntries;
    }

    public void setPendingEntries(NavigableMap<StreamID, StreamNack> pendingEntries) {
        this.pendingEntries = pendingEntries;
    }

    public List<StreamConsumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<StreamConsumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public String toString() {
        String r = "StreamGroup{" + "name='" + Strings.toString(name) + '\'' + ", lastId=" + lastId;
        if (consumers != null && !consumers.isEmpty()){
            r += ", consumers=" + consumers;
        }
        if (pendingEntries != null && !pendingEntries.isEmpty()){
            r += ", gpel=" + pendingEntries.size();
        }
        return r + '}';
    }

}
