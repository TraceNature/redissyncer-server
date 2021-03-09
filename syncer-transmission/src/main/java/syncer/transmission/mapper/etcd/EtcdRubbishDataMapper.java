package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.ibm.etcd.api.DeleteRangeRequest;
import com.ibm.etcd.api.RequestOp;
import com.ibm.etcd.api.TxnRequest;
import lombok.Builder;
import lombok.Data;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;
import syncer.transmission.mapper.RubbishDataMapper;
import syncer.transmission.model.TaskModel;

/**
 * TODO
 * @author: Eq Zhan
 * @create: 2021-03-05
 * 垃圾数据清理
 **/

@Builder
@Data
public class EtcdRubbishDataMapper implements RubbishDataMapper {
    private JEtcdClient client ;
    private String nodeId;

    @Override
    public void deleteRubbishDataFromTaskOffSet() {

    }

    @Override
    public void deleteRubbishDataFromTaskBigKey() {

    }

    @Override
    public void deleteRubbishDataFromTaskDataMonitor() {

    }

    @Override
    public void deleteRubbishDataFromTaskDataCompensation() {

    }

    @Override
    public void deleteRubbishDataFromTaskDataAbandonCommand() {

    }
}
