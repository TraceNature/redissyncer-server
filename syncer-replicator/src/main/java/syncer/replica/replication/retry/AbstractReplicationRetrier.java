package syncer.replica.replication.retry;

import syncer.replica.constant.SyncStatusType;
import syncer.replica.entity.Configuration;
import syncer.replica.exception.IncrementException;
import syncer.replica.replication.Replication;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author zhanenqiang
 * @Description 复制重试抽象类
 * @Date 2020/8/7
 */
public abstract class AbstractReplicationRetrier implements ReplicationRetrier {
    //重试次数
    protected int retries = 0;
    protected SyncStatusType replicaType=SyncStatusType.RdbSync;
    protected abstract boolean isManualClosed();

    protected abstract boolean open() throws IOException, IncrementException;

    protected abstract boolean connect() throws IOException, IncrementException;

    protected abstract boolean close(IOException reason) throws IOException;



    @Override
    public void retry(Replication replication) throws IOException,IncrementException {
        IOException exception = null;
        Configuration configuration = replication.getConfiguration();
        for (; retries < configuration.getRetries() || configuration.getRetries() <= 0; retries++) {
            exception = null;
            if (isManualClosed()) {
                break;
            }
            final long interval = configuration.getRetryTimeInterval();
            try {
                if (connect()) {
                    reset();
                }

                //mode == SYNC_LATER && socketReplication.getStatus() == CONNECTED
                if (!open()) {
                    //reset();
                    close(null);
                    sleep(interval);

                    if(replicaType.equals(SyncStatusType.CommandSync)){
                        throw new IncrementException("全量阶段异常 同步失败 本阶段禁止重试...");
                    }
                    continue;
                }
                exception = null;
                break;
            } catch (IOException | UncheckedIOException e) {
                exception = translate(e);
                close(exception);
                sleep(interval);
            }
        }

        if (exception != null){
            throw exception;
        }else if(retries >=configuration.getRetries()){
            throw new IOException();
        }
    }


    /**
     * 重试次数置0
     */
    protected void reset() {
        this.retries = 0;
    }

    /**
     * 当前线程sleep
     * @param interval
     */
    protected void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException interrupt) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 异常处理
     * @param e
     * @return
     */
    protected IOException translate(Exception e) {
        if (e instanceof UncheckedIOException) {
            return ((UncheckedIOException) e).getCause();
        } else if (e instanceof IOException) {
            return (IOException) e;
        } else {
            return new IOException(e.getMessage());
        }
    }
}
