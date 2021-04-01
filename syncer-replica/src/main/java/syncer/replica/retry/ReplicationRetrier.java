package syncer.replica.retry;

import syncer.replica.exception.IncrementException;
import syncer.replica.replication.Replication;

import java.io.IOException;

/**
 * Replication重试接口
 * @author: Eq Zhan
 * @create: 2021-03-19
 **/
public interface ReplicationRetrier {
    void retry(Replication replication) throws IOException, IncrementException;
}
