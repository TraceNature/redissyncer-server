package syncer.replica.replication;

import syncer.replica.listener.ReplicationListener;
import syncer.replica.register.ReplicationRegister;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public interface Replication extends Closeable, ReplicationRegister, ReplicationListener {
    void open() throws IOException;

    @Override
    void close() throws IOException;

}
