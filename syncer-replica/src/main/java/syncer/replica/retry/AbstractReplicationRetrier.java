package syncer.replica.retry;

import syncer.replica.config.ReplicConfig;
import syncer.replica.exception.IncrementException;
import syncer.replica.exception.RedisAuthErrorException;
import syncer.replica.replication.Replication;
import syncer.replica.util.type.SyncStatusType;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author: Eq Zhan
 * @create: 2021-03-19
 **/
public abstract class AbstractReplicationRetrier implements ReplicationRetrier {
    //重试次数
    protected int retries = 0;
    protected SyncStatusType replicaType= SyncStatusType.RdbSync;

    @Override
    public void retry(Replication replication) throws IOException, IncrementException {
        ReplicConfig config=replication.getConfig();
        IOException exception = null;

        for (;retries<config.getRetries()||config.getRetries()<=0;retries++){
            exception = null;
            if (isManualClosed()) {
                break;
            }
            //重试间隔时间
            final long interval = config.getRetryTimeInterval();
            try {
                //若连接成功... 则置0
                if (connect()) {
                    if(!config.isFullResyncBrokenTask()){
                        reset();
                    }
                }

                //mode == SYNC_LATER && socketReplication.getStatus() == RDBRUNNING||COMMANDRUNNING
                if (!open(retries)) {
                    //reset();
                    close(null);
                    sleep(interval);

                    //若此时处于增量状态，结束任务
                    //else 重试
                    if (replicaType.equals(SyncStatusType.CommandSync)) {
                        throw new IncrementException("全量阶段异常 同步失败 本阶段禁止重试...");
                    }
                    continue;
                }
                exception = null;
                break;
            }catch (RedisAuthErrorException e){
                throw new IncrementException(e.getMessage());
            } catch (IOException | UncheckedIOException e) {
                exception = translate(e);
                close(exception);
                sleep(interval);
            }
        }

        if (exception != null){
            throw exception;
        }else if(retries >=config.getRetries()){
            throw new IOException("the number of retries exceeds the threshold");
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

    public abstract boolean isManualClosed();

    public abstract boolean open() throws IOException, IncrementException, RedisAuthErrorException;

    public abstract boolean open(int retries) throws IOException, IncrementException, RedisAuthErrorException;

    public abstract boolean connect() throws IOException, IncrementException, RedisAuthErrorException;

    public abstract boolean close(IOException reason) throws IOException;

}
