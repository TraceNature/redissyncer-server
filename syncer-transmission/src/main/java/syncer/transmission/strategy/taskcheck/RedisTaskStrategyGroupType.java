package syncer.transmission.strategy.taskcheck;

/**
 * @author zhanenqiang
 * @Description 任务创建检测策略类型
 * @Date 2020/12/14
 */
public enum  RedisTaskStrategyGroupType {

    /**
     * sync组
     */
    SYNCGROUP,

    /**
     * 统一策略不判断是否重复
     */
    NODISTINCT,

    /**
     * 文件组
     */
    FILEGROUP,

    /**
     * 实时备份AOF任务组
     */
    COMMANDUPGROUP
}