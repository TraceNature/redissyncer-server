package syncer.replica.sentinel;

import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/14
 */
public interface Sentinel {
    void open() throws IOException;

    void close() throws IOException;

    boolean addSentinelListener(SentinelListener listener);

    boolean removeSentinelListener(SentinelListener listener);
}
