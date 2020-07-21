package syncer.syncerreplication.replicator;

import syncer.syncerreplication.exception.IncrementException;

import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description 连接重试
 * @Date 2020/4/7
 */
public interface ReplicatorRetrier {
    void retry(Replicator replicator) throws IOException, IncrementException;

    void retry(Replicator replicator,String taskId) throws IOException;
}
