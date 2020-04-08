package syncer.syncerreplication.replicator;

import syncer.syncerreplication.constant.SyncerStatus;
import syncer.syncerreplication.event.Event;
import syncer.syncerreplication.event.EventListener;
import syncer.syncerreplication.io.RawByteListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/8
 */
public class AbstractReplicatorListener implements ReplicatorListener {
     final List<CloseListener> closeListeners = new CopyOnWriteArrayList<>();
     final List<EventListener> eventListeners = new CopyOnWriteArrayList<>();
     final List<SyncerStatusListener> statusListeners = new CopyOnWriteArrayList<>();
     final List<RawByteListener> rawByteListeners = new CopyOnWriteArrayList<>();
     final List<ExceptionListener> exceptionListeners = new CopyOnWriteArrayList<>();

    @Override
    public boolean addEventListener(EventListener listener) {
        return eventListeners.add(listener);
    }

    @Override
    public boolean removeEventListener(EventListener listener) {
        return eventListeners.remove(listener);
    }

    @Override
    public boolean addRawByteListener(RawByteListener listener) {
        return this.rawByteListeners.add(listener);
    }

    @Override
    public boolean removeRawByteListener(RawByteListener listener) {
        return this.rawByteListeners.remove(listener);
    }

    @Override
    public boolean addCloseListener(CloseListener listener) {
        return closeListeners.add(listener);
    }

    @Override
    public boolean removeCloseListener(CloseListener listener) {
        return closeListeners.remove(listener);
    }

    @Override
    public boolean addExceptionListener(ExceptionListener listener) {
        return exceptionListeners.add(listener);
    }

    @Override
    public boolean removeExceptionListener(ExceptionListener listener) {
        return exceptionListeners.remove(listener);
    }

    @Override
    public boolean addStatusListener(SyncerStatusListener listener) {
        return statusListeners.add(listener);
    }

    @Override
    public boolean removeStatusListener(SyncerStatusListener listener) {
        return statusListeners.remove(listener);
    }

    protected void doEventListener(Replicator replicator, Event event) {
        if (eventListeners.isEmpty()) {
            return;
        }
        for (EventListener listener : eventListeners) {
            listener.onEvent(replicator, event);
        }
    }

    protected void doCloseListener(Replicator replicator) {
        if (closeListeners.isEmpty()){
            return;
        }
        for (CloseListener listener : closeListeners) {
            listener.handle(replicator);
        }
    }

    protected void doExceptionListener(Replicator replicator, Throwable throwable, Event event) {
        if (exceptionListeners.isEmpty()) {
            return;
        }
        for (ExceptionListener listener : exceptionListeners) {
            listener.handle(replicator, throwable, event);
        }
    }


    protected void doStatusListener(Replicator replicator, SyncerStatus status) {
        if (statusListeners.isEmpty()) {
            return;
        }
        for (SyncerStatusListener listener : statusListeners) {
            listener.handle(replicator, status);
        }
    }
}
