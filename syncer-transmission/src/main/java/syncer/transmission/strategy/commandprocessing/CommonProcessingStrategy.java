package syncer.transmission.strategy.commandprocessing;

import syncer.replica.replication.Replication;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.po.entity.KeyValueEventEntity;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
public interface CommonProcessingStrategy {
    void run(Replication replication, KeyValueEventEntity eventEntity) throws StartegyNodeException;

    void toNext(Replication replication, KeyValueEventEntity eventEntity) throws StartegyNodeException;

    void setNext(CommonProcessingStrategy nextStrategy);
}
