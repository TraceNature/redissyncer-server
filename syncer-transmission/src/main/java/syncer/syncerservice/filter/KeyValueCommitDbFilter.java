package syncer.syncerservice.filter;

import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

/**
 * @author zhanenqiang
 * @Description 提交刷库操作节点
 * @Date 2020/7/22
 */
public class KeyValueCommitDbFilter implements CommonFilter{
    private CommonFilter next;
    private JDRedisClient client;
    private String taskId;

    public KeyValueCommitDbFilter(CommonFilter next, JDRedisClient client, String taskId) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
    }

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);
    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {
        if(null!=next) {
            next.run(replicator,eventEntity);
        }
    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }
}
