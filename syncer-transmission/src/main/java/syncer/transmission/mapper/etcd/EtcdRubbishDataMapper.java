package syncer.transmission.mapper.etcd;

import lombok.Builder;
import lombok.Data;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.mapper.RubbishDataMapper;

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
