package syncer.transmission.mapper.etcd;

import com.ibm.etcd.api.KeyValue;
import lombok.Builder;
import lombok.Data;
import syncer.replica.status.TaskStatus;
import syncer.replica.type.SyncType;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.mapper.DashBoardMapper;

import java.util.List;
import java.util.Objects;


/**
 * @author: Eq Zhan
 * @create: 2021-03-05
 **/
@Builder
@Data
public class EtcdDashBoardMapper  implements DashBoardMapper {
    private JEtcdClient client ;
    private String nodeId;
    @Override
    public int taskCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTasksTaskIdPrefix());
        if (Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int brokenCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskListByStatusPrex(TaskStatus.BROKEN.getCode()));
        if (Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int stopCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskListByStatusPrex(TaskStatus.STOP.getCode()));
        if (Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int runCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskListByStatusPrex(TaskStatus.RDBRUNNING.getCode()));
        List<KeyValue>commandKeyValueList=client.getPrefix(EtcdKeyCmd.getTaskListByStatusPrex(TaskStatus.COMMANDRUNNING.getCode()));
        int num=0;
        if (Objects.nonNull(keyValueList)){
            num+=keyValueList.size();
        }
        if(Objects.nonNull(commandKeyValueList)){
            num+=commandKeyValueList.size();
        }
        return num;
    }

    @Override
    public int syncCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskTypePrefix(SyncType.SYNC.getCode()));
        if(Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int rdbCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskTypePrefix(SyncType.RDB.getCode()));
        if(Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int aofCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskTypePrefix(SyncType.AOF.getCode()));
        if(Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int mixedCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskTypePrefix(SyncType.MIXED.getCode()));
        if(Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int onlineRdbCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskTypePrefix(SyncType.ONLINERDB.getCode()));
        if(Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int onlineAofCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskTypePrefix(SyncType.ONLINEAOF.getCode()));
        if(Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int onlineMixedCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskTypePrefix(SyncType.ONLINEMIXED.getCode()));
        if(Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }

    @Override
    public int commandDumpUpCount() {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getTaskTypePrefix(SyncType.COMMANDDUMPUP.getCode()));
        if(Objects.isNull(keyValueList)){
            return 0;
        }
        return keyValueList.size();
    }
}
