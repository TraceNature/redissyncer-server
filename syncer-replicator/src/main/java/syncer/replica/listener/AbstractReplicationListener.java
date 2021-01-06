package syncer.replica.listener;

import syncer.replica.event.Event;
import syncer.replica.event.SyncerEvent;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.replication.Replication;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/10
 */
public class AbstractReplicationListener implements ReplicationListener {

    protected final List<CloseListener> closeListeners = new CopyOnWriteArrayList<>();
    protected final List<EventListener> eventListeners = new CopyOnWriteArrayList<>();
    protected final List<StatusListener> statusListeners = new CopyOnWriteArrayList<>();
    protected final List<RawByteListener> rawByteListeners = new CopyOnWriteArrayList<>();
    protected final List<ExceptionListener> exceptionListeners = new CopyOnWriteArrayList<>();
    protected final List<TaskStatusListener> taskStatusListeners = new CopyOnWriteArrayList<>();

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
    public boolean addStatusListener(StatusListener listener) {
        return statusListeners.add(listener);
    }

    @Override
    public boolean removeStatusListener(StatusListener listener) {
        return statusListeners.remove(listener);
    }

    @Override
    public boolean addTaskStatusListener(TaskStatusListener listener) {
        return taskStatusListeners.add(listener);
    }

    @Override
    public boolean removeTaskStatusListener(TaskStatusListener listener) {
        return taskStatusListeners.remove(listener);
    }


    /**
     * 发送事件消息
     * @param replicator
     * @param event
     */
    public void doEventListener(Replication replicator, Event event) {
        if (eventListeners.isEmpty()) {
            return;
        }
        for (EventListener listener : eventListeners) {
            listener.onEvent(replicator, event);
        }
    }


    /**
     * 发送关闭消息
     * @param replicator
     */
    public void doCloseListener(Replication replicator) {
        if (closeListeners.isEmpty()) {
            return;
        }
        for (CloseListener listener : closeListeners) {
            listener.handle(replicator);
        }
    }

    /**
     * 发送异常消息
     * @param replicator
     * @param throwable
     * @param event
     */
    public void doExceptionListener(Replication replicator, Throwable throwable, Event event) {
        if (exceptionListeners.isEmpty()){
            return;
        }
        for (ExceptionListener listener : exceptionListeners) {
            listener.handle(replicator, throwable, event);
        }
    }


    /**
     * 发送状态改变消息
     * @param replicator
     * @param status
     */
    public void doStatusListener(Replication replicator, SyncerEvent status) {
        if (statusListeners.isEmpty()) {
            return;
        }
        for (StatusListener listener : statusListeners) {
            listener.handle(replicator, status);
        }
    }

    /**
     * 发送状态改变消息
     * @param replicator
     * @param event
     */
    public void doTaskStatusListener(Replication replicator, SyncerTaskEvent event) {
        if (taskStatusListeners.isEmpty()) {
            return;
        }
        for (TaskStatusListener listener : taskStatusListeners) {
            listener.handle(replicator, event);
        }
    }

}
