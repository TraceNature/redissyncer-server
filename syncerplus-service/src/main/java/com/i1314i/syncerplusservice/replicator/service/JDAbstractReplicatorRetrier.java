package com.i1314i.syncerplusservice.replicator.service;



import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.replicator.Replicator;

import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class JDAbstractReplicatorRetrier implements JDReplicatorRetrier {
    protected int retries = 0;

    protected abstract boolean isManualClosed();

    protected abstract boolean open() throws IOException;

    protected abstract boolean connect() throws IOException;

    protected abstract boolean close(IOException reason) throws IOException;

    @Override
    public void retry(Replicator replicator) throws IOException {
        IOException exception = null;
        Configuration configuration = replicator.getConfiguration();
        for (; retries < configuration.getRetries() || configuration.getRetries() <= 0; retries++) {
            exception = null;
            if (isManualClosed()) break;
            final long interval = configuration.getRetryTimeInterval();
            try {
                if (connect()) {
                    reset();
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
        if (exception != null) throw exception;
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