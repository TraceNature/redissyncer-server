package com.i1314i.syncerplusredis.extend.replicator.service;



import com.i1314i.syncerplusredis.exception.IncrementException;
import com.i1314i.syncerplusredis.replicator.Replicator;

import java.io.IOException;

public interface JDReplicatorRetrier {
    void retry(Replicator replicator) throws IOException, IncrementException;
    void retry(Replicator replicator, String taskId) throws IOException, IncrementException;

}
