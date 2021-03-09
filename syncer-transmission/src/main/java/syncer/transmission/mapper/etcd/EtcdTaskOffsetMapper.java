package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;
import com.ibm.etcd.api.KeyValue;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.entity.etcd.EtcdOffSetEntity;
import syncer.transmission.entity.etcd.EtcdTaskGroup;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;
import syncer.transmission.mapper.TaskOffsetMapper;
import syncer.transmission.model.TaskOffsetModel;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * etcd task offset 操作
 * @author: Eq Zhan
 * @create: 2021-03-09
 **/

@Builder
@Data
@Slf4j
public class EtcdTaskOffsetMapper  implements TaskOffsetMapper {
    private JEtcdClient client ;
    private String nodeId;
    private final static String offsetLockName="updateOffset";

    @Override
    public boolean insetTaskOffset(TaskOffsetModel taskOffsetModel) throws Exception {
        AtomicBoolean status=new AtomicBoolean(false);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                EtcdOffSetEntity offSetEntity = EtcdOffSetEntity.builder().replOffset(new AtomicLong(taskOffsetModel.getOffset())).replId(taskOffsetModel.getReplId()).build();
                client.put(EtcdKeyCmd.getOffset(taskOffsetModel.getTaskId()), JSON.toJSONString(offSetEntity));
                status.set(true);
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(offsetLockName, taskOffsetModel.getTaskId());
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        return status.get();
    }

    @Override
    public boolean updateOffsetByTaskId(String taskId, long offset) throws Exception {
        AtomicBoolean status=new AtomicBoolean(false);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getOffset(taskId));
                EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
                offSetEntity.setReplOffset(new AtomicLong(offset));
                client.put(EtcdKeyCmd.getOffset(taskId), JSON.toJSONString(offSetEntity));
                status.set(true);
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(offsetLockName, taskId);
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        return status.get();
    }

    @Override
    public boolean updateReplIdByTaskId(String taskId, String replId) throws Exception {
        AtomicBoolean status=new AtomicBoolean(false);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getOffset(taskId));
                EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
                offSetEntity.setReplId(replId);
                client.put(EtcdKeyCmd.getOffset(taskId), JSON.toJSONString(offSetEntity));
                status.set(true);
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(offsetLockName, taskId);
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        return status.get();
    }

    @Override
    public boolean updateOffsetAndReplIdByTaskId(String taskId, String replId, long offset) throws Exception {
        AtomicBoolean status=new AtomicBoolean(false);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getOffset(taskId));
                EtcdOffSetEntity offSetEntity = JSON.parseObject(result, EtcdOffSetEntity.class);
                offSetEntity.setReplId(replId);
                offSetEntity.setReplOffset(new AtomicLong(offset));
                client.put(EtcdKeyCmd.getOffset(taskId), JSON.toJSONString(offSetEntity));
                status.set(true);
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(offsetLockName, taskId);
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        return status.get();
    }

    @Override
    public int delOffsetEntityByTaskId(String taskId) throws Exception {
        AtomicInteger num=new AtomicInteger(0);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                String result = client.get(EtcdKeyCmd.getOffset(taskId));
                if(Objects.nonNull(result)){
                    client.deleteByKey(EtcdKeyCmd.getOffset(taskId));
                    num.incrementAndGet();
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(offsetLockName, taskId);
            }
            @Override
            public int grant() {
                return 30;
            }
        });

        return num.get();
    }

    @Override
    public int delOffsetEntityByGroupId(String groupId) throws Exception {
        AtomicInteger num=new AtomicInteger(0);
        List<KeyValue> keyValueList=client.getPrefix(EtcdKeyCmd.getGroupIdPrefix(groupId));
        keyValueList.forEach(keyValue -> {
            try {
                EtcdTaskGroup taskGroup=JSON.parseObject(keyValue.getValue().toStringUtf8(),EtcdTaskGroup.class);
                delOffsetEntityByTaskId(taskGroup.getTaskId());
                num.incrementAndGet();
            } catch (Exception e) {
                log.error("del offset fail reason {}",e.getMessage());
            }
        });
        return num.get();
    }
}
