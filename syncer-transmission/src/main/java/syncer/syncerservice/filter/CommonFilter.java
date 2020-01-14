package syncer.syncerservice.filter;

import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.po.KeyValueEventEntity;


public interface CommonFilter {

    void run(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException;

    void toNext(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException;

    void setNext(CommonFilter nextFilter);

}
