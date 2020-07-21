package syncer.syncerreplication.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
@AllArgsConstructor
@NoArgsConstructor
public enum SyncerStatus {

    /**
     * 停止
     */
    STOP(Status.STOP,0,"stop"),
    /**
     * 任务创建中
     */
    CREATING(Status.CREATING,1,"creating"),

    /**
     * 任务创建完成（完成数据校验阶段）
     */
    CREATED(Status.CREATED,2,"created"),


    /**
     * 源Redis节点建立连接中
     */
    CONNECTING(Status.CONNECTING,3,"源Redis节点连接中"),

    /**
     * 源Redis节点已连接   */
    CONNECTED(Status.CONNECTED,4,"源Redis节点连接完成"),


    /**
     * 运行状态
     * 准备拆分 TASK_BEGAIN PSYNC
     */
    RUN(Status.RUN,5,"任务进入运行状态"),


    /**
     * 全量任务进行中
     */
    RDBRUNING(Status.RDBRUNING,6,"rdbrunning"),

    /**
     * 增量任务进行中
     */
    COMMANDRUNING(Status.COMMANDRUNING,7,"commandrunning"),


    /**
     * 源Redis断开连接中
   */
    DISCONNECTING(Status.DISCONNECTING,8,"源Redis节点连接完成"),

    /**
     * 源Redis断开连接成功
     */
    DISCONNECTED(Status.DISCONNECTED,9,"源Redis节点连接完成"),


    /**
     * 任务因异常而停止
     */
    BROKEN(Status.BROKEN,10,"broken");


    @Getter
    private Status status;
    @Getter
    private int code;
    @Getter
    private String msg;

}
