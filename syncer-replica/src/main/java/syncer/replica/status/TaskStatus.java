package syncer.replica.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author: Eq Zhan
 * @create: 2021-01-26
 **/
@AllArgsConstructor
public enum TaskStatus implements Serializable {

    /**
     * 创建中
     */
    CREATING(Status.CREATING,1,"creating"),

    /**
     * 创建完成（完成数据校验阶段）
     */
    CREATED(Status.CREATED,2,"created"),


    /**
     * 任务启动中
     */
    STARTING(Status.STARTING,3,"starting"),


    /**
     * 全量任务进行中
     */
    RDBRUNNING(Status.RDBRUNNING,6,"rdbrunning"),


    /**
     * 增量任务进行中
     */
    COMMANDRUNNING(Status.COMMANDRUNNING,7,"commandrunning"),


    /**
     * 停止
     */
    STOP(Status.STOP,0,"stop"),



    /**
     * 任务完成
     */

    FINISH(Status.FINISH,8,"finish"),

    /**
     * 任务因异常而停止
     */
    BROKEN(Status.BROKEN,5,"broken"),


    /**
     * 任务因异常而停止
     */
    FAILOVER(Status.FAILOVER,9,"failover");



    public static TaskStatus getTaskStatusTypeByName(String name){
        if(name.equalsIgnoreCase(TaskStatus.CREATING.getMsg())){
            return TaskStatus.CREATING;
        }else if(name.equalsIgnoreCase(TaskStatus.CREATED.getMsg())){
            return TaskStatus.CREATED;
        }else if(name.equalsIgnoreCase(TaskStatus.STARTING.getMsg())){
            return TaskStatus.STARTING;
        }else if(name.equalsIgnoreCase(TaskStatus.RDBRUNNING.getMsg())){
            return TaskStatus.RDBRUNNING;
        }else if(name.equalsIgnoreCase(TaskStatus.COMMANDRUNNING.getMsg())){
            return TaskStatus.COMMANDRUNNING;
        }else if(name.equalsIgnoreCase(TaskStatus.STOP.getMsg())){
            return TaskStatus.STOP;
        }else if(name.equalsIgnoreCase(TaskStatus.BROKEN.getMsg())){
            return TaskStatus.BROKEN;
        }else if(name.equalsIgnoreCase(TaskStatus.FINISH.getMsg())){
            return TaskStatus.FINISH;
        }else if(name.equalsIgnoreCase(TaskStatus.FAILOVER.getMsg())){
            return TaskStatus.FAILOVER;
        }

        return null;
    }



    @Getter
    private Status status;
    @Getter
    private int code;
    @Getter
    private String msg;
}
