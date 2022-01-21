package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.ibm.etcd.api.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.entity.etcd.EtcdCompensationGroup;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.mapper.DataCompensationMapper;
import syncer.transmission.model.DataCompensationModel;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 *      * /tasks/compensation/{taskId}/{compensationId}    {}
 *      * /tasks/compensation/{groupId}/{compensationId}   {"compensationId": 1,"taskId":"xxx"}
 * Etcd 数据补偿 记录
 * @author: Eq Zhan
 * @create: 2021-03-09
 **/

@Builder
@Data
@Slf4j
public class EtcdDataCompensationMapper implements DataCompensationMapper {
    private JEtcdClient client;
    private String nodeId;
    private static final String lockName = "dataCompensation";
    private EtcdID etcdID;

    @Override
    public List<DataCompensationModel> selectAll() throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getCompensationPrefix());
        List<DataCompensationModel>dataCompensationModelList= Lists.newArrayList();
        if(Objects.nonNull(keyValueList)){
            dataCompensationModelList.addAll(keyValueList.stream().map(keyValue -> {
                return JSON.parseObject(keyValue.getValue().toStringUtf8(),DataCompensationModel.class);
            }).collect(Collectors.toList()));
        }
        return dataCompensationModelList;
    }

    @Override
    public List<DataCompensationModel> findDataCompensationModelListByTaskId(String taskId) throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getCompensationByTaskIdPrefix(taskId));
        List<DataCompensationModel>dataCompensationModelList= Lists.newArrayList();
        if(Objects.nonNull(keyValueList)){
            dataCompensationModelList.addAll(keyValueList.stream().map(keyValue -> {
                return JSON.parseObject(keyValue.getValue().toStringUtf8(),DataCompensationModel.class);
            }).collect(Collectors.toList()));
        }
        return dataCompensationModelList;
    }

    @Override
    public List<DataCompensationModel> findDataCompensationModelListByGroupId(String groupId) throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getCompensationByGroupIdPrefix(groupId));
        List<DataCompensationModel>dataCompensationModelList= Lists.newArrayList();
        if(Objects.nonNull(keyValueList)){
            dataCompensationModelList.addAll(keyValueList.stream().map(keyValue -> {
                EtcdCompensationGroup etcdCompensationGroup= JSON.parseObject(keyValue.getValue().toStringUtf8(),EtcdCompensationGroup.class);
                String res=client.get(EtcdKeyCmd.getCompensationByTaskId(etcdCompensationGroup.getTaskId(),etcdCompensationGroup.getCompensationId()));
                return JSON.parseObject(res,DataCompensationModel.class);
            }).collect(Collectors.toList()));
        }
        return dataCompensationModelList;
    }

    @Override
    public boolean insertDataCompensationModel(DataCompensationModel dataCompensationModel) throws Exception {
        int id=etcdID.getID(lockName);
        dataCompensationModel.setId(id);

        client.getKvClient()
                .txn(TxnRequest.newBuilder()
                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByTaskId(dataCompensationModel.getTaskId(),dataCompensationModel.getId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(dataCompensationModel))).build()).build())
                        .addSuccess(RequestOp.newBuilder().setRequestPut(PutRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByGroupId(dataCompensationModel.getGroupId(), dataCompensationModel.getId()))).setValue(ByteString.copyFromUtf8(JSON.toJSONString(EtcdCompensationGroup.builder().compensationId(dataCompensationModel.getId()).taskId(dataCompensationModel.getTaskId()).build()))).build()).build())
                        .build()).get();


        return true;
    }

    /**
     * etcd 弃用
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void deleteDataCompensationModelById(String id) throws Exception {

    }

    @Override
    public void deleteDataCompensationModelByTaskId(String taskId) throws Exception {
        List<DataCompensationModel>dataCompensationModelList=client.getPrefix(EtcdKeyCmd.getCompensationByTaskIdPrefix(taskId)).stream().filter(keyValue -> {
            return Objects.nonNull(keyValue);
        }).map(keyValue -> {
            return JSON.parseObject(keyValue.getValue().toStringUtf8(), DataCompensationModel.class);
        }).collect(Collectors.toList());
        if(Objects.nonNull(dataCompensationModelList)&& dataCompensationModelList.size()>0){
            client.getKvClient()
                    .txn(TxnRequest.newBuilder()
                            .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByTaskIdPrefix(taskId))).clearPrevKv().build()).build())
                            .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByGroupIdPrefix(dataCompensationModelList.get(0).getGroupId()))).clearPrevKv().build()).build())
                            .build()).get();
        }

    }

    @Override
    public void deleteDataCompensationModelByGroupId(String groupId) throws Exception {
        List<EtcdCompensationGroup> abandonCommandModels = client.getPrefix(EtcdKeyCmd.getCompensationByGroupIdPrefix(groupId)).stream().filter(keyValue -> {
            return Objects.nonNull(keyValue);
        }).map(keyValue -> {
            return JSON.parseObject(keyValue.getValue().toStringUtf8(), EtcdCompensationGroup.class);
        }).collect(Collectors.toList());
        if (Objects.nonNull(abandonCommandModels) && abandonCommandModels.size() > 0) {
            client.getKvClient()
                    .txn(TxnRequest.newBuilder()
                            .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByTaskIdPrefix(groupId))).clearPrevKv().build()).build())
                            .addSuccess(RequestOp.newBuilder().setRequestDeleteRange(DeleteRangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getCompensationByGroupIdPrefix(abandonCommandModels.get(0).getTaskId()))).clearPrevKv().build()).build())
                            .build()).get();
        }
    }
}
