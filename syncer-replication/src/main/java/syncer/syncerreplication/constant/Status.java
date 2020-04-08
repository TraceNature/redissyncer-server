package syncer.syncerreplication.constant;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
public enum Status {

    /**
     * 任务创建中
     */
    CREATING,

    /**
     * 任务创建完成
     */
    CREATED,

    /**
     * 源Redis节点建立连接中
     */
    CONNECTING,
    /**
     * 源Redis节点已连接   */
    CONNECTED,
    /**
     * 正在断开连接
     */
    DISCONNECTING,

    /**
     * 断开连接成功
     */
    DISCONNECTED,

    /**
     * 全量数据传输中
     */
    RDBRUNING,

    /**
     * 增量命令传播中
     */
    COMMANDRUNING,

    /**
     * 因为异常结束
     */
    BROKEN,

    /**
     * 进入运行状态
     */
    RUN,

    /**
     * 停止状态
     */
    STOP;

}
