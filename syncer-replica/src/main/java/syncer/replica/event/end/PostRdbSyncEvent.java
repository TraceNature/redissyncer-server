package syncer.replica.event.end;

import syncer.replica.kv.AbstractEvent;

/**
 * @author zhanenqiang
 * @Description 全量结束
 * @Date 2020/8/7
 */
public class PostRdbSyncEvent extends AbstractEvent {

    private static final long serialVersionUID = 1L;

    private long checksum;

    public PostRdbSyncEvent() {
    }

    public PostRdbSyncEvent(long checksum) {
        this.checksum = checksum;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }
}
