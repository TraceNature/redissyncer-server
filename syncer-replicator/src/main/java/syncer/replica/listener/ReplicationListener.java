package syncer.replica.listener;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public interface ReplicationListener {
    /*
     * Event
     */
    boolean addEventListener(EventListener listener);

    boolean removeEventListener(EventListener listener);

    /*
     * Raw byte
     */
    boolean addRawByteListener(RawByteListener listener);

    boolean removeRawByteListener(RawByteListener listener);

    /*
     * Close
     */
    boolean addCloseListener(CloseListener listener);

    boolean removeCloseListener(CloseListener listener);

    /*
     * Exception
     */
    boolean addExceptionListener(ExceptionListener listener);

    boolean removeExceptionListener(ExceptionListener listener);

    /*
     * Connection
     */
    boolean addStatusListener(StatusListener listener);

    boolean removeStatusListener(StatusListener listener);

    /**
     * TASK STATUS
     * @param listener
     * @return
     */
    boolean addTaskStatusListener(TaskStatusListener listener);

    boolean removeTaskStatusListener(TaskStatusListener listener);


}
