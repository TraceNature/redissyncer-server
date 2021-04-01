package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.ibm.etcd.api.*;
import com.ibm.etcd.client.kv.KvClient;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import syncer.common.util.TimeUtils;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.entity.OffSetEntity;
import syncer.transmission.entity.etcd.*;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;
import syncer.transmission.mapper.TaskMapper;
import syncer.transmission.model.TaskModel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: Eq Zhan
 * @create: 2021-02-23
 **/
@Builder
@Data
@Slf4j
public class EtcdTaskMapper implements TaskMapper {
    private JEtcdClient client ;
    private String nodeId;

    @Override
    public List<TaskModel> selectAll() throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getTasksByNodeId(nodeId));
        List<TaskModel> taskModelList = Lists.newArrayList();
        if(Objects.isNull(keyValueList)){
            return taskModelList;
        }
        keyValueList.forEach(keyValue -> {
            EtcdTaskIdEntity etcdTaskIdEntity=JSON.parseObject(keyValue.getValue().toStringUtf8(),EtcdTaskIdEntity.class);
            if(Objects.nonNull(etcdTaskIdEntity)){
                String taskResult=client.get(EtcdKeyCmd.getTasksTaskId(etcdTaskIdEntity.getTaskId()));
                TaskModel taskModel = JSON.parseObject(taskResult, TaskModel.class);
                String result = client.get(EtcdKeyCmd.getOffset(taskModel.getTaskId()));
                if(Objects.nonNull(result)){
                    EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
                    taskModel.setOffset(offSetEntity.getReplOffset().get());
                    taskModel.setReplId(offSetEntity.getReplId());
                }
                taskModelList.add(taskModel);
            }

        });
        return taskModelList;
    }

    @Override
    public TaskModel findTaskById(String id) throws Exception {
        String value = client.get(EtcdKeyCmd.getTasksTaskId(id));
        if(Objects.nonNull(value)){
            TaskModel taskModel = JSON.parseObject(value, TaskModel.class);
            String result = client.get(EtcdKeyCmd.getOffset(taskModel.getTaskId()));
            if(Objects.nonNull(result)){
                EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
                taskModel.setOffset(offSetEntity.getReplOffset().get());
                taskModel.setReplId(offSetEntity.getReplId());
            }
            return taskModel;
        }
        return null;

    }

    @Override
    public int countItem() throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getTasksByNodeId(nodeId));
        return keyValueList.size();
    }

    @Override
    public List<TaskModel> findTaskBytaskName(String taskName) throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getNodeIdTaskName(taskName));
        List<TaskModel> taskModelList = Lists.newArrayList();
        if(Objects.nonNull(keyValueList)&&keyValueList.size()>0){
            keyValueList.forEach(keyValue -> {
                    EtcdTaskIdEntity etcdTaskIdEntity=JSON.parseObject(keyValue.getValue().toStringUtf8(), EtcdTaskIdEntity.class);
                    String taskResult=client.get(EtcdKeyCmd.getTasksTaskId(etcdTaskIdEntity.getTaskId()));
                    TaskModel taskModel = JSON.parseObject(taskResult, TaskModel.class);
                    String result = client.get(EtcdKeyCmd.getOffset(taskModel.getTaskId()));
                    EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
                    if(Objects.nonNull(offSetEntity)){
                        taskModel.setOffset(offSetEntity.getReplOffset().get());
                        taskModel.setReplId(offSetEntity.getReplId());
                    }
                    taskModelList.add(taskModel);
            });
        }
        return taskModelList;
    }

    @Override
    public List<TaskModel> findTaskBytaskMd5(String md5) throws Exception {
        String jresult = client.get(EtcdKeyCmd.getMd5(md5));
        List<TaskModel> taskModelList = Lists.newArrayList();
        if(Objects.nonNull(jresult)){
            EtcdTaskIdEntity etcdTaskIdEntity=JSON.parseObject(jresult, EtcdTaskIdEntity.class);
            String taskResult=client.get(EtcdKeyCmd.getTasksTaskId(etcdTaskIdEntity.getTaskId()));
            TaskModel taskModel = JSON.parseObject(taskResult, TaskModel.class);
            String result = client.get(EtcdKeyCmd.getOffset(taskModel.getTaskId()));
            EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
            if(Objects.nonNull(offSetEntity)){
                taskModel.setOffset(offSetEntity.getReplOffset().get());
                taskModel.setReplId(offSetEntity.getReplId());
            }
            taskModelList.add(taskModel);
        }
        return taskModelList;
    }

    @Override
    public List<TaskModel> findTaskBytaskStatus(Integer status) throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getTaskListByStatusPrex(status));
        List<TaskModel> taskModelList = Lists.newArrayList();
        keyValueList.forEach(keyValue -> {
            EtcdTaskIdEntity etcdTaskIdEntity=JSON.parseObject(keyValue.getValue().toStringUtf8(), EtcdTaskIdEntity.class);
            String taskResult=client.get(EtcdKeyCmd.getTasksTaskId(etcdTaskIdEntity.getTaskId()));
            TaskModel taskModel = JSON.parseObject(taskResult, TaskModel.class);
            String result = client.get(EtcdKeyCmd.getOffset(taskModel.getTaskId()));
            EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
            if(Objects.nonNull(offSetEntity)){
                taskModel.setOffset(offSetEntity.getReplOffset().get());
                taskModel.setReplId(offSetEntity.getReplId());
            }

            taskModelList.add(taskModel);
        });
        return taskModelList;
    }

    @Override
    public List<TaskModel> findTaskByGroupId(String groupId) throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getGroupIdPrefix(groupId));
        List<TaskModel> taskModelList = Lists.newArrayList();
        keyValueList.forEach(keyValue -> {
            EtcdTaskIdEntity etcdTaskIdEntity=JSON.parseObject(keyValue.getValue().toStringUtf8(), EtcdTaskIdEntity.class);
            String taskResult=client.get(EtcdKeyCmd.getTasksTaskId(etcdTaskIdEntity.getTaskId()));
            TaskModel taskModel = JSON.parseObject(taskResult, TaskModel.class);
            String result = client.get(EtcdKeyCmd.getOffset(taskModel.getTaskId()));
            EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
            if(Objects.nonNull(offSetEntity)){
                taskModel.setOffset(offSetEntity.getReplOffset().get());
                taskModel.setReplId(offSetEntity.getReplId());
            }
            taskModelList.add(taskModel);
        });
        return taskModelList;
    }

    /**
     * /tasks/taskid/{taskId}  json
     * /tasks/node/{nodeId}/{taskId} taskid
     * /tasks/groupid/{groupid}/{taskId}  groupid
     * /tasks/status/{currentstatus}/{taskid}  taskId
     * /tasks/name/{taskname}  {taskId,nodeId}
     * /tasks/offset/taskId  offset
     * /tasks/md5/{md5}   {taskId,groupId,nodeId}
     *
     * @param taskModel
     * @return
     * @throws Exception
     */

    @Override
    public boolean insertTask(TaskModel taskModel) throws Exception {
        taskModel.setCreateTime(TimeUtils.getNowTimeString());
        KvClient kvClient = client.getKvClient();
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    EtcdOffSetEntity offSetEntity = EtcdOffSetEntity.builder().replId(taskModel.getReplId()).replOffset(new AtomicLong(taskModel.getOffset())).build();
                    offSetEntity.getReplOffset().set(taskModel.getOffset());
                    TxnResponse response = kvClient
                            .txn(TxnRequest.newBuilder()
//                        .addSuccess(RequestOp.newBuilder().setRequestPut(kvClient.put(bs("newBuilderew"), bs("newval")).asRequest()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTasksTaskId(taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(taskModel))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getNodeIdTaskId(nodeId, taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdNodeTaskEntity.builder().taskId(taskModel.getTaskId()).nodeId(nodeId).build()))).build()).build())
                                    //groupId
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getGroupIdTaskId(taskModel.getGroupId(), taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdTaskGroup.builder().taskId(taskModel.getTaskId()).groupId(taskModel.getGroupId()).build()))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getStatusTaskId(taskModel.getStatus(), taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdTaskIdEntity.builder().taskId(taskModel.getTaskId()).build()))).build()).build())
                                    //   taskName 可能会多个
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getNodeIdTaskName(taskModel.getTaskName(),taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdTaskIdEntity.builder().taskId(taskModel.getTaskId()).build()))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getOffset(taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(offSetEntity))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getMd5(taskModel.getMd5()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdMd5Entity.builder().taskId(taskModel.getTaskId()).groupId(taskModel.getGroupId()).nodeId(nodeId).build()))).build()).build())
                                    //taskType
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTaskType(taskModel.getTasktype(),taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdMd5Entity.builder().taskId(taskModel.getTaskId()).groupId(taskModel.getGroupId()).nodeId(nodeId).build()))).build()).build())

                                    .build()).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", taskModel.getTaskId());
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        return true;
    }

    @Override
    public int insertTaskList(List<TaskModel> taskModelList) {
        AtomicInteger num = new AtomicInteger(0);
        taskModelList.forEach(taskModel -> {
            try {
                insertTask(taskModel);
                num.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return num.get();
    }

    @Override
    public boolean deleteTaskById(String id) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {

                    String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                    TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                    client.getKvClient()
                            .txn(TxnRequest.newBuilder()
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTasksTaskId(taskModel.getTaskId()))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getNodeIdTaskId(nodeId, taskModel.getTaskId()))).build()).build())
                                    //groupId
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getGroupIdTaskId(taskModel.getGroupId(), taskModel.getTaskId()))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getStatusTaskId(taskModel.getStatus(), taskModel.getTaskId()))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getNodeIdTaskName(taskModel.getTaskName(),taskModel.getTaskId()))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getOffset(taskModel.getTaskId()))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getMd5(taskModel.getMd5()))).build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTaskType(taskModel.getTasktype(),taskModel.getTaskId()))).build()).build())

                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getBigKeyByTaskIdPrefix(taskModel.getTaskId()))).clearPrevKv().build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByTaskIdPrefix(taskModel.getTaskId()))).clearPrevKv().build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByGroupIdPrefix(taskModel.getGroupId()))).clearPrevKv().build()).build())

                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByTaskIdPrefix(taskModel.getTaskId()))).clearPrevKv().build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByGroupIdPrefix(taskModel.getGroupId()))).clearPrevKv().build()).build())
                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getBigKeyByTaskIdPrefix(taskModel.getTaskId()))).clearPrevKv().build()).build())

                                    .build()).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public int deleteTasksByGroupId(String groupId) throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getGroupIdPrefix(groupId));
        AtomicInteger num = new AtomicInteger(0);
        keyValueList.forEach(keyValue -> {
            try {
                EtcdTaskGroup taskGroup = JSON.parseObject(keyValue.getValue().toStringUtf8(), EtcdTaskGroup.class);
                boolean result = deleteTaskById(taskGroup.getTaskId());
                if (result) {
                    num.incrementAndGet();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return num.get();
    }

    @Override
    public long deleteAllTask() {
        try {
            return client.getKvClient().delete(DeleteRangeRequest.newBuilder().setPrevKv(true).setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTasksTaskIdPrefix())).build()).get().getDeleted();
        } catch (Exception e) {
            log.error("deleteAllTask reason {}",e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean updateTask(TaskModel taskModel) throws Exception {
        client.put(EtcdKeyCmd.getTasksTaskId(taskModel.getTaskId()), JSON.toJSONString(taskModel));
        return true;
    }

    @Override
    public boolean updateTaskStatusById(String id, int status) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    String value = client.get(EtcdKeyCmd.getTasksTaskId(id));
                    TaskModel taskModel = JSON.parseObject(value, TaskModel.class);
                    Integer oldStatus = taskModel.getStatus();
                    if(oldStatus.equals(status)){
                        return;
                    }
                    taskModel.setStatus(status);
                    taskModel.setUpdateTime(TimeUtils.getNowTimeString());
                    client.getKvClient()
                            .txn(TxnRequest.newBuilder()
                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTasksTaskId(id))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(taskModel))).build()).build())


                                    .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getStatusTaskId(oldStatus, taskModel.getTaskId()))).build()).build())


                                    .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getStatusTaskId(status, taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdTaskIdEntity.builder().taskId(taskModel.getTaskId()).build()))).build()).build())
                                    .build()).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        return true;
    }

    @Override
    public boolean updateTaskStausByGroupId(String groupId, int status) throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getGroupIdPrefix(groupId));
        keyValueList.forEach(keyValue -> {
            EtcdTaskGroup taskGroup = JSON.parseObject(keyValue.getValue().toStringUtf8(), EtcdTaskGroup.class);
            try {
                updateTaskStatusById(taskGroup.getTaskId(), status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public boolean updateTaskOffsetById(String id, long offset) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getOffset(id));
                EtcdOffSetEntity offSetEntity=null;
                if(Objects.nonNull(result)){
                    offSetEntity= JSON.parseObject(result, EtcdOffSetEntity.class);
                    offSetEntity.setReplOffset(new AtomicLong(offset));
                }else {
                    String taskData=client.get(EtcdKeyCmd.getTasksTaskId(id));
                    TaskModel taskModel=JSON.parseObject(taskData,TaskModel.class);
                    taskModel.setUpdateTime(TimeUtils.getNowTimeString());
                    offSetEntity= EtcdOffSetEntity.builder().replId(taskModel.getReplId()).replOffset(new AtomicLong(offset)).build();
                }

                client.put(EtcdKeyCmd.getOffset(id), JSON.toJSONString(offSetEntity));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateOffset", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        return true;
    }

    @Override
    public boolean updateAfreshsetById(String id, boolean afresh) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                taskModel.setAfresh(afresh);
                taskModel.setUpdateTime(TimeUtils.getNowTimeString());
                client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });


        return true;
    }

    @Override
    public boolean updateTaskMsgAndStatusById(Integer status, String taskMsg, String id) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {

                try {
                    String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                    TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                    taskModel.setTaskMsg(taskMsg);
                    Integer oldStatus = taskModel.getStatus();
                    taskModel.setUpdateTime(TimeUtils.getNowTimeString());
                    if(oldStatus.equals(status)){
                        client.getKvClient()
                                .txn(TxnRequest.newBuilder()
                                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTasksTaskId(id))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(taskModel))).build()).build())
                                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getStatusTaskId(oldStatus, taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdTaskIdEntity.builder().taskId(taskModel.getTaskId()).build()))).build()).build())
                                        .build()).get();
                    }else {
                        taskModel.setStatus(status);
                        client.getKvClient()
                                .txn(TxnRequest.newBuilder()
                                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTasksTaskId(id))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(taskModel))).build()).build())
                                        .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getStatusTaskId(oldStatus, taskModel.getTaskId()))).build()).build())
                                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getStatusTaskId(oldStatus, taskModel.getTaskId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdTaskIdEntity.builder().taskId(taskModel.getTaskId()).build()))).build()).build())
                                        .build()).get();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateTaskMsgById(String taskMsg, String id) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                    TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                    taskModel.setTaskMsg(taskMsg);
                    client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
                }catch (Exception e){
                    log.error("updateTaskMsgById fail reason {}",e.getMessage());
                }

            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateTime(String id) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                taskModel.setUpdateTime(TimeUtils.getNowTimeString());
                client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateOffset(String id, Long offset) throws Exception {
        updateTaskOffsetById(id, offset);
        return true;
    }

    @Override
    public boolean updateOffsetAndReplId(String id, Long offset, String replId) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getOffset(id)); 
                EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
                offSetEntity.setReplOffset(new AtomicLong(offset));
                offSetEntity.setReplId(replId);
                client.put(EtcdKeyCmd.getOffset(id), JSON.toJSONString(offSetEntity));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateOffset", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateOffsetAndReplIdAndAllKey(String id, Long offset, String replId, String allKeyCount, String realKeyCount) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                String offsetresult = client.get(EtcdKeyCmd.getOffset(id));
                EtcdOffSetEntity offSetEntity = JSON.parseObject(offsetresult, EtcdOffSetEntity.class);
                offSetEntity.setReplOffset(new AtomicLong(offset));
                offSetEntity.setReplId(replId);
                taskModel.setAllKeyCount(Long.valueOf(allKeyCount));
                taskModel.setRealKeyCount(Long.valueOf(realKeyCount));
                client.lockCommandRunner(new EtcdLockCommandRunner() {
                    @Override
                    public void run() {

                        try {
                            client.getKvClient()
                                    .txn(TxnRequest.newBuilder()
                                            .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTasksTaskId(id))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(taskModel))).build()).build())
                                            .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getOffset(id))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(offSetEntity))).build()).build())
                                            .build()).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public String lockName() {
                        return EtcdKeyCmd.getLockName("updateOffset", id);
                    }

                    @Override
                    public int grant() {
                        return 30;
                    }
                });

//                client.put(EtcdKeyCmd.getOffset(id),JSON.toJSONString(offSetEntity));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateDataAnalysis(String id, String dataAnalysis) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                taskModel.setDataAnalysis(dataAnalysis);
                client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateRdbKeyCountById(String id, Long rdbKeyCount) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                taskModel.setRdbKeyCount(rdbKeyCount);
                client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateRealKeyCountById(String id, Long realKeyCount) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                taskModel.setRealKeyCount(realKeyCount);
                client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateAllKeyCountById(String id, Long allKeyCount) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                taskModel.setAllKeyCount(allKeyCount);
                client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateKeyCountById(String id, Long rdbKeyCount, Long allKeyCount, Long realKeyCount) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                taskModel.setRdbKeyCount(rdbKeyCount);
                taskModel.setAllKeyCount(allKeyCount);
                taskModel.setRealKeyCount(realKeyCount);

                client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public boolean updateExpandTaskModelById(String id, String expandJson) {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getTasksTaskId(id));
                TaskModel taskModel = JSON.parseObject(result, TaskModel.class);
                taskModel.setExpandJson(expandJson);
                client.put(EtcdKeyCmd.getTasksTaskId(id), JSON.toJSONString(taskModel));
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("updateTaskModel", id);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public void close() {
        client.close();
    }

}
