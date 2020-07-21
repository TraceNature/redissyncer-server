package syncer.syncerreplication.replicator;

import syncer.syncerreplication.event.Event;

/**
 * @author zhanenqiang
 * @Description 异常监听器
 * @Date 2020/4/7
 */
@FunctionalInterface
public interface ExceptionListener {
    void handle(Replicator replicator, Throwable throwable, Event event);
}
