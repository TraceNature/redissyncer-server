package syncer.transmission.strategy.taskcheck.impl;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import syncer.common.constant.ResultCodeAndMessage;
import syncer.common.exception.TaskMsgException;
import syncer.replica.entity.SyncType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.RdbVersionModel;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.util.code.CodeUtils;
import syncer.transmission.util.redis.RedisVersionUtil;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description Redis版本获取节点
 * @Date 2020/12/14
 */

@Builder
@Slf4j
public class TaskSelectVersionStrategy implements ITaskCheckStrategy {

    private ITaskCheckStrategy next;
    private RedisClient client;
    private TaskModel taskModel;
    @Builder.Default
    private RedisVersionUtil redisVersionUtil=new RedisVersionUtil();
    /**
     * jimdb版本头
     */
    private final static String JIMDB_VERSION_HEADER="jimdb";
    /**
     * jimdb默认RDB版本
     */
    private final static double JIMDB_DEFAULT_VERSION=2.8;
    /**
     *
     */
    private final static String DEFAULT_NO_VERSION="0.0";

    @Override
    public void run(RedisClient client, TaskModel taskModel) throws Exception {
        if(Objects.isNull(redisVersionUtil)){
            redisVersionUtil=new RedisVersionUtil();
        }
        if(taskModel.getSyncType().equals(SyncType.SYNC.getCode())
                ||taskModel.getSyncType().equals(SyncType.RDB.getCode())
                ||taskModel.getSyncType().equals(SyncType.AOF.getCode())
                ||taskModel.getSyncType().equals(SyncType.MIXED.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINERDB.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINEAOF.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINEMIXED.getCode())){
            String version=redisVersionUtil.selectSyncerVersion(String.valueOf(taskModel.getTargetUri().toArray()[0]));
            log.warn("自动获取redis版本号：{},手动输入版本号：{}",version,taskModel.getRedisVersion());
            if(DEFAULT_NO_VERSION.equalsIgnoreCase(version)){
                if(taskModel.getRedisVersion()==0){
                    // targetRedisVersion can not be empty /targetRedisVersion error
                    throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_REDIS_MSG_ERROR.getCode(),ResultCodeAndMessage.TASK_MSG_REDIS_MSG_ERROR.getMsg()));
                }else {
                    version= String.valueOf(taskModel.getRedisVersion());
                }
            }
            if(version.contains(JIMDB_VERSION_HEADER)){
                taskModel.setRedisVersion(JIMDB_DEFAULT_VERSION);
            }else {
                taskModel.setRedisVersion(Double.valueOf(version));
            }
            RdbVersionModel rdbVersion= SqlOPUtils.findRdbVersionModelByRedisVersion(version);
            if(Objects.isNull(rdbVersion)){
                rdbVersion=RdbVersionModel.builder()
                        .rdb_version(SingleTaskDataManagerUtils.getRDB_VERSION_MAP().get(version))
                        .redis_version(version)
                        .build();
            }
            taskModel.setRdbVersion(rdbVersion.getRdb_version());
        }

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
