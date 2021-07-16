package syncer.transmission.strategy.taskcheck.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import syncer.common.constant.ResultCodeAndMessage;
import syncer.common.exception.TaskMsgException;
import syncer.replica.constant.RedisType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.util.code.CodeUtils;
import syncer.transmission.util.strings.StringUtils;

/**
 * targetRedisName ckeck strategy
 */
@AllArgsConstructor
@Builder
public class TaskTargetTaskNameCheckStrategy implements ITaskCheckStrategy {
    private ITaskCheckStrategy next;
    private RedisClient client;
    private TaskModel taskModel;
    @Override
    public void run(RedisClient client, TaskModel taskModel) throws Exception {
        if (!RedisType.KAFKA.getCode().equals(taskModel.getTargetRedisType())) {
            if (StringUtils.isEmpty(taskModel.getTargetRedisAddress())) {
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASK_TARGET_REDIS_ADDRESS_NULL.getCode(), ResultCodeAndMessage.TASK_MSG_TASK_TARGET_REDIS_ADDRESS_NULL.getMsg()));
            }
        }else {
            if (StringUtils.isEmpty(taskModel.getTargetKafkaAddress())) {
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASK_TARGET_KAFKA_ADDRESS_NULL.getCode(), ResultCodeAndMessage.TASK_MSG_TASK_TARGET_KAFKA_ADDRESS_NULL.getMsg()));
            }
        }
        toNext(client, taskModel);
    }

    @Override
    public void toNext(RedisClient client, TaskModel taskModel) throws Exception {
        if(null!=next) {
            next.run(client,taskModel);
        }
    }

    @Override
    public void setNext(ITaskCheckStrategy nextStrategy) {
        this.next=nextStrategy;
    }
}
