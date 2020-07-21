package syncer.syncerservice.filter.redis_start_check_strategy;
import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.syncerjedis.Jedis;
import syncer.syncerpluscommon.constant.ResultCodeAndMessage;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.dao.RdbVersionMapper;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.RdbVersionModel;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.jedis.TestJedisClient;
import java.net.URISyntaxException;

/**
 * @author zhanenqiang
 * @Description Redis版本获取节点
 * @Date 2020/2/27
 */
@Builder
@Slf4j
public class RedisStartSelectVersionStrategy implements IRedisStartCheckBaseStrategy{
    private IRedisStartCheckBaseStrategy next;
    private JDRedisClient client;
    private RedisPoolProps redisPoolProps;
    private TaskModel taskModel;

    @Override
    public void run(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) throws Exception {
        if(taskModel.getSyncType().equals(SyncType.SYNC.getCode())
                ||taskModel.getSyncType().equals(SyncType.RDB.getCode())
                ||taskModel.getSyncType().equals(SyncType.AOF.getCode())
                ||taskModel.getSyncType().equals(SyncType.MIXED.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINERDB.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINEAOF.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINEMIXED.getCode())){


            String version=selectSyncerVersion(String.valueOf(taskModel.getTargetUri().toArray()[0]));

            log.warn("自动获取redis版本号：{},手动输入版本号：{}",version,taskModel.getRedisVersion());

            if(version.equalsIgnoreCase("0.0")){
                if(taskModel.getRedisVersion()==0){
                    // throw new TaskMsgException("targetRedisVersion can not be empty /targetRedisVersion error");
                    throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_REDIS_MSG_ERROR.getCode(),ResultCodeAndMessage.TASK_MSG_REDIS_MSG_ERROR.getMsg()));
                }else {
                    version= String.valueOf(taskModel.getRedisVersion());
                }
            }

            if(version.indexOf("jimdb")>=0){
                taskModel.setRedisVersion(2.8);
            }else {
                taskModel.setRedisVersion(Double.valueOf(version));
            }

            RdbVersionMapper rdbVersionMapper= SpringUtil.getBean(RdbVersionMapper.class);
            RdbVersionModel rdbVersion=rdbVersionMapper.findRdbVersionModelByRedisVersion(version);
            if(rdbVersion==null){
                rdbVersion=RdbVersionModel.builder()
                        .rdb_version(TaskDataManagerUtils.getRdbVersionMap().get(version))
                        .redis_version(version)
                         .build();
            }

            taskModel.setRdbVersion(rdbVersion.getRdb_version());

        }

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
