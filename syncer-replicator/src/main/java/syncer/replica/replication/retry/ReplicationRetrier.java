package syncer.replica.replication.retry;

import syncer.replica.exception.IncrementException;
import syncer.replica.replication.Replication;

import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description Replication重试接口
 * @Date 2020/8/7
 */
public interface ReplicationRetrier {
    void retry(Replication replication) throws IOException, IncrementException;
}
