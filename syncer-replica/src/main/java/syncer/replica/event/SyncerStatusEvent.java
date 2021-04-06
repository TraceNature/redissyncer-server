package syncer.replica.event;


import syncer.replica.status.ReplicaStatus;

/**
 * @author: Eq Zhan
 * 监听订阅模式 STATUS消息通知 携带taskid
 *
 * @create: 2021-03-12
 **/
public class SyncerStatusEvent {
    private String taskId;
    private ReplicaStatus status;
}
