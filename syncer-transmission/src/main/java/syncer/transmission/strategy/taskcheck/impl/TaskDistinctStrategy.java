package syncer.transmission.strategy.taskcheck.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import syncer.common.constant.ResultCodeAndMessage;
import syncer.common.exception.TaskMsgException;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.util.code.CodeUtils;
import syncer.transmission.util.sql.SqlOPUtils;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 判断任务是否重复
 * @Date 2020/12/14
 */
@AllArgsConstructor
@Builder

public class TaskDistinctStrategy implements ITaskCheckStrategy {
    private ITaskCheckStrategy next;
    private RedisClient client;
    private TaskModel taskModel;


    @Override
    public void run(RedisClient client, TaskModel taskModel) throws Exception {
        List<TaskModel> taskModelList = SqlOPUtils.findTaskBytaskMd5(taskModel.getMd5());
        if (taskModelList != null && taskModelList.size() > 0) {
            throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASKSETTING_ERROR.getCode(), ResultCodeAndMessage.TASK_MSG_TASKSETTING_ERROR.getMsg()));
        }
        //下一节点
        toNext(client, taskModel);

    }

    @Override
    public void toNext(RedisClient client, TaskModel taskModel) throws Exception {
        if (null != next) {
            next.run(client, taskModel);
        }
    }

    @Override
    public void setNext(ITaskCheckStrategy nextStrategy) {
        this.next = nextStrategy;
    }
}
