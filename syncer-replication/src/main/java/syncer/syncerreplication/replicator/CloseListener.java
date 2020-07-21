package syncer.syncerreplication.replicator;

/**
 * @author zhanenqiang
 * @Description 关闭监听器
 * @Date 2020/4/7
 */
@FunctionalInterface
public interface CloseListener {
    void handle(Replicator replicator);
}
