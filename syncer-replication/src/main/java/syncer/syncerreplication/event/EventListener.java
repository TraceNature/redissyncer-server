package syncer.syncerreplication.event;

import syncer.syncerreplication.replicator.Replicator;

/**
 * @author zhanenqiang
 * @Description 命令事件监听器接口
 * @Date 2020/4/7
 */
@FunctionalInterface

public interface EventListener {
    /**
     * 触发命令
     * @param replicator
     * @param event
     */
    void onEvent(Replicator replicator, Event event);
}
