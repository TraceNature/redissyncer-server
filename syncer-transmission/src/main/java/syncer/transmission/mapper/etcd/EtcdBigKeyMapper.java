package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ibm.etcd.api.KeyValue;
import lombok.Builder;
import lombok.Data;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.mapper.BigKeyMapper;
import syncer.transmission.model.BigKeyModel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * EtcdBigKey /tasks/bigkey/{taskId}/{bigKey}
 * value  {"id":0,"taskId":"","command":"","command_type":""}
 * @author: Eq Zhan
 * @create: 2021-03-09
 **/
@Builder
@Data
public class EtcdBigKeyMapper implements BigKeyMapper {
    private JEtcdClient client;
    private String nodeId;
    private EtcdID etcdID;
    private final static String bigKeyLockName="bigkeyLock";
    @Override
    public List<BigKeyModel> findBigKeyCommandListByTaskId(String taskId) throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getBigKeyByTaskIdPrefix(taskId));
        List<BigKeyModel>bigKeyModelList= Lists.newArrayList();
        if(Objects.nonNull(keyValueList)){
            List<BigKeyModel>bigKeyModels=keyValueList.stream().filter(keyValue -> {
                return Objects.nonNull(keyValue);
            }).map(keyValue -> {
                return JSON.parseObject(keyValue.getValue().toStringUtf8(),BigKeyModel.class);
            }).collect(Collectors.toList());
            bigKeyModelList.addAll(bigKeyModels);
        }
        return bigKeyModelList;
    }

    @Override
    public boolean insertBigKeyCommandModel(BigKeyModel bigKeyModel) throws Exception {
        int id=etcdID.getID(bigKeyLockName);
        bigKeyModel.setId(id);
        client.put(EtcdKeyCmd.getBigKeyByTaskIdAndId(bigKeyModel.getTaskId(), String.valueOf(id)), JSON.toJSONString(bigKeyModel));
        return true;
    }

    @Override
    public void deleteBigKeyCommandModelById(String id) throws Exception {

    }

    @Override
    public void deleteBigKeyCommandModelByTaskId(String taskId) throws Exception {
        client.deleteByKeyPrefix(EtcdKeyCmd.getBigKeyByTaskIdPrefix(taskId));
    }

    @Override
    public void deleteBigKeyCommandModelByGroupId(String groupId) throws Exception {

    }
}
