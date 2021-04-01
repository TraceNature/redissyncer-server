package syncer.transmission.strategy.taskcheck.impl;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.type.SyncType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.util.redis.RedisUrlCheck;
import syncer.transmission.util.redis.RedisVersionUtil;

import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description url心跳检查策略
 * @Date 2020/12/14
 */
@AllArgsConstructor
@Builder
@Slf4j
public class TaskCheckRedisUrlStrategy implements ITaskCheckStrategy {
    private ITaskCheckStrategy next;
    private RedisClient client;
    private TaskModel taskModel;
    @Builder.Default
    RedisUrlCheck redisUrlCheck=new RedisUrlCheck();



    @Override
    public void run(RedisClient client, TaskModel taskModel) throws Exception {
        if(Objects.isNull(redisUrlCheck)){
            redisUrlCheck=new RedisUrlCheck();
        }
        //两边都需要进行检测
        if(taskModel.getSyncType().equals(SyncType.SYNC.getCode())){
            redisUrlCheck.checkRedisUrl(taskModel.getSourceUri(), "sourceUri:" + taskModel.getSourceHost());
            for (String targetUri : taskModel.getTargetUri()) {
                redisUrlCheck.checkRedisUrl(targetUri, "targetUri:" + taskModel.getTargetHost());
            }
            //只校验目标
        }else if(taskModel.getSyncType().equals(SyncType.RDB.getCode())
                ||taskModel.getSyncType().equals(SyncType.AOF.getCode())
                ||taskModel.getSyncType().equals(SyncType.MIXED.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINERDB.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINEAOF.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINEMIXED.getCode())){
            for (String targetUri : taskModel.getTargetUri()) {
                redisUrlCheck.checkRedisUrl(targetUri, "sourcehost"+taskModel.getSourceHost()+"targetUri:" + taskModel.getTargetHost());
            }
        }else if(taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())){
            //只校验源
            redisUrlCheck.checkRedisUrl(taskModel.getSourceUri(), "sourceUri:" + taskModel.getSourceHost());
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
