package syncer.transmission.strategy.taskcheck;


import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;

/**
 * @author zhanenqiang
 * @Description 任务启动前检查策略
 * @Date 2020/2/25
 */
public interface ITaskCheckStrategy {

    void run(RedisClient client, TaskModel taskModel) throws Exception;

    void toNext(RedisClient client, TaskModel taskModel) throws Exception;

    void setNext(ITaskCheckStrategy nextStrategy);

}
