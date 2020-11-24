package syncer.syncerplusredis.util;

import com.alibaba.fastjson.JSON;
import com.beust.jcommander.internal.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import syncer.syncerpluscommon.util.DeepBeanUtils;
import syncer.syncerpluscommon.util.taskType.SyncerTaskType;
import syncer.syncerplusredis.constant.TaskStatusConstant;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.StartTaskEntity;
import syncer.syncerplusredis.entity.muli.multisync.MultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltCreateTaskData;
import syncer.syncerplusredis.model.MultiTaskModelResult;
import syncer.syncerplusredis.model.ParentMultiTaskModelResult;
import syncer.syncerplusredis.model.TaskModelResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhanenqiang
 * @Description 双向同步任务管理
 * @Date 2020/9/21
 */
public class MultiSyncTaskManagerutils {
    /**
     * 双向同步任务
     */
    @Getter
    @Setter
    private static Map<String, ParentMultiTaskModel> aliveMulitSyncHashMap=new ConcurrentHashMap<String,ParentMultiTaskModel>();


    /**
     * 根据双向全局任务id在任务列表中获取子节点任务
     * @param globalTaskId
     * @return
     */
    public static MultiTaskModel get(String globalTaskId){
        List<String>ids= SyncerTaskType.globalTaskId2TaskId(globalTaskId);
        String taskId=ids.get(0);
        String nodeId=ids.get(2);
        return Stream.of(aliveMulitSyncHashMap.get(taskId).getRedisNodeA().stream(),aliveMulitSyncHashMap.get(taskId).getRedisNodeB().stream())
                .flatMap(nodeList->nodeList).filter(body->body.getNodeId().equals(nodeId)).findFirst().get();
    }

    /***
     * 在任务列表中获取子节点任务
     * @param taskId
     * @param boyId
     * @return
     */
    public static MultiTaskModel get(String taskId,String boyId){
        return Stream.of(aliveMulitSyncHashMap.get(taskId).getRedisNodeA().stream(),aliveMulitSyncHashMap.get(taskId).getRedisNodeB().stream())
                .flatMap(nodeList->nodeList).filter(body->body.getNodeId().equals(boyId)).findFirst().get();
    }


    /***
     * 新增任务
     * @param parentMultiTaskModel
     * @return
     */
    public static void addTask(ParentMultiTaskModel parentMultiTaskModel){
        aliveMulitSyncHashMap.put(parentMultiTaskModel.getTaskId(),parentMultiTaskModel);
    }


    /**
     * 设置任务状态
     * @param taskId
     * @param nodeId
     * @param taskStatusType
     */
    public static void setNodeStatusByTaskIdAndNodeId(String taskId, String nodeId, TaskStatusType taskStatusType){
        ParentMultiTaskModel parentMultiTaskModel=aliveMulitSyncHashMap.get(taskId);

        Stream.of(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB())
                .flatMap(Collection::stream).forEach(multiTaskModel -> {
            if(multiTaskModel.getNodeId().equals(nodeId)){
                multiTaskModel.setStatus(taskStatusType.getCode());
            }
        });

        /**
        for (MultiTaskModel multiTaskModel:parentMultiTaskModel.getRedisNodeA()) {
            if(multiTaskModel.getNodeId().equals(nodeId)){
                multiTaskModel.setStatus(taskStatusType.getCode());
            }
        }

        for (MultiTaskModel multiTaskModel:parentMultiTaskModel.getRedisNodeB()) {
            if(multiTaskModel.getNodeId().equals(nodeId)){
                multiTaskModel.setStatus(taskStatusType.getCode());
            }
        }
         **/
    }


    /**
     * 设置single全局任务状态
     * @param globalTaskId
     * @param taskStatusType
     */
    public static void setGlobalNodeStatus(String globalTaskId,String msg, TaskStatusType taskStatusType){
        List<String>ids= SyncerTaskType.globalTaskId2TaskId(globalTaskId);
        String taskId=ids.get(0);
        String nodeId=ids.get(2);
        ParentMultiTaskModel parentMultiTaskModel=aliveMulitSyncHashMap.get(taskId);

        for (MultiTaskModel multiTaskModel:parentMultiTaskModel.getRedisNodeA()) {
            if(multiTaskModel.getNodeId().equals(nodeId)){
                multiTaskModel.setStatus(taskStatusType.getCode());
            }
        }

        for (MultiTaskModel multiTaskModel:parentMultiTaskModel.getRedisNodeB()) {
            if(multiTaskModel.getNodeId().equals(nodeId)){
                multiTaskModel.setStatus(taskStatusType.getCode());
            }
        }

    }




    /**
     * 设置全局任务状态
     * @param taskId
     * @param taskStatusType
     */
    public static void setGlobalNodeStatusByTaskId(String taskId, TaskStatusType taskStatusType){

        ParentMultiTaskModel parentMultiTaskModel=aliveMulitSyncHashMap.get(taskId);
        Stream.of(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB())
                .flatMap(Collection::stream).forEach(multiTaskModel -> {
             multiTaskModel.setStatus(taskStatusType.getCode());
        });

    }


    public static void main1(String[] args) {

//经过测试，当元素个数小于24时，并行时线程数等于元素个数，当大于等于24时，并行时线程数为16
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24);

        Integer v = list.stream().reduce((x1, x2) -> x1 + x2).get();
        System.out.println(v);   // 300

        Integer v1 = list.stream().reduce(10, (x1, x2) -> x1 + x2);
        System.out.println(v1);  //310

        Integer v2 = list.stream().reduce(0,
                (x1, x2) -> {
                    System.out.println("stream accumulator: x1:" + x1 + "  x2:" + x2);
                    return x1 - x2;
                },
                (x1, x2) -> {
                    System.out.println("stream combiner: x1:" + x1 + "  x2:" + x2);
                    return x1 * x2;
                });
        System.out.println(v2); // -300

        Integer v3 = list.parallelStream().reduce(0,
                (x1, x2) -> {
                    System.out.println("parallelStream accumulator: x1:" + x1 + "  x2:" + x2);
                    return x1 - x2;
                },
                (x1, x2) -> {
                    System.out.println("parallelStream combiner: x1:" + x1 + "  x2:" + x2);
                    return  x1+x2;
//                    return x1 * x2;
                });
        System.out.println(v3);
    }



    /**
     * 任务是否关闭或者停止
     * @param taskId
     * @param taskId
     * @return
     */
    public static synchronized boolean isTaskClose(String taskId){
        if(aliveMulitSyncHashMap.containsKey(taskId)){

            ParentMultiTaskModel parentMultiTaskModel=aliveMulitSyncHashMap.get(taskId);

           List<MultiTaskModel> multiTaskModelList= Stream.of(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            for (MultiTaskModel node : multiTaskModelList) {
                if(node.getStatus().equals(TaskStatusType.BROKEN.getCode())||node.getStatus().equals(TaskStatusType.STOP.getCode())){
                    return true;
                }
            }


           /**
            for (MultiTaskModel nodeA : parentMultiTaskModel.getRedisNodeA()) {
                if(nodeA.getStatus().equals(TaskStatusType.BROKEN.getCode())||nodeA.getStatus().equals(TaskStatusType.STOP.getCode())){
                    return true;
                }
            }

            for (MultiTaskModel nodeB : parentMultiTaskModel.getRedisNodeB()) {
                if(nodeB.getStatus().equals(TaskStatusType.BROKEN.getCode())||nodeB.getStatus().equals(TaskStatusType.STOP.getCode())){
                    return true;
                }
            }

            **/
            return false;
        }
        return true;
    }


    /**
     * 根据globalTaskId获取全局任务状态
     *
     *  状态监测优先级：---> broken stop run rdbRunning aofRunning
     *
     *
     * @param globalTaskId
     * @return
     */
    public static Integer  getTaskStatus(String globalTaskId){
        List<String>ids= SyncerTaskType.globalTaskId2TaskId(globalTaskId);
        String taskId=ids.get(0);
        return getTaskStatusByTaskId(taskId);
    }

    /**
     * 根据taskId获取全局任务状态
     *
     *  状态监测优先级：---> broken stop run rdbRunning aofRunning
     *
     *
     * @param taskId
     * @return
     */
    public static Integer  getTaskStatusByTaskId(String taskId){
        ParentMultiTaskModel parentMultiTaskModel=aliveMulitSyncHashMap.get(taskId);

        List<MultiTaskModel>allTaskList=Stream.of(parentMultiTaskModel.getRedisNodeA(),parentMultiTaskModel.getRedisNodeB())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //BROKEN状态
        List<MultiTaskModel>brokenTask=allTaskList.stream()
                .filter(node->{
                    return node.getStatus().equals(TaskStatusType.BROKEN.getCode());
                })
                .collect(Collectors.toList());

        if(Objects.nonNull(brokenTask)&&brokenTask.size()>0){
            return TaskStatusType.BROKEN.getCode();
        }

        //STOP状态
        List<MultiTaskModel>stopTask=allTaskList.stream()
                .filter(node->{
                    return node.getStatus().equals(TaskStatusType.STOP.getCode());
                })
                .collect(Collectors.toList());

        if(Objects.nonNull(stopTask)&&stopTask.size()>0){
            return TaskStatusType.STOP.getCode();
        }


        //RUN状态
        List<MultiTaskModel>rdbRunTask=allTaskList.stream()
                .filter(node->{
                    return node.getStatus().equals(TaskStatusType.RUN.getCode());
                })
                .collect(Collectors.toList());


        if(Objects.nonNull(stopTask)&&rdbRunTask.size()>0){
            return TaskStatusType.RUN.getCode();
        }


        //COMMANDRUNING状态

        List<MultiTaskModel>commandRuningTask=allTaskList.stream()
                .filter(node->{
                    return node.getStatus().equals(TaskStatusType.COMMANDRUNING.getCode() );
                })
                .collect(Collectors.toList());

        if(Objects.nonNull(stopTask)&&commandRuningTask.size()==allTaskList.size()){
            return TaskStatusType.COMMANDRUNING.getCode();
        }

        //若不符合上述条件一律返回RDBRUNING
        return TaskStatusType.RDBRUNING.getCode();
    }
    /**
     * 停止任务s
     * @param taskids
     * @return
     * @throws Exception
     */
    public synchronized static Map<String,String> stopTaskList(List<String> taskids) throws Exception {
        Map<String,String>dataMap=new HashMap<>();
        taskids.stream().forEach(taskid->{
            if(aliveMulitSyncHashMap.containsKey(taskid)){
                ParentMultiTaskModel data=aliveMulitSyncHashMap.get(taskid);
                Stream.of(data.getRedisNodeA(),data.getRedisNodeB()).flatMap(Collection::stream)
                        .forEach(node->{
                            node.setStatus(TaskStatusType.STOP.getCode());
                        });
                dataMap.put(taskid, TaskStatusConstant.OK);
            }else {
                dataMap.put(taskid,TaskStatusConstant.FAIL);
            }
        });
        return dataMap;
    }


    /**
     * 判断相同参数的任务是否已创建并存在
     * @param data
     * @return
     * @throws Exception
     */
    public synchronized static boolean containsValue(MuiltCreateTaskData data) {
        long num=aliveMulitSyncHashMap.entrySet().stream().filter(mulitSync->{
            return mulitSync.getValue().getData().equals(data);
        }).count();

        return num>0L?true:false;
    }


    /**
     * 分页查询双向列表
     * @param currentPage
     * @param pageSize
     * @return
     * @throws Exception
     */
    public synchronized static List<ParentMultiTaskModelResult> listTaskList( int currentPage,int pageSize) throws Exception {
        if(currentPage<=0){
            currentPage=1;
        }
        List<ParentMultiTaskModelResult>multiTaskModelList= Lists.newArrayList();
        int nums=aliveMulitSyncHashMap.size();
        aliveMulitSyncHashMap.entrySet().stream().forEach(sync->{

            ParentMultiTaskModelResult parentMultiTaskModelResult=ParentMultiTaskModelResult.builder().build();
            BeanUtils.copyProperties(sync.getValue(),parentMultiTaskModelResult);
            List<MultiTaskModelResult> nodeA=DeepBeanUtils.cloneList(sync.getValue().getRedisNodeA(),MultiTaskModelResult.class);
            List<MultiTaskModelResult> nodeB=DeepBeanUtils.cloneList(sync.getValue().getRedisNodeB(),MultiTaskModelResult.class);
            parentMultiTaskModelResult.setRedisNodeA(nodeA);
            parentMultiTaskModelResult.setRedisNodeB(nodeB);
            parentMultiTaskModelResult.setStatus(MultiSyncTaskManagerutils.getTaskStatusByTaskId(sync.getKey()));
            multiTaskModelList.add(parentMultiTaskModelResult);
//            multiTaskModelList.addAll(sync.getValue().getRedisNodeB());
        });



        return  PageUtils.startPage(multiTaskModelList,currentPage,pageSize);
    }


}
