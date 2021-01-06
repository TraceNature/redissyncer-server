package syncer.replica.listener;

import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.replication.Replication;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/18
 */
@FunctionalInterface
public interface TaskStatusListener {
    void handle(Replication replication, SyncerTaskEvent event);
}
