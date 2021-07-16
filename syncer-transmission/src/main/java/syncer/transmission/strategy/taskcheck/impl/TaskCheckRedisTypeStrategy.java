package syncer.transmission.strategy.taskcheck.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import syncer.common.constant.ResultCodeAndMessage;
import syncer.common.exception.TaskMsgException;
import syncer.replica.constant.RedisType;
import syncer.replica.type.SyncType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.util.code.CodeUtils;
import syncer.transmission.util.strings.StringUtils;

/**
 * @author zhanenqiang
 * @Description 判断RedisType是否存在
 * @Date 2020/12/14
 */
@AllArgsConstructor
@Builder

public class TaskCheckRedisTypeStrategy implements ITaskCheckStrategy {
    private ITaskCheckStrategy next;
    private RedisClient client;
    private TaskModel taskModel;

    @Override
    public void run(RedisClient client, TaskModel taskModel) throws Exception {
        //SYNC
        if(SyncType.SYNC.getCode().equals(taskModel.getSyncType())){
            if(RedisType.NONE.getCode().equals(taskModel.getSourceRedisType())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASK_SOURCE_REDIS_TYPE_NULL.getCode(),ResultCodeAndMessage.TASK_MSG_TASK_SOURCE_REDIS_TYPE_NULL.getMsg()));
            }
        }

        //非COMMANDDUMPUP
        if(!SyncType.COMMANDDUMPUP.getCode().equals(taskModel.getSyncType())){
            if(RedisType.NONE.getCode().equals(taskModel.getTargetRedisType())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASK_TARGET_REDIS_TYPE_NULL.getCode(),ResultCodeAndMessage.TASK_MSG_TASK_TARGET_REDIS_TYPE_NULL.getMsg()));
            }
        }

        if(RedisType.SENTINEL.getCode().equals(taskModel.getSourceRedisType())){
            if(StringUtils.isEmpty(taskModel.getSourceRedisMasterName())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASK_SOURCE_MASTER_REDIS_NAME_NULL.getCode(),ResultCodeAndMessage.TASK_MSG_TASK_SOURCE_MASTER_REDIS_NAME_NULL.getMsg()));
            }
        }

        if(RedisType.SENTINEL.getCode().equals(taskModel.getTargetRedisType())){
            if(StringUtils.isEmpty(taskModel.getSourceRedisMasterName())){
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASK_TARGET_MASTER_REDIS_NAME_NULL.getCode(),ResultCodeAndMessage.TASK_MSG_TASK_TARGET_MASTER_REDIS_NAME_NULL.getMsg()));
            }
        }


        //下一节点
        toNext(client,taskModel);
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
