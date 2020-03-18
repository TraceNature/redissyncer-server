package syncer.syncerservice.sync;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.constant.TargetAndSourceRedisType;
import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.entity.thread.OffSetEntity;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.SyncTypeUtils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerservice.compensator.ISyncerCompensator;
import syncer.syncerservice.compensator.ISyncerCompensatorFactory;
import syncer.syncerservice.filter.CommonFilter;
import syncer.syncerservice.filter.KeyValueRunFilterChain;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisClientFactory;
import syncer.syncerservice.util.JDRedisClient.RedisMigrator;
import syncer.syncerservice.util.RedisUrlCheckUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/16
 */
@Slf4j
@AllArgsConstructor
public class RedisDataSyncTransmissionTask implements Runnable{
    private TaskModel taskModel;
    /**
     * 目标Redis类型
  */
    private TargetAndSourceRedisType redisType;

    @Override
    public void run() {

        //这一步应交给上层去做
        if(null==taskModel.getBatchSize()||taskModel.getBatchSize()==0){
            taskModel.setBatchSize(1500);
        }

        try {
            Replicator replicator = null;
            //replication
            if (taskModel.getSyncType().equals(SyncType.SYNC.getCode())) {
                RedisURI suri = new RedisURI(taskModel.getSourceUri());
                replicator = new JDRedisReplicator(suri, taskModel.isAfresh());
            } else {
                //文件
                replicator = new JDRedisReplicator(null, SyncTypeUtils.getSyncType(taskModel.getSyncType()).getFileType(), taskModel.getFileAddress(), Configuration.defaultSetting(), taskModel.getId());
            }
            //注册增量命令解析器
            final Replicator replicationHandler = RedisMigrator.newBacthedCommandDress(replicator);
            //注册RDB全量解析器
            replicationHandler.setRdbVisitor(new ValueDumpIterableRdbVisitor(replicationHandler, taskModel.getRdbVersion()));

            OffSetEntity offset = TaskDataManagerUtils.get(taskModel.getId()).getOffSetEntity();
            if (offset == null) {
                offset = new OffSetEntity();
                TaskDataManagerUtils.get(taskModel.getId()).setOffSetEntity(offset);
            } else {
                if (StringUtils.isEmpty(offset.getReplId())) {
                    offset.setReplId(replicationHandler.getConfiguration().getReplId());
                } else if (offset.getReplOffset().get() > -1) {
                    if (!taskModel.isAfresh()) {
                        replicationHandler.getConfiguration().setReplOffset(offset.getReplOffset().get());
                        replicationHandler.getConfiguration().setReplId(offset.getReplId());
                    }

                }
            }


            //只增量相关代码
            if (SyncTypeUtils.getTaskType(taskModel.getTasktype()).getType().equals(TaskRunTypeEnum.INCREMENTONLY)) {

                String[] data = RedisUrlCheckUtils.selectSyncerBuffer(taskModel.getSourceUri(), SyncTypeUtils.getOffsetPlace(taskModel.getOffsetPlace()).getOffsetPlace());

                long offsetNum = 0L;
                try {
                    offsetNum = Long.parseLong(data[0]);
                    offsetNum -= 1;
                    //offsetNum -= 1;
                } catch (Exception e) {

                }
                if (offsetNum != 0L && !StringUtils.isEmpty(data[1])) {
                    replicationHandler.getConfiguration().setReplOffset(offsetNum);
                    replicationHandler.getConfiguration().setReplId(data[1]);
                }
            }





            List<CommonFilter> commonFilterList = new ArrayList<>();
//            JDRedisClient client = JDRedisClientFactory.createJDRedisClient(SyncTypeUtils.getRedisBranchType(taskModel.getSourceRedisType()), syncDataDto.getTargetHost(), syncDataDto.getTargetPort(), syncDataDto.getTargetPassword(), batchSize, taskId);
//
//            //根据type生成相对节点List [List顺序即为filter节点执行顺序]
//            assemble_the_list(commonFilterList, type, taskId, syncDataDto, client);
//
//            ISyncerCompensator syncerCompensator= ISyncerCompensatorFactory.createJDRedisClient(branchTypeEnum,taskId,client);

//            SendCommandWithOutQueue sendCommandWithOutQueue=SendCommandWithOutQueue.builder()
//                    .filterChain(KeyValueRunFilterChain.builder().commonFilterList(commonFilterList).build())
//                    .r(replicationHandler)
//                    .taskId(taskModel.getId())
//                    .syncerCompensator(syncerCompensator)
//                    .build();
//



        }catch (Exception e){

        }
    }
}
