package com.i1314i.syncerplusservice.replicator.service;

import com.moilioncircle.redis.replicator.Replicator;

import java.io.IOException;

public interface JDReplicatorRetrier {
    void retry(Replicator replicator) throws IOException;
}
