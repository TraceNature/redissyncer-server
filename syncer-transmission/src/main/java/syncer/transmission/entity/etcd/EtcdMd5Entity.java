package syncer.transmission.entity.etcd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Eq Zhan
 * @create: 2021-03-01
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EtcdMd5Entity {
    private String taskId;
    private String groupId;
    private String nodeId;
}
