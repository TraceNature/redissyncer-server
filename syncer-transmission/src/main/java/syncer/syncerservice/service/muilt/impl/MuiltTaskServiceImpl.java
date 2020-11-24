package syncer.syncerservice.service.muilt.impl;

import org.springframework.stereotype.Service;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.ThreadPoolUtils;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.muli.multisync.MultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.util.MultiSyncTaskManagerutils;
import syncer.syncerservice.MultiMasterReplication.multiSync.RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask;
import syncer.syncerservice.po.FlushCommandStatus;
import syncer.syncerservice.service.muilt.MuiltTaskService;
import syncer.syncerservice.util.circle.MultiSyncCircle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/10/14
 */
@Service
public class MuiltTaskServiceImpl implements MuiltTaskService {
    /**
     * 创建任务
     * @param parentMultiTaskModel
     * @return
     */
    @Override
    public ResultMap createMuilTask(ParentMultiTaskModel parentMultiTaskModel) {
//        MulitSyncTaskManagerutils.addTask(parentMultiTaskModel);

//        MulitSyncTaskManagerutils.addTask(parentMultiTaskModel);


        String parentAId=parentMultiTaskModel.getRedisNodeA().get(0).getParentId();
        String parentBId=parentMultiTaskModel.getRedisNodeB().get(0).getParentId();

        MultiSyncCircle circle=getCircle(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB());

//        new Thread(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(targetmultiTaskModel,"222",circle)).start();
        MultiSyncTaskManagerutils.addTask(parentMultiTaskModel);

        if(parentMultiTaskModel.getAutostart()==1){
            parentMultiTaskModel.getRedisNodeA().forEach(node->{
                node.setStatus(TaskStatusType.CREATING.getCode());
                ThreadPoolUtils.exec(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(node,parentAId,parentBId,circle));
            });
            parentMultiTaskModel.getRedisNodeB().forEach(node->{
                ThreadPoolUtils.exec(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(node,parentBId,parentAId,circle));

            });
        }else {
            Stream.of(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB())
                    .flatMap(Collection::stream).forEach(node->{
                        node.setStatus(TaskStatusType.STOP.getCode());
            });


        }

        List<String>list=Stream.of(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB())
                .flatMap(Collection::stream)
                .map(multiTaskModel -> {
                    return multiTaskModel.getNodeId();
                }).collect(Collectors.toList());
        Map<String,Object>data=new HashMap<>();
        data.put("taskId",parentMultiTaskModel.getTaskId());
        data.put("nodeIds",list);
        return ResultMap.builder().code("2000").msg("success").data(data);
    }

    @Override
    public ResultMap startMuilTask(String parentId) {
        return null;
    }


    /**
     * 停止双向任务（支持多个）
     * @param taskIdList
     * @return
     */
    @Override
    public ResultMap stopMuilTask(List<String> taskIdList) {
        try {
            Map<String,String>stopData=MultiSyncTaskManagerutils.stopTaskList(taskIdList);
            return ResultMap.builder().code("2000").data(stopData).msg("success");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResultMap.builder().code("1000").msg("Operation failed");
    }


    /**
     * 启动双向数据同步任务
     * @param taskId
     * @param afresh
     * @return
     * @throws Exception
     */
    @Override
    public ResultMap startTaskByTaskId(String taskId, boolean afresh) throws Exception {
        Integer status=MultiSyncTaskManagerutils.getTaskStatusByTaskId(taskId);
        if(!status.equals(TaskStatusType.STOP.getCode())&&!status.equals(TaskStatusType.BROKEN.getCode())){
            return ResultMap.builder().code("1000").msg("任务不在STOP或BROKEN状态");
        }

            ParentMultiTaskModel parentMultiTaskModel= MultiSyncTaskManagerutils.getAliveMulitSyncHashMap().get(taskId);



        String parentAId=parentMultiTaskModel.getRedisNodeA().get(0).getParentId();
        String parentBId=parentMultiTaskModel.getRedisNodeB().get(0).getParentId();

        MultiSyncCircle circle=getCircle(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB());

//        parentMultiTaskModel.getRedisNodeA().stream().forEach(node->{
//            node.setStatus(TaskStatusType.RUN.getCode());
//            ThreadPoolUtils.exec(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(node,parentBId,parentAId,circle));
//
//        });
        parentMultiTaskModel.getRedisNodeA().forEach(node->{
            node.setStatus(TaskStatusType.RUN.getCode());
            ThreadPoolUtils.exec(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(node,parentAId,parentBId,circle));
        });
        parentMultiTaskModel.getRedisNodeB().forEach(node->{
            ThreadPoolUtils.exec(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(node,parentBId,parentAId,circle));

        });

//        Stream.of(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB())
//                .flatMap(Collection::stream).forEach(node->{
//            node.setStatus(TaskStatusType.RUN.getCode());
//            ThreadPoolUtils.exec(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(node,parentBId,parentAId,circle));
//
//        });

        return ResultMap.builder().code("2000").msg("start task success");
    }


    /**
     * 构造破环类
     * @param redisNodeA
     * @param redisNodeB
     * @return
     */
    MultiSyncCircle getCircle(List<MultiTaskModel> redisNodeA,List<MultiTaskModel> redisNodeB){
        Map<String, Map<String, AtomicLong>>nodeGroupData=new ConcurrentHashMap<>();
        String parentAId=redisNodeA.get(0).getParentId();
        String parentBId=redisNodeB.get(0).getParentId();
        Long nodeLongCount=Stream.of(redisNodeA,redisNodeB).flatMap(Collection::stream).count();
        int nodeCount=nodeLongCount.intValue();
        nodeGroupData.put(parentAId,new ConcurrentHashMap<String, AtomicLong>());
        nodeGroupData.put(parentBId,new ConcurrentHashMap<String, AtomicLong>());

        Map<String, FlushCommandStatus>flushCommandStatus=new ConcurrentHashMap<>();
        flushCommandStatus.put(parentAId, FlushCommandStatus.builder().type(-1).num(new AtomicInteger(0)).db(-1).status(new AtomicBoolean(false)).build());
        flushCommandStatus.put(parentBId,FlushCommandStatus.builder().type(-1).num(new AtomicInteger(0)).db(-1).status(new AtomicBoolean(false)).build());

        Map<String, AtomicInteger>dbData=new ConcurrentHashMap<>();
        dbData.put(parentAId,new AtomicInteger(0));
        dbData.put(parentBId,new AtomicInteger(0));

        MultiSyncCircle circle=MultiSyncCircle.builder().nodeGroupData(nodeGroupData)
                .dbData(dbData)
                .flushCommandStatus(flushCommandStatus)
                .nodeCount(nodeCount)
                .nodeSuccessStatusType(new AtomicInteger(0))
                .nodeStatus(new AtomicInteger(0))
                .nodeSuccessStatus(new AtomicBoolean(true))
                .build();

        return circle;
    }
}
