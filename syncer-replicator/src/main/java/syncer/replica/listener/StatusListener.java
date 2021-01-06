package syncer.replica.listener;

import syncer.replica.event.SyncerEvent;
import syncer.replica.replication.Replication;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
@FunctionalInterface
public interface StatusListener {
    void handle(Replication replication, SyncerEvent status);
}