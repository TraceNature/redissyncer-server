package syncer.replica.replication;

import syncer.replica.replication.async.AsyncReplication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/14
 */
public class CommonReplications {
    /*
     * SYNC
     */
    public static void open(Replication replicator) {
        try {
            Objects.requireNonNull(replicator).open();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void close(Replication replicator) {
        try {
            Objects.requireNonNull(replicator).close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void openQuietly(Replication replicator) {
        try {
            open(replicator);
        } catch (Throwable e) {
        }
    }

    public static void closeQuietly(Replication replicator) {
        try {
            close(replicator);
        } catch (Throwable e) {
        }
    }

    /*
     * ASYNC
     */
    public static CompletableFuture<Void> open(AsyncReplication replicator) {
        return open(replicator, null);
    }

    public static CompletableFuture<Void> close(AsyncReplication replicator) {
        return close(replicator, null);
    }

    public static CompletableFuture<Void> open(AsyncReplication replicator, Executor executor) {
        return Objects.requireNonNull(replicator).open(executor);
    }

    public static CompletableFuture<Void> close(AsyncReplication replicator, Executor executor) {
        return Objects.requireNonNull(replicator).close(executor);
    }
}
