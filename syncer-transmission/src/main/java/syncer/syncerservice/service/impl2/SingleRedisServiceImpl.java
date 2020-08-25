package syncer.syncerservice.service.impl2;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import syncer.syncerplusredis.constant.*;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.RedisStartCheckEntity;
import syncer.syncerplusredis.entity.StartTaskEntity;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.entity.thread.OffSetEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.ExpandTaskModel;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.ExpandTaskUtils;
import syncer.syncerplusredis.util.SyncTypeUtils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TaskErrorUtils;
import syncer.syncerservice.filter.redis_start_check_strategy.RedisTaskStrategyGroupSelecter;
import syncer.syncerservice.filter.strategy_type.RedisTaskStrategyGroupType;
import syncer.syncerservice.service.IRedisTaskService;
import syncer.syncerservice.sync.RedisDataCommandUpTransmissionTask;
import syncer.syncerservice.sync.RedisDataSyncTransmissionTask;
import syncer.syncerservice.util.RedisUrlCheckUtils;


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

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Override
    public String runSyncerTask(RedisStartCheckEntity redisStartCheckEntity, RedisStartCheckTypeEnum redisStartCheckType) throws TaskMsgException {

        return null;
    }

    /**
     * 运行之前已经创建过任务
     * @param taskModel
     * @return
     * @throws TaskMsgException
     */
    @Override
    public String runSyncerTask(TaskModel taskModel) throws Exception {

        if(StringUtils.isEmpty(taskModel.getTaskName())){
            if(taskModel.getSourceRedisType()==3){
                taskModel.setTaskName(taskModel.getId()+"【"+taskModel.getFileAddress()+"数据文件】");
            }else{
                taskModel.setTaskName(taskModel.getId()+"【"+taskModel.getSourceRedisAddress()+"节点】");
            }
        }

        TaskDataEntity dataEntity=null;
        if(taskModel.getSyncType().equals(SyncType.SYNC.getCode())||taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())){
           //获取offset和服务id
            String[] data = RedisUrlCheckUtils.selectSyncerBuffer(taskModel.getSourceUri(), SyncTypeUtils.getOffsetPlace(taskModel.getOffsetPlace()).getOffsetPlace());

            dataEntity=TaskDataEntity.builder()
                    .taskModel(taskModel)
                    .offSetEntity(OffSetEntity.builder().replId(data[1]).build())
                    .build();
            dataEntity.getOffSetEntity().getReplOffset().set(taskModel.getOffset());
        }else {

            dataEntity=TaskDataEntity.builder()
                    .taskModel(taskModel)
                    .offSetEntity(OffSetEntity.builder().replId("").build())
                    .build();
        }

        //初始化ExpandTaskModel
        ExpandTaskUtils.loadingExpandTaskData(taskModel,dataEntity);

        TaskDataManagerUtils.addMemThread(taskModel.getId(),dataEntity,true);
        //创建中
        TaskDataManagerUtils.changeThreadStatus(taskModel.getId(),taskModel.getOffset(), TaskStatusType.CREATING);
        try {
            //校验
            RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.NODISTINCT,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);

        }catch (Exception e){
            TaskDataManagerUtils.removeThread(taskModel.getId());
            throw e;
        }

        //创建完成
        TaskDataManagerUtils.changeThreadStatus(taskModel.getId(),taskModel.getOffset(), TaskStatusType.CREATED);
        try{
            threadPoolTaskExecutor.execute(new RedisDataSyncTransmissionTask(taskModel, true));
        }catch (Exception e){
            TaskErrorUtils.brokenStatusAndLog(e,this.getClass(),taskModel.getId());
        }
        return taskModel.getId();
    }




    /**
     * 创建任务
     * @param taskModel
     * @return
     * @throws Exception
     */
    @Override
    public String createSyncerTask(TaskModel taskModel) throws Exception {
        if(StringUtils.isEmpty(taskModel.getTaskName())){
            taskModel.setTaskName(taskModel.getId()+"【"+taskModel.getSourceRedisAddress()+"节点】");
        }

        TaskDataEntity dataEntity=TaskDataEntity.builder()
                .taskModel(taskModel)
                .offSetEntity(OffSetEntity.builder().build())
                .build();

        //首次创建完成后不自动启动
        try {
            TaskDataManagerUtils.addDbThread(taskModel.getId(),dataEntity.getTaskModel());
        }catch (Exception e){
            TaskErrorUtils.updateStatusAndLog(e,this.getClass(),taskModel.getId(),dataEntity);
        }

        if(taskModel.isAutostart()){
            //首次创建完成后自动启动
            runSyncerTask(taskModel);
        }

        return taskModel.getId();
    }

    @Override
    public String createCommandSyncerTask(TaskModel taskModel) throws Exception {
        if(StringUtils.isEmpty(taskModel.getTaskName())){
            taskModel.setTaskName(taskModel.getId()+"【"+taskModel.getSourceRedisAddress()+"节点】");
        }

        TaskDataEntity dataEntity=null;
        if(taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())){
            String[] data = RedisUrlCheckUtils.selectSyncerBuffer(taskModel.getSourceUri(), SyncTypeUtils.getOffsetPlace(taskModel.getOffsetPlace()).getOffsetPlace());

            dataEntity=TaskDataEntity.builder()
                    .taskModel(taskModel)
                    .offSetEntity(OffSetEntity.builder().replId(data[1]).build())
                    .build();
            dataEntity.getOffSetEntity().getReplOffset().set(taskModel.getOffset());
        }else {
            dataEntity=TaskDataEntity.builder()
                    .taskModel(taskModel)
                    .offSetEntity(OffSetEntity.builder().replId("").build())
                    .build();
        }

        dataEntity.getTaskModel().setStatus(TaskStatusType.CREATING.getCode());

        //初始化ExpandTaskModel
        ExpandTaskUtils.loadingExpandTaskData(taskModel,dataEntity);

        TaskDataManagerUtils.addMemThread(taskModel.getId(),dataEntity,true);

        //创建中
        TaskDataManagerUtils.changeThreadStatus(taskModel.getId(),-1L, TaskStatusType.CREATING);

        try {

            //校验
            RedisTaskStrategyGroupSelecter.select(RedisTaskStrategyGroupType.NODISTINCT,null,taskModel,redisPoolProps).run(null,taskModel,redisPoolProps);

        }catch (Exception e){
            TaskDataManagerUtils.removeThread(taskModel.getId());
            throw e;
        }


        //创建完成
        TaskDataManagerUtils.changeThreadStatus(taskModel.getId(),-1L, TaskStatusType.CREATED);

        try{
            threadPoolTaskExecutor.execute(new RedisDataCommandUpTransmissionTask(taskModel));
        }catch (Exception e){
            TaskErrorUtils.brokenStatusAndLog(e,this.getClass(),taskModel.getId());
        }


        return taskModel.getId();

    }




}
