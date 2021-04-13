package syncer.replica.kv;

import syncer.replica.context.Context;
import syncer.replica.event.Event;
import syncer.replica.util.tuple.Tuple2;

/**
 * 如果event来自于文件，则偏移量是相应文件中的起始位置和结束位置。（rdb aof mixed）
 * 全量event来自于rdb文件，增量来自于backlog，因此增量event的offset可能会小于全量event
 * 忽略部分命令的offset RDB_OPCODE_RESIZEDB, RDB_OPCODE_SELECTDB, RDB_OPCODE_MODULE_AUX. 因此offset可能不连续
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public abstract class AbstractEvent implements Event {
    private Context context = new DefaultContext();

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private static class DefaultContext implements Context {
        private static final long serialVersionUID = 1L;

        private Tuple2<Long, Long> offsets;

        private String replid;
        private long currentOffset;

        public static long getSerialVersionUID() {
            return serialVersionUID;
        }


        public Tuple2<Long, Long> getOffsets() {
            return offsets;
        }
        @Override
        public void setReplid(String replid) {
            this.replid = replid;
        }
        @Override
        public void setCurrentOffset(long currentOffset) {
            this.currentOffset = currentOffset;
        }
        @Override
        public String getReplid() {
            return replid;
        }
        @Override
        public long getCurrentOffset() {
            return currentOffset;
        }

        /**
         * Fetch offset behavior:
         *
         * 1. if event is self-define event like {@link }
         *    or {@link }  and etc.
         *    then offset.getV2() - offset.getV1() = 0.
         *    Notice that {@link } is not self-define event.
         *    it contains 9 bytes with 1 byte rdb type and 8 bytes checksum.
         *
         * 2. if event from Rdb file or Aof file or Mixed file(set aof-use-rdb-preamble yes in redis.conf).
         *    the offset is the start position and end position in corresponding file.
         *    Notice that the we ignore following Rdb type's offset
         *    RDB_OPCODE_RESIZEDB, RDB_OPCODE_SELECTDB, RDB_OPCODE_MODULE_AUX. so the offset may be discontinuous.
         *
         * 3. if event from redis replication protocol via socket.
         *    then the KeyValuePair event's offset is the position from Rdb file, and Command event's offset is
         *    redis replication backlog's offset. so the Command event's offset may less then the KeyValuePair event's offset.
         *
         * Calculate the offset:
         *
         * RDB
         * | rdb type(1 byte)        | rdb key value content  |
         * | start offset(inclusion) | end offset(exclusion)  |
         *
         * start offset contains rdb type. so offset.getV2() - offset.getV1() = (rdb type) + (rdb key value content)
         *
         * AOF
         * |*3\r\n$3\r\nset\r\n$1\r\na\r\n$1\r\nb\r\n(set a b) |
         * | start offset(inclusion) , end offset(exclusion)   |
         * then offset.getV2() - offset.getV1() = 27
         *
         * @return a Tuple2 with the start offset and end offset
         */

        @Override
        public Tuple2<Long, Long> getOffset() {
            return offsets;
        }

        @Override
        public void setOffset(Tuple2<Long, Long> offset) {
            this.offsets = offset;
        }
    }
}
