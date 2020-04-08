package syncer.syncerreplication.event;

import syncer.syncerreplication.util.type.SyncerTuple2;

/**
 * @author zhanenqiang
 * @Description 抽象命令接口
 * @Date 2020/4/7
 */
public abstract class AbstractEvent implements Event {
    protected Context context = new ContextImpl();

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private static class ContextImpl implements Context {
        private static final long serialVersionUID = 1L;

        private SyncerTuple2<Long, Long> offsets;

        /**
         * Fetch offset behavior:
         *
         * 1. if event is self-define event like {@link PreRdbSyncEvent}
         *    or {@link PreCommandSyncEvent}  and etc.
         *    then offset.getV2() - offset.getV1() = 0.
         *    Notice that {@link PostRdbSyncEvent} is not self-define event.
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
        public SyncerTuple2<Long, Long> getOffsets() {
            return offsets;
        }

        @Override
        public void setOffsets(SyncerTuple2<Long, Long> offset) {
            this.offsets = offset;
        }
    }
}
