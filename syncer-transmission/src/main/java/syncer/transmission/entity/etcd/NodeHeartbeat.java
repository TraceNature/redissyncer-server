package syncer.transmission.entity.etcd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 心跳检测上报参数
 * @author: Eq Zhan
 * @create: 2021-02-22
 **/
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NodeHeartbeat {
    /**
     * 节点类型
     */
    private String NodeType;
    /**
     * 节点Id
     */
    private String NodeId;
    /**
     * 节点ip地址
     */
    private String NodeAddr;
    /**
     * 探活port
     */
    private Integer NodePort;

    private boolean Online;

    private long LastReportTime;
}
