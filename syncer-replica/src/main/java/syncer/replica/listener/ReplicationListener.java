package syncer.replica.listener;

/**
 * @author: Eq Zhan
 * @create: 2021-03-12
 **/

public interface ReplicationListener {
    /*
     * Event
     */
    boolean addEventListener(EventListener listener);

    boolean removeEventListener(EventListener listener);

    /*
     * Raw byte
     */
    boolean addRawByteListener(TaskRawByteListener listener);

    boolean removeRawByteListener(TaskRawByteListener listener);


    /**
     * TASK STATUS
     * @param listener
     * @return
     */
    boolean addTaskStatusListener(TaskStatusListener listener);

    boolean removeTaskStatusListener(TaskStatusListener listener);

}
