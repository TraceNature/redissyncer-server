package syncer.syncerservice.filter.strategy_type;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/26
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
