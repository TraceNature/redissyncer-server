package syncer.syncerservice.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.PostCommandSyncEvent;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.SyncTaskUtils;

/**
 * 增量数据同步节点
 */
@Builder
@Getter
@Setter
@Slf4j
public class KeyValueCommandSyncEventFilter implements CommonFilter {
    private CommonFilter next;
    private JDRedisClient client;
    private String taskId;

    public KeyValueCommandSyncEventFilter(CommonFilter next, JDRedisClient client, String taskId) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
    }

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) {
        Event event=eventEntity.getEvent();

        //增量同步开始
        if(event instanceof PreCommandSyncEvent){
            log.warn("taskId为[{}]的任务增量/AOF文件同步开始..",taskId);
            SyncTaskUtils.editTaskMsg(taskId,"增量/AOF文件同步开始");
        }

        //增量同步结束（AOF文件）
        if (event instanceof PostCommandSyncEvent) {
            log.warn("taskId为[{}]的任务增量/AOF文件同步结束..",taskId);
            SyncTaskUtils.editTaskMsg(taskId,"增量/AOF文件同步结束");

        }

        //命令解析器
        if (event instanceof DefaultCommand) {
            DefaultCommand dc = (DefaultCommand) event;
            client.send(dc.getCommand(),dc.getArgs());
//            Configuration configuration=eventEntity.getConfiguration();
//            eventEntity.getBaseOffSet().setReplId(configuration.getReplId());
//            eventEntity.getBaseOffSet().getReplOffset().set(configuration.getReplOffset());

            eventEntity.getBaseOffSet().setReplId(eventEntity.getReplId());
            eventEntity.getBaseOffSet().getReplOffset().set(eventEntity.getReplOffset());
        }

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);
    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) {
        if(null!=next)
            next.run(replicator,eventEntity);
    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }
}
