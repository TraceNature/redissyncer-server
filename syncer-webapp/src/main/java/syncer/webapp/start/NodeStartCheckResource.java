package syncer.webapp.start;

import com.alibaba.fastjson.JSON;
import com.ibm.etcd.api.KeyValue;
import lombok.extern.slf4j.Slf4j;
import syncer.ShutdownContext;
import syncer.common.config.EtcdServerConfig;
import syncer.common.util.spring.SpringUtil;
import syncer.replica.status.TaskStatus;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.entity.etcd.EtcdTaskIdEntity;
import syncer.transmission.entity.etcd.NodeHeartbeat;
import syncer.transmission.etcd.IEtcdOpCenter;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;
import syncer.transmission.mapper.etcd.EtcdTaskMapper;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.sql.SqlOPUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: Eq Zhan
 * @create: 2021-03-02
 **/
@Slf4j
public class NodeStartCheckResource {
    private EtcdServerConfig config = new EtcdServerConfig();

    /**
     * 初始化检测资源情况。
     */
    public boolean initCheckResource() {
        if (Objects.isNull(config.getNodeId()) || "".equals(config.getNodeId().trim())) {
            log.error("nodeId is null");
            close();
            return true;
        }

        if (Objects.isNull(config.getEtcdConfig().getUrl()) || "".equals(config.getEtcdConfig().getUrl().trim())) {
            log.error("etcd host and port is empty");
            close();
            return true;
        }
        IEtcdOpCenter configCenter = JEtcdClient.build();
        try {
            AtomicBoolean status = new AtomicBoolean(false);
            configCenter.lockCommandRunner(new EtcdLockCommandRunner() {
                @Override
                public void run() {

                    String result = configCenter.get(new StringBuilder("/nodes/").append(config.getNodeType()).append("/").append(config.getNodeId()).toString());
                    if (Objects.nonNull(result)) {
                        NodeHeartbeat nodeHeartbeat = JSON.parseObject(result, NodeHeartbeat.class);
                        if (nodeHeartbeat.isOnline()) {
                            log.error("node id already exists");
                            close();
                            status.set(true);
                            return;
                        }else {
                            log.info("etcd check success");
                        }

                        if(config.isSingleNode()){
                            log.info("init single node task status");
                            initTaskStatus(configCenter);
                        }

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
            if (Objects.nonNull(configCenter)) {
                configCenter.close();
            }

            if (status.get()) {
                return true;
            }
        } catch (Exception e) {
            log.error("etcd connect error {}", e.getMessage());
            close();
            e.printStackTrace();
            return true;
        }finally {
            configCenter.close();
        }

        return false;

    }

    void close() {
        log.warn("node[{}] start fail ,closeing...", config.getNodeId());
        SpringUtil.getBean(ShutdownContext.class).showdown();
    }


    /**
     * 初始化任务状态
     */
    void initTaskStatus(IEtcdOpCenter configCenter) {
        List<KeyValue> keyValueList = configCenter.getPrefix(EtcdKeyCmd.getTasksByNodeId(config.getNodeId()));
        if (Objects.nonNull(keyValueList)) {
            keyValueList.stream().forEach(keyValue -> {
                EtcdTaskIdEntity etcdTaskIdEntity = JSON.parseObject(keyValue.getValue().toStringUtf8(), EtcdTaskIdEntity.class);
                try {
                    TaskModel taskModel = SqlOPUtils.findTaskById(etcdTaskIdEntity.getTaskId());
                    if (taskModel.getStatus() != TaskStatus.FINISH.getCode()
                            || taskModel.getStatus() != TaskStatus.STOP.getCode()
                            || taskModel.getStatus() != TaskStatus.BROKEN.getCode()) {
                        SqlOPUtils.updateTaskStatusById(taskModel.getId(), TaskStatus.BROKEN.getCode());
                    }
                } catch (Exception e) {
                    log.error("init node task id {} status fail {}", etcdTaskIdEntity.getTaskId(), e.getMessage());
                }
            });
        }
        log.info("init status finish");
    }
}
