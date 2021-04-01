package syncer.replica.listener;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.event.Event;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.replication.Replication;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
@Slf4j
public abstract class AbstractReplicationListener implements ReplicationListener {

    private final List<EventListener> eventListenerList=new CopyOnWriteArrayList<>();

    private final List<TaskStatusListener> taskStatusListenerList=new CopyOnWriteArrayList<>();
    protected final List<TaskRawByteListener> rawByteListenerList = new CopyOnWriteArrayList<>();

    @Override
    public boolean addEventListener(EventListener listener) {
        return eventListenerList.add(listener);
    }

    @Override
    public boolean removeEventListener(EventListener listener) {
        return eventListenerList.remove(listener);
    }

    @Override
    public boolean addTaskStatusListener(TaskStatusListener listener) {
        return taskStatusListenerList.add(listener);
    }

    @Override
    public boolean removeTaskStatusListener(TaskStatusListener listener) {
        return taskStatusListenerList.remove(listener);
    }

    /**
     * 发送事件消息
     * @param replication
     * @param event
     */
    public void doEventListener(Replication replication, Event event) {
        if (eventListenerList.isEmpty()) {
            return;
        }
        for (EventListener listener : eventListenerList) {
            try {
                listener.onEvent(replication, event);
            }catch (Exception e){
                log.error("send event to listener {} fail",listener.eventListenerName());
            }

        }
    }


    /**
     * 发送状态改变消息
     * @param replication
     * @param event
     */
    public void doTaskStatusListener(Replication replication, SyncerTaskEvent event) {
        if (taskStatusListenerList.isEmpty()) {
            return;
        }
        for (TaskStatusListener listener : taskStatusListenerList) {
            try {
                listener.handler(replication, event);
            }catch (Exception e){
                log.error("send task status change event to listener {} fail",listener.eventListenerName());
            }

        }
    }




    @Override
    public boolean addRawByteListener(TaskRawByteListener listener) {
        return false;
    }

    @Override
    public boolean removeRawByteListener(TaskRawByteListener listener) {
        return false;
    }
}
