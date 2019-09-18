package com.i1314i.syncerplusservice.replicator.service;



import com.i1314i.syncerplusredis.replicator.Replicator;

import java.io.IOException;

public interface JDReplicatorRetrier {
    void retry(Replicator replicator) throws IOException;
}
