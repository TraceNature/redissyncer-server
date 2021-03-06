package syncer.transmission.strategy.taskcheck.impl;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.constant.RedisType;
import syncer.replica.type.SyncType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.util.redis.RedisUrlCheck;

import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description url心跳检查策略
 * @Date 2020/12/14
 */
@AllArgsConstructor
@Builder
@Slf4j
public class TaskCheckRedisUrlStrategy1 implements ITaskCheckStrategy {
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
            if(RedisType.SENTINEL.getCode().equals(taskModel.getSourceRedisType())){

                redisUrlCheck.checkRedisUrl(taskModel.getSourceHostUris(), "sourceUri:" + JSON.toJSONString(taskModel.getSourceHostUris()));
            }else {
                redisUrlCheck.checkRedisUrl(taskModel.getSourceUri(), "sourceUri:" + taskModel.getSourceHost());
            }

            if(RedisType.SENTINEL.getCode().equals(taskModel.getTargetRedisType())){
                redisUrlCheck.checkRedisUrl(taskModel.getTargetHostUris(), "targetUri:" + JSON.toJSONString(taskModel.getTargetHostUris()));
            }else{
                for (String targetUri : taskModel.getTargetUri()) {
                    redisUrlCheck.checkRedisUrl(targetUri, "targetUri:" + taskModel.getTargetHost());
                }
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
            if(RedisType.SENTINEL.getCode().equals(taskModel.getSourceRedisType())){
                //只校验源
                redisUrlCheck.checkRedisUrl(taskModel.getSourceHostUris(), "sourceUri:" + JSON.toJSONString(taskModel.getSourceHostUris()));
            }else {
                //只校验源
                redisUrlCheck.checkRedisUrl(taskModel.getSourceUri(), "sourceUri:" + taskModel.getSourceHost());
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
