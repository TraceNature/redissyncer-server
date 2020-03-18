package syncer.syncerservice.service.impl2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.RedisStartCheckEntity;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.thread.OffSetEntity;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TaskErrorUtils;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerservice.service.IRedisTaskService;
import syncer.syncerservice.util.SyncTaskUtils;
import syncer.syncerservice.util.TaskCheckUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/25
 */
@Slf4j
@Service("singleRedisService")
public class SingleRedisServiceImpl implements IRedisTaskService {
    @Autowired
    RedisPoolProps redisPoolProps;

    @Override
    public String runSyncerTask(RedisStartCheckEntity redisStartCheckEntity, RedisStartCheckTypeEnum redisStartCheckType) throws TaskMsgException {

        return null;
    }

    /**
     * @param taskModel
     * @return
     * @throws TaskMsgException
     */
    @Override
        public String runSyncerTask(TaskModel taskModel) throws Exception {

        if(StringUtils.isEmpty(taskModel.getTaskName())){
            taskModel.setTaskName(taskModel.getId()+"【"+taskModel.getSourceRedisAddress()+"节点】");
        }

        TaskDataEntity dataEntity=TaskDataEntity.builder()
                .taskModel(taskModel)
                .offSetEntity(OffSetEntity.builder().build())
                .build();

        try {
            TaskDataManagerUtils.addAliveThread(taskModel.getId(),dataEntity);
        }catch (Exception e){
            TaskErrorUtils.updateStatusAndLog(e,this.getClass(),taskModel.getId(),dataEntity);
        }



        if(taskModel.isAutostart()){



        }



        if(dto.isAutostart()){
            try{
                redisBatchedSyncerService.batchedSync(dto,threadId,dto.isAfresh());
                msgEntity.setStatus(ThreadStatusEnum.RUN);
            }catch (Exception e){
                msgEntity.setStatus(ThreadStatusEnum.BROKEN);
                log.warn("任务Id【{}】任务启动失败 ，失败原因【{}】", threadId, e.getMessage());
                e.printStackTrace();
            }

        }else {
            msgEntity.getRedisClusterDto().setAfresh(true);
        }

        return threadId;
        return null;
    }
}
