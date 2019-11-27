package syncerservice.syncerplusredis.extend.replicator.service;



import syncerservice.syncerplusredis.exception.IncrementException;
import syncerservice.syncerplusredis.replicator.Replicator;

import java.io.IOException;

public interface JDReplicatorRetrier {
    void retry(Replicator replicator) throws IOException, IncrementException;
    void retry(Replicator replicator, String taskId) throws IOException, IncrementException;

}
