package syncer.replica.replication.async;

import syncer.replica.listener.ReplicationListener;
import syncer.replica.register.ReplicationRegister;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/14
 */
public interface AsyncReplication extends ReplicationRegister, ReplicationListener {
    CompletableFuture<Void> open(Executor executor);

    CompletableFuture<Void> close(Executor executor);
}
