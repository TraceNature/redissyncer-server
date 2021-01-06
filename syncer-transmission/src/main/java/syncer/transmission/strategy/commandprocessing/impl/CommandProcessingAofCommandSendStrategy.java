package syncer.transmission.strategy.commandprocessing.impl;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.cmd.impl.DefaultCommand;
import syncer.replica.entity.FileType;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.event.Event;
import syncer.replica.event.PostCommandSyncEvent;
import syncer.replica.event.PreCommandSyncEvent;
import syncer.replica.replication.Replication;
import syncer.replica.util.objectutil.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

/**
 * 增量数据同步节点
 */
@Builder
@Getter
@Setter
@Slf4j
public class CommandProcessingAofCommandSendStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;

    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity) throws StartegyNodeException {
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
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"AOF文件同步开始");
//                    SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "AOF文件同步开始", TaskStatusType.COMMANDRUNING);
                }else {
                    log.warn("taskId为[{}]的任务增量同步开始..",taskId);
//                    SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "增量同步开始", TaskStatusType.COMMANDRUNING);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"增量同步开始");

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
//                    SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "AOF文件同步结束", TaskStatusType.STOP);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"AOF文件同步结束");
                }else {
                    log.warn("taskId为[{}]的任务增量同步结束..",taskId);
//                    SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "增量/同步结束", TaskStatusType.STOP);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"增量/同步结束");
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
            toNext(replication,eventEntity);

        }catch (Exception e){
            if(eventEntity.getEvent() instanceof DefaultCommand){
                DefaultCommand dc = (DefaultCommand) eventEntity.getEvent();
                System.out.println(Strings.byteToString(dc.getCommand())+": "+Strings.byteToString(dc.getArgs()));
            }
            throw new StartegyNodeException(e.getMessage()+"->AofCommandSendStrategy",e.getCause());
        }
    }

    @Override
    public void toNext(Replication replication, KeyValueEventEntity eventEntity) throws StartegyNodeException {
        if(null!=next){
            next.run(replication,eventEntity);
        }
    }

    @Override
    public void setNext(CommonProcessingStrategy nextStrategy) {
        this.next=nextStrategy;
    }
}
