package syncer.transmission.entity.etcd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Eq Zhan
 * @create: 2021-03-03
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtcdTaskIdEntity {
    private String taskId;
}
