package syncer.syncerservice.filter.redis_start_check_strategy;

import org.springframework.util.StringUtils;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.RedisStartCheckEntity;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/4
 */
public class RedisStartTaskTypeStrategy implements IRedisStartCheckBaseStrategy {
    private IRedisStartCheckBaseStrategy next;
    private JDRedisClient client;
    private RedisStartCheckEntity eventEntity;
    private RedisPoolProps redisPoolProps;

    public RedisStartTaskTypeStrategy(IRedisStartCheckBaseStrategy next, JDRedisClient client, RedisStartCheckEntity eventEntity, RedisPoolProps redisPoolProps) {
        this.next = next;
        this.client = client;
        this.eventEntity = eventEntity;
        this.redisPoolProps = redisPoolProps;
    }

    @Override
    public void run(JDRedisClient client, RedisStartCheckEntity eventEntity, RedisPoolProps redisPoolProps) throws Exception {

        //初始化数据
        RedisStartCheckTypeEnum type = eventEntity.getStartCheckType();

        if (type.equals(RedisStartCheckTypeEnum.SINGLE_REDIS_TO_SINGLE_REDIS)
                || type.equals(RedisStartCheckTypeEnum.SINGLE_REDIS_TO_CLUSTER)
        ||type.equals(RedisStartCheckTypeEnum.SINGLE_REDIS_TO_FILE)) {

            RedisClusterDto redisClusterDto = eventEntity.getClusterDto();

            if ("incrementonly".equals(redisClusterDto.getTasktype().trim().toLowerCase())) {
                String incrementtype = redisClusterDto.getOffsetPlace().trim().toLowerCase();
                if (StringUtils.isEmpty(incrementtype)) {
                    incrementtype = "endbuffer";
                    redisClusterDto.setOffsetPlace(incrementtype);
                }

                if (!"endbuffer".equals(incrementtype) && !"beginbuffer".equals(incrementtype)) {
                    throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_INCREMENT_ERROR_CODE, TaskMsgConstant.TASK_MSG_INCREMENT_ERROR));
                }

            }

            redisClusterDto.setFileType(FileType.SYNC);

        }


    }

    @Override
    public void toNext(JDRedisClient client, RedisStartCheckEntity eventEntity, RedisPoolProps redisPoolProps) throws Exception {
        if(null!=next) {
            next.run(client,eventEntity,redisPoolProps);
        }
    }

    @Override
    public void setNext(IRedisStartCheckBaseStrategy nextStrategy) {
        this.next=nextStrategy;
    }

}
