package syncer.syncerservice.service.impl2;

import org.springframework.beans.factory.annotation.Autowired;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.constant.TargetAndSourceRedisType;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.service.IRedisSyncerService;
import syncer.syncerservice.service.IRedisTaskService;
import syncer.syncerservice.service.ISyncerService;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/14
 */
public class TaskGroupServiceImpl implements ISyncerService {

    @Autowired
    RedisPoolProps redisPoolProps;
    @Autowired
    IRedisSyncerService redisBatchedSyncerService;


    /**
     * 单机Redis-->单机Redis数据服务
     */
    @Autowired
    IRedisTaskService singleRedisService;




    @Override
    public ResultMap createFileToRedisTask(List<TaskModel> taskModelList) throws TaskMsgException {
        return null;
    }

    @Override
    public ResultMap createCommandDumpUptask(List<TaskModel> taskModelList) throws TaskMsgException {
        return null;
    }

    @Override
    public ResultMap createRedisToRedisTask(List<TaskModel> taskModelList) throws TaskMsgException {

        String groupId=null;
        if(taskModelList!=null&&taskModelList.size()>0){
            taskModelList.forEach(taskModel->{
                if(taskModel.getTargetRedisType().equals(TargetAndSourceRedisType.Single.getCode())){
                    //目标为单机

                }else if(taskModel.getTargetRedisType().equals(TargetAndSourceRedisType.Cluster.getCode())){
                    //目标为集群

                }
            });
        }


        return null;
    }
}
