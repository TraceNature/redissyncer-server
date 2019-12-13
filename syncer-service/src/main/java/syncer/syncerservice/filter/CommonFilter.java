package syncer.syncerservice.filter;

import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.po.KeyValueEventEntity;


public interface CommonFilter {

    void run(Replicator replicator, KeyValueEventEntity eventEntity);

    void toNext(Replicator replicator, KeyValueEventEntity eventEntity);

    void setNext(CommonFilter nextFilter);
}
