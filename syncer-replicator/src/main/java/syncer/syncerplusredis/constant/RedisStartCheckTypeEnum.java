package syncer.syncerplusredis.constant;

/**
 * 任务启动检查类型枚举
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/25
 */
public enum RedisStartCheckTypeEnum {
    /**
     * 单节点到单节点
     */
    SINGLE_REDIS_TO_SINGLE_REDIS,

    /**
     * 单节点到多节点
     */
    SINGLE_REDIS_TO_CLUSTER,

    /**
     * 文件到单节点
     */
    FILE_TO_SINGLE_REDIS,

    /**
     * 文件到多节点
     */

    FILE_TO_CLUSTER,

    SINGLE_REDIS_TO_FILE
}
