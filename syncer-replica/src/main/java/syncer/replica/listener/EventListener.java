package syncer.replica.listener;

import syncer.replica.event.Event;
import syncer.replica.replication.Replication;

/**
 * @author: Eq Zhan
 * @create: 2021-03-12
 **/
public interface EventListener {
    void onEvent(Replication replication, Event event);

    String eventListenerName();
}
