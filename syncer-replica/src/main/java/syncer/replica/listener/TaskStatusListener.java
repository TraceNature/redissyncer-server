package syncer.replica.listener;

import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.replication.Replication;

/**
 * @author: Eq Zhan
 * @create: 2021-03-12
 **/
public interface TaskStatusListener {
    void handler(Replication replication, SyncerTaskEvent event);

    String eventListenerName();
}
