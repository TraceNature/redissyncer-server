package syncer.syncerplusredis.extend.replicator.service;



import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.replicator.Replicator;

import java.io.IOException;

public interface JDReplicatorRetrier {
    void retry(Replicator replicator) throws IOException, IncrementException;
    void retry(Replicator replicator, String taskId) throws IOException, IncrementException;

}
