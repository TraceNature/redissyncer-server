package syncer.replica.datatype.rdb.stream;

import java.io.Serializable;

/**
 * https://github.com/redis/redis/blob/5.0.0/src/stream.h
 * 消费者组中的挂起（尚未确认）消息。
 * Pending (yet not acknowledged) message in a consumer group.

     typedef struct streamNACK {
     mstime_t delivery_time;     //Last time this message was delivered.
     uint64_t delivery_count;    // Number of times this message was delivered.
     streamConsumer * consumer;   // The consumer this message was delivered to in the last delivery.
     }streamNACK;

 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public class StreamNack implements Serializable {
    private static final long serialVersionUID = 1L;
    private StreamID id;
    /**
     * The consumer this message was delivered to  in the last delivery.
     */
    private StreamConsumer consumer;
    /**
     * Last time this message was delivered.
     */
    private long deliveryTime;
    /**
     *  Number of times this message was delivered.
     */
    private long deliveryCount;

    public StreamNack() {

    }

    public StreamNack(StreamID id, StreamConsumer consumer, long deliveryTime, long deliveryCount) {
        this.id = id;
        this.consumer = consumer;
        this.deliveryTime = deliveryTime;
        this.deliveryCount = deliveryCount;
    }

    public StreamID getId() {
        return id;
    }

    public void setId(StreamID id) {
        this.id = id;
    }

    public StreamConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(StreamConsumer consumer) {
        this.consumer = consumer;
    }

    public long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public long getDeliveryCount() {
        return deliveryCount;
    }

    public void setDeliveryCount(long deliveryCount) {
        this.deliveryCount = deliveryCount;
    }

    @Override
    public String toString() {
        return "StreamNACK{" +
                "id=" + id +
                ", consumer=" + consumer +
                ", deliveryTime=" + deliveryTime +
                ", deliveryCount=" + deliveryCount +
                '}';
    }
}
