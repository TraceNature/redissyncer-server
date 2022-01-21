package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.ibm.etcd.api.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.entity.etcd.EtcdAbandonGroup;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.mapper.AbandonCommandMapper;
import syncer.transmission.model.AbandonCommandModel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * etcd 抛弃命令
 *  * /tasks/abandon/{taskId}/{abandonId}     {"id":1,"taskId":"xxx","groupId":"xxx","command":"xxx","key":"xxx","value":"xxx","type":1,"ttl":1000,"exception":"xxx","result":"xxx","desc":"xxx","createTime":"xxx"}
 *  * /tasks/abandon/{groupId}/{abandonId}   {"abandonId": 1,"taskId":"xxx"}
 * @author: Eq Zhan
 * @create: 2021-03-10
 **/
@Builder
@Data
@Slf4j
public class EtcdAbandonCommandMapper implements AbandonCommandMapper {
    private JEtcdClient client;
    private String nodeId;
    private static final String lockName = "dataAbandonCommand";
    private EtcdID etcdID;
    @Override
    public List<AbandonCommandModel> selectAll() throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getAbandonCommandPrefix());
        List<AbandonCommandModel>abandonCommandModelList= Lists.newArrayList();
        if(Objects.nonNull(keyValueList)){
            abandonCommandModelList.addAll(keyValueList.stream().map(keyValue -> {
                return JSON.parseObject(keyValue.getValue().toStringUtf8(),AbandonCommandModel.class);
            }).collect(Collectors.toList()));
        }
        return abandonCommandModelList;
    }

    @Override
    public List<AbandonCommandModel> findAbandonCommandListByTaskId(String taskId) throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getAbandonCommandByTaskIdPrefix(taskId));
        List<AbandonCommandModel>abandonCommandModelList= Lists.newArrayList();
        if(Objects.nonNull(keyValueList)){
            abandonCommandModelList.addAll(keyValueList.stream().map(keyValue -> {
                return JSON.parseObject(keyValue.getValue().toStringUtf8(),AbandonCommandModel.class);
            }).collect(Collectors.toList()));
        }
        return abandonCommandModelList;
    }

    @Override
    public List<AbandonCommandModel> findAbandonCommandListByGroupId(String groupId) throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getAbandonCommandByGroupIdPrefix(groupId));
        List<AbandonCommandModel>abandonCommandModelList= Lists.newArrayList();
        if(Objects.nonNull(keyValueList)){
            abandonCommandModelList.addAll(keyValueList.stream().map(keyValue -> {
                EtcdAbandonGroup etcdAbandonGroup=JSON.parseObject(keyValue.getValue().toStringUtf8(),EtcdAbandonGroup.class);
                return JSON.parseObject(client.get(EtcdKeyCmd.getAbandonCommandByTaskId(etcdAbandonGroup.getTaskId(),etcdAbandonGroup.getAbandonId())),AbandonCommandModel.class);
            }).collect(Collectors.toList()));
        }
        return abandonCommandModelList;
    }

    @Override
    public boolean insertAbandonCommandModel(AbandonCommandModel abandonCommandModel) throws Exception {
        int id=etcdID.getID(lockName);
        abandonCommandModel.setId(id);

        client.getKvClient()
                .txn(TxnRequest.newBuilder()
                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByTaskId(abandonCommandModel.getTaskId(),abandonCommandModel.getId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(abandonCommandModel))).build()).build())
                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByGroupId(abandonCommandModel.getGroupId(), abandonCommandModel.getId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdAbandonGroup.builder().abandonId(abandonCommandModel.getId()).taskId(abandonCommandModel.getTaskId()).build()))).build()).build())
                        .build()).get();
        return true;
    }

    @Override
    public boolean insertSimpleAbandonCommandModel(AbandonCommandModel abandonCommandModel) throws Exception {
        int id=etcdID.getID(lockName);
        abandonCommandModel.setId(id);
        client.getKvClient()
                .txn(TxnRequest.newBuilder()
                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByTaskId(abandonCommandModel.getTaskId(),abandonCommandModel.getId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(abandonCommandModel))).build()).build())
                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByGroupId(abandonCommandModel.getGroupId(), abandonCommandModel.getId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdAbandonGroup.builder().abandonId(abandonCommandModel.getId()).taskId(abandonCommandModel.getTaskId()).build()))).build()).build())
                        .build()).get();
        return true;
    }

    @Override
    public void deleteAbandonCommandModelById(String id) throws Exception {

    }

    @Override
    public void deleteAbandonCommandModelByTaskId(String taskId) throws Exception {
        List<AbandonCommandModel>abandonCommandModelList=client.getPrefix(EtcdKeyCmd.getAbandonCommandByTaskIdPrefix(taskId)).stream().filter(keyValue -> {
            return Objects.nonNull(keyValue);
        }).map(keyValue -> {
            return JSON.parseObject(keyValue.getValue().toStringUtf8(), AbandonCommandModel.class);
        }).collect(Collectors.toList());
        if(Objects.nonNull(abandonCommandModelList)&& abandonCommandModelList.size()>0){
            client.getKvClient()
                    .txn(TxnRequest.newBuilder()
                            .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByTaskIdPrefix(taskId))).clearPrevKv().build()).build())
                            .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByGroupIdPrefix(abandonCommandModelList.get(0).getGroupId()))).clearPrevKv().build()).build())
                            .build()).get();
        }

    }

    @Override
    public void deleteAbandonCommandModelByGroupId(String groupId) throws Exception {
        List<AbandonCommandModel>abandonCommandModelList=client.getPrefix(EtcdKeyCmd.getAbandonCommandByGroupIdPrefix(groupId)).stream().filter(keyValue -> {
            return Objects.nonNull(keyValue);
        }).map(keyValue -> {
            return JSON.parseObject(keyValue.getValue().toStringUtf8(), AbandonCommandModel.class);
        }).collect(Collectors.toList());
        if(Objects.nonNull(abandonCommandModelList)&& abandonCommandModelList.size()>0){
            client.getKvClient()
                    .txn(TxnRequest.newBuilder()
                            .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getAbandonCommandByGroupIdPrefix(groupId))).clearPrevKv().build()).build())
                            .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByGroupIdPrefix(abandonCommandModelList.get(0).getTaskId()))).clearPrevKv().build()).build())
                            .build()).get();
        }
    }
}
