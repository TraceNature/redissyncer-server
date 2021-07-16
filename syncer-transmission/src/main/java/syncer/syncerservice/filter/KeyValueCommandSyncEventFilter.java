package syncer.syncerservice.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.PostCommandSyncEvent;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

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
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {
        try {


        Event event=eventEntity.getEvent();

        //增量同步开始
        if(event instanceof PreCommandSyncEvent){

            if(eventEntity.getFileType().equals(FileType.ONLINERDB)
                    ||eventEntity.getFileType().equals(FileType.RDB)
                    ||eventEntity.getFileType().equals(FileType.ONLINEAOF)
                    ||eventEntity.getFileType().equals(FileType.AOF)
                    ||eventEntity.getFileType().equals(FileType.ONLINEMIXED)
                    ||eventEntity.getFileType().equals(FileType.MIXED)){
                log.warn("taskId为[{}]的任务AOF文件同步开始..",taskId);

                TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "AOF文件同步开始", TaskStatusType.COMMANDRUNING);

            }else {
                log.warn("taskId为[{}]的任务增量同步开始..",taskId);

                TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "增量同步开始", TaskStatusType.COMMANDRUNING);

            }

        }

        //增量同步结束（AOF文件）
        if (event instanceof PostCommandSyncEvent) {

            if(eventEntity.getFileType().equals(FileType.ONLINERDB)
                    ||eventEntity.getFileType().equals(FileType.RDB)
                    ||eventEntity.getFileType().equals(FileType.ONLINEAOF)
                    ||eventEntity.getFileType().equals(FileType.AOF)
                    ||eventEntity.getFileType().equals(FileType.ONLINEMIXED)
                    ||eventEntity.getFileType().equals(FileType.MIXED)){
                log.warn("taskId为[{}]AOF文件同步结束..",taskId);

                TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "AOF文件同步结束", TaskStatusType.STOP);

            }else {
                log.warn("taskId为[{}]的任务增量同步结束..",taskId);
                TaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "增量/同步结束", TaskStatusType.STOP);

            }

            return;
        }

        //命令解析器
        if (event instanceof DefaultCommand) {
            DefaultCommand dc = (DefaultCommand) event;
            client.send(dc.getCommand(),dc.getArgs());
            eventEntity.getBaseOffSet().setReplId(eventEntity.getReplId());
            eventEntity.getBaseOffSet().getReplOffset().set(eventEntity.getReplOffset());
        }

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);

        }catch (Exception e){
            throw new FilterNodeException(e.getMessage()+"->KeyValueCommandSyncEventFilter",e.getCause());
        }
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
