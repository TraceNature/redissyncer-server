package syncer.syncerservice.filter.redis_start_check_strategy;
import lombok.Builder;
import org.springframework.util.StringUtils;
import syncer.syncerjedis.Jedis;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.RedisStartCheckEntity;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.TaskCheckUtils;
import syncer.syncerservice.util.jedis.TestJedisClient;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description Redis版本获取节点
 * @Date 2020/2/27
 */
@Builder
public class RedisStartSelectVersionStrategy implements IRedisStartCheckBaseStrategy{
    private IRedisStartCheckBaseStrategy next;
    private JDRedisClient client;
    private RedisStartCheckEntity eventEntity;
    private RedisPoolProps redisPoolProps;

    public RedisStartSelectVersionStrategy(IRedisStartCheckBaseStrategy next, JDRedisClient client, RedisStartCheckEntity eventEntity, RedisPoolProps redisPoolProps) {
        this.next = next;
        this.client = client;
        this.eventEntity = eventEntity;
        this.redisPoolProps = redisPoolProps;
    }

    @Override
    public void run(JDRedisClient client, RedisStartCheckEntity eventEntity, RedisPoolProps redisPoolProps) throws Exception {
        RedisStartCheckTypeEnum type=eventEntity.getStartCheckType();

//        if(type.equals(RedisStartCheckTypeEnum.SINGLE_TO_SINGLE)
//                ||type.equals(RedisStartCheckTypeEnum.SINGLE_TO_CLUSTER)
//                ||type.equals(RedisStartCheckTypeEnum.FILE_TO_CLUSTER)
//                ||type.equals(RedisStartCheckTypeEnum.FILE_TO_SINGLE)){
//            RedisClusterDto redisClusterDto= eventEntity.getClusterDto();
//            Set<String> targetUris= redisClusterDto.getTargetUris();
//            if(null==targetUris||targetUris.size()==0){
//                throw new FilterNodeException("目标路径不存在或targetUri解析错误");
//            }
//
//            String targetUri= (String) targetUris.toArray()[0];
//
//            try {
//                String version=selectSyncerVersion(targetUri);
//                redisClusterDto.setTargetRedisVersion(version);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            } catch (TaskMsgException e) {
//                e.printStackTrace();
//            }
//
//        }



        toNext(client,eventEntity,redisPoolProps);
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


    /**
     * 获取redis版本号
     * info信息中若无版本号信息则返回0L
     * @param targetUri
     * @return
     * @throws URISyntaxException
     */
    public  String selectSyncerVersion( String targetUri) throws URISyntaxException, TaskMsgException {
        RedisURI targetUriplus = new RedisURI(targetUri);
        /**
         * 源目标
         */
        Jedis target = null;
        String targetVersion = null;
        try {
            target = new Jedis(targetUriplus.getHost(), targetUriplus.getPort());
            Configuration targetConfig = Configuration.valueOf(targetUriplus);

            //获取password
            if (!StringUtils.isEmpty(targetConfig.getAuthPassword())) {
                Object targetAuth = target.auth(targetConfig.getAuthPassword());
            }
            String info=target.info();

            targetVersion = TestJedisClient.getRedisVersionString(info);

        } catch (Exception e) {
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,e.getMessage()));

        } finally {
            if (target != null) {
                target.close();
            }
        }
        return targetVersion;
    }



}
