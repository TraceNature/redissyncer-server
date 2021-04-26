package syncer.replica.status;

/**
 * @author: Eq Zhan
 * @create: 2021-01-26
 **/
public enum Status {
    /**
     * 创建中
     */
    CREATING,
    /**
     * 创建完成
     */
    CREATED,
    /**
     * 启动中
     */
    STARTING,
    /**
     * RDB同步
     */
    RDBRUNNING,
    /**
     * AOF/增量数据同步
     */
    COMMANDRUNNING,
    /**
     * 任务手动停止
     */
    STOP,
    /**
     * 任务完成
     */
    FINISH,

    /**
     * 任务异常
     */
    BROKEN,

    /**
     * sentinel故障转移
     */
    FAILOVER
}
