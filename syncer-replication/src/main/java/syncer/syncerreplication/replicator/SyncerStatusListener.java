package syncer.syncerreplication.replicator;

import syncer.syncerreplication.constant.SyncerStatus;

/**
 * @author zhanenqiang
 * @Description 任务状态监听器
 * @Date 2020/4/7
 */
@FunctionalInterface
public interface SyncerStatusListener {
    void handle(Replicator replicator, SyncerStatus status);
}
