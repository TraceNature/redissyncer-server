package syncer.replica.replication;

import syncer.replica.listener.ReplicationListener;
import syncer.replica.register.ReplicationRegister;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author: Eq Zhan
 * @create: 2021-03-12
 **/
public interface Replication  extends Closeable, ReplicationRegister, ReplicationListener {
    void open() throws IOException;

    @Override
    void close() throws IOException;
}
