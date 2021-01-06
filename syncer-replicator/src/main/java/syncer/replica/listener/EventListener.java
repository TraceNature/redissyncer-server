package syncer.replica.listener;

import syncer.replica.event.Event;
import syncer.replica.replication.Replication;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public interface EventListener {
    void onEvent(Replication replication, Event event);
}
