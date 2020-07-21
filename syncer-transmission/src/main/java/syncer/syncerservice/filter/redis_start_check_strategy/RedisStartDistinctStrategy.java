package syncer.syncerservice.filter.redis_start_check_strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.util.StringUtils;
import syncer.syncerjedis.exceptions.JedisDataException;
import syncer.syncerpluscommon.constant.ResultCodeAndMessage;
import syncer.syncerpluscommon.util.md5.MD5Utils;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.exception.TaskRestoreException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.pool.RedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static syncer.syncerjedis.Protocol.Command.AUTH;

/**
 * @author zhanenqiang
 * @Description 判断任务是否重复
 * @Date 2020/3/3
 */
@AllArgsConstructor
@Builder
public class RedisStartDistinctStrategy implements IRedisStartCheckBaseStrategy {
    private IRedisStartCheckBaseStrategy next;
    private JDRedisClient client;
    private TaskModel taskModel;
    private RedisPoolProps redisPoolProps;


    @Override
    public void run(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) throws Exception {


        TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
        List<TaskModel> taskModelList=taskMapper.findTaskBytaskMd5(taskModel.getMd5());
        if(taskModelList!=null&&taskModelList.size()>0){
            throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASKSETTING_ERROR.getCode(),ResultCodeAndMessage.TASK_MSG_TASKSETTING_ERROR.getMsg()));
        }
        //下一节点
        toNext(client,taskModel,redisPoolProps);


    }

    @Override
    public void toNext(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) throws Exception {
        if(null!=next) {
            next.run(client,taskModel,redisPoolProps);
        }
    }

    @Override
    public void setNext(IRedisStartCheckBaseStrategy nextStrategy) {
        this.next=nextStrategy;
    }






}
