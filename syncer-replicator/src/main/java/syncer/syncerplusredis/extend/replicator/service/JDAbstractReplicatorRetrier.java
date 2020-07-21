package syncer.syncerplusredis.extend.replicator.service;



import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TaskMsgUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
@Slf4j
public abstract class JDAbstractReplicatorRetrier implements JDReplicatorRetrier {
    protected Integer retries = 0;

    protected abstract boolean isManualClosed();

    protected abstract boolean open() throws IOException, IncrementException;

    protected abstract boolean connect() throws IOException;

    protected abstract boolean close(IOException reason) throws IOException;

    @Override
    public void retry(Replicator replicator) throws IOException, IncrementException {

        IOException exception = null;
        Configuration configuration = replicator.getConfiguration();

        for (; retries < configuration.getRetries() || configuration.getRetries() <= 0; retries++) {
            exception = null;
            if (isManualClosed()){
                break;
            }
            final long interval = configuration.getRetryTimeInterval();
            try {
                if (connect()) {
//                    reset();
                }
                if (!open()) {
                    reset();
                    close(null);
                    sleep(interval);
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
        if (exception != null) {
            throw exception;
        }else if(retries >=configuration.getRetries()){

            throw new IOException();
        }
    }


    @Override
    public void retry(Replicator replicator,String taskId) throws IOException, IncrementException {

        retry(replicator);


        try {
            TaskDataManagerUtils.updateThreadStatusAndMsg(taskId,"重试多次后失败", TaskStatusType.BROKEN);

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId);
//        if (exception != null) {
//            throw exception;
//        }else if(retries >=configuration.getRetries()){
//
//            throw new IOException();
//        }
    }

    protected void reset() {
        this.retries = 0;
    }

    protected void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException interrupt) {
            Thread.currentThread().interrupt();
        }
    }

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