package syncer.syncerreplication.replicator;

import syncer.syncerreplication.exception.IncrementException;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description 同步任务接口
 * @Date 2020/4/7
 */
public interface Replicator extends Closeable, ReplicatorRegister, ReplicatorListener {

    void open() throws IOException, IncrementException;

    void open(String taskId) throws IOException, IncrementException;

    @Override
    void close() throws IOException;
}
