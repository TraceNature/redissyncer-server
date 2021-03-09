package syncer.transmission.mapper.etcd;

import lombok.Builder;
import lombok.Data;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.mapper.BigKeyMapper;
import syncer.transmission.model.BigKeyModel;

import java.util.List;

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

    @Override
    public List<BigKeyModel> findBigKeyCommandListByTaskId(String taskId) throws Exception {

        return null;
    }

    @Override
    public boolean insertBigKeyCommandModel(BigKeyModel bigKeyModel) throws Exception {
        return false;
    }

    @Override
    public void deleteBigKeyCommandModelById(String id) throws Exception {

    }

    @Override
    public void deleteBigKeyCommandModelByTaskId(String taskId) throws Exception {

    }

    @Override
    public void deleteBigKeyCommandModelByGroupId(String groupId) throws Exception {

    }
}
