package syncer.syncerplusredis.constant;

import lombok.AllArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 任务运行状态
 * @Date 2020/3/10
 */
@AllArgsConstructor
public enum TaskStatusType {

    /**
     * 停止
     */
    STOP(ThreadStatusEnum.STOP,0),
    /**
     * 创建中
     */
    CREATING(ThreadStatusEnum.CREATING,1),

    /**
     * 创建完成（完成数据校验阶段）
     */
    CREATED(ThreadStatusEnum.CREATE,2),

    /**
     * 运行状态
     */
    RUN(ThreadStatusEnum.RUN,3),

    /**
     * 任务暂停
     */

    PAUSE(ThreadStatusEnum.PAUSE,4),

    /**
     * 任务因异常而停止
     */
    BROKEN(ThreadStatusEnum.BROKEN,5);

    private ThreadStatusEnum status;
    private int code;
}
