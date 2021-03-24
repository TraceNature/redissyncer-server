package syncer.transmission.entity.etcd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Eq Zhan
 * @create: 2021-03-10
 **/
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EtcdAbandonGroup {
    private Integer abandonId;
    private String taskId;
}
