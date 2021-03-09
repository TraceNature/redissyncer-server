package syncer.transmission.entity.etcd;

import lombok.Builder;
import lombok.Data;

/**
 * @author: Eq Zhan
 * @create: 2021-02-23
 **/
@Builder
@Data
public class EtcdTaskGroup {
    private String groupId;
    private String taskId;
}
