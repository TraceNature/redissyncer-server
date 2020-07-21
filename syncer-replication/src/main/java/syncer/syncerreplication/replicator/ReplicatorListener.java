package syncer.syncerreplication.replicator;

import syncer.syncerreplication.event.EventListener;
import syncer.syncerreplication.io.RawByteListener;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
public interface ReplicatorListener {

    /**
     * Event
     * 命令事件监听器
     * @param listener
     * @return
     */
    boolean addEventListener(EventListener listener);

    boolean removeEventListener(EventListener listener);



    /**
     *  Raw byte
     *
     * 原始字节命令监听器
     * @param listener
     * @return
     */
    boolean addRawByteListener(RawByteListener listener);

    boolean removeRawByteListener(RawByteListener listener);



    /**
     * Close
     *
     * 关闭监听器
     * @param listener
     * @return
     */
    boolean addCloseListener(CloseListener listener);

    boolean removeCloseListener(CloseListener listener);


    /**
     * Exception
     *
     * 注册异常监听器
     * @param listener
     * @return
     */
    boolean addExceptionListener(ExceptionListener listener);

    boolean removeExceptionListener(ExceptionListener listener);



    /**
     * Connection
     *
     * 注册状态监听器
     * @param listener
     * @return
     */
    boolean addStatusListener(SyncerStatusListener listener);

    boolean removeStatusListener(SyncerStatusListener listener);
}
