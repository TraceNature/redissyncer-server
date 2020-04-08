package syncer.syncerservice.filter;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.PostCommandSyncEvent;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.SyncTaskUtils;
import syncer.syncerservice.util.common.Strings;

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
                SyncTaskUtils.editTaskMsg(taskId,"AOF文件同步开始");
            }else {
                log.warn("taskId为[{}]的任务增量同步开始..",taskId);
                SyncTaskUtils.editTaskMsg(taskId,"增量同步开始");
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
                SyncTaskUtils.editTaskMsg(taskId,"AOF文件同步结束");
                SyncTaskUtils.stopCreateThread(taskId);
            }else {
                log.warn("taskId为[{}]的任务增量同步结束..",taskId);
                SyncTaskUtils.editTaskMsg(taskId,"增量/同步结束");
            }

            return;
        }

        //命令解析器
        if (event instanceof DefaultCommand) {
            DefaultCommand dc = (DefaultCommand) event;

            if(dc!=null){

                if(dc.getCommand()==null){
                    log.info("[{}]命令null,赋值为空..command:[{}]",taskId,Strings.byteToString(dc.getCommand()));

                }
                if(dc.getArgs()==null){
                    dc.setArgs(new byte[0][]);
                    log.info("[{}]命令参数为null,赋值为空..command:[{}]",taskId,Strings.byteToString(dc.getCommand()));
                }

                Object result=client.send(dc.getCommand(),dc.getArgs());
            }else {
                log.info("[{}]DefaultCommand命令null",taskId);
            }


//            log.debug("增量命令 command:{} Args[0]:{}",dc.getCommand(),dc.getArgs());
//            Configuration configuration=eventEntity.getConfiguration();
//            eventEntity.getBaseOffSet().setReplId(configuration.getReplId());
//            eventEntity.getBaseOffSet().getReplOffset().set(configuration.getReplOffset());

            try {
                eventEntity.getBaseOffSet().setReplId(eventEntity.getReplId());
                eventEntity.getBaseOffSet().getReplOffset().set(eventEntity.getReplOffset());
            }catch (Exception e){
                log.info("task:[{}],command:{} 记录offset失败,无影响",taskId, Strings.byteToString(dc.getCommand()));
            }

        }

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);

        }catch (Exception e){
            Event event=eventEntity.getEvent();
            if (event instanceof DefaultCommand) {
                DefaultCommand dc = (DefaultCommand) event;
                log.info("[{}]抛弃key的命令[{}] ,参数为：[{}]",taskId,Strings.byteToString(dc.getCommand()),JSON.toJSONString(Strings.byteToString(dc.getArgs())));
            }

            e.printStackTrace();
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
