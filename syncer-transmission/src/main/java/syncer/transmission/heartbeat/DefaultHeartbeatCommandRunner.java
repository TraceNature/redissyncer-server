package syncer.transmission.heartbeat;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import syncer.common.config.EtcdAuthConfig;
import syncer.common.config.EtcdServerConfig;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.entity.etcd.NodeHeartbeat;
import syncer.transmission.etcd.IEtcdOpCenter;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;


/**
 * 默认
 *
 * @author: Eq Zhan
 * @create: 2021-02-20
 **/
@Slf4j
public class DefaultHeartbeatCommandRunner implements HeartbeatCommandRunner {

    private EtcdServerConfig config = new EtcdServerConfig();
    private IEtcdOpCenter configCenter = JEtcdClient.build();

    @Override
    public void run() {
        configCenter.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    NodeHeartbeat nodeHeartbeat = NodeHeartbeat.builder()
                            .NodeType(config.getNodeType())
                            .NodeId(config.getNodeId())
                            .LastReportTime(System.currentTimeMillis())
                            .Online(true)
                            .NodeAddr(config.getHost())
                            .NodePort(config.getLocalPort())
                            .heartbeatUrl("/health")
                            .build();
                    configCenter.put(new StringBuilder("/nodes/").append(config.getNodeType()).append("/").append(config.getNodeId()).toString(), JSON.toJSONString(nodeHeartbeat).toLowerCase());
                    log.info("node heartbeat success.");
                } catch (Exception e) {
                    log.info("node heartbeat fail.");
                    e.printStackTrace();
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName("heartbeat", config.getNodeId());
            }

            @Override
            public int grant() {
                return 30;
            }
        });


    }
}
