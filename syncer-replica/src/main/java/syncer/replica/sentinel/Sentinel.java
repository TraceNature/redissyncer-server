package syncer.replica.sentinel;


import java.io.Closeable;
import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description 哨兵模式
 * @Date 2020/8/25
 */
public interface Sentinel extends Closeable {
    void open() throws IOException;

    void close() throws IOException;

    boolean addSentinelListener(SentinelListener listener);

    boolean removeSentinelListener(SentinelListener listener);
}
