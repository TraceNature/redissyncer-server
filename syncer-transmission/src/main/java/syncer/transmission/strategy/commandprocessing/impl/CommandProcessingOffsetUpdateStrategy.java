package syncer.transmission.strategy.commandprocessing.impl;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.replication.Replication;
import syncer.replica.type.SyncType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.po.entity.OffSetCommitEntity;
import syncer.transmission.queue.DbDataCommitQueue;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;

import java.util.Objects;

/**
 * offset更新策略
 */
@Builder
@Getter
@Setter
@Slf4j
public class CommandProcessingOffsetUpdateStrategy implements CommonProcessingStrategy{
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;
    private TaskModel taskModel;

    public CommandProcessingOffsetUpdateStrategy(CommonProcessingStrategy next, RedisClient client, String taskId, TaskModel taskModel) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.taskModel = taskModel;
    }

    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        try {

            Event event=eventEntity.getEvent();
            if(Objects.nonNull(event)){
                if(eventEntity.getEvent() instanceof DefaultCommand) {
                    if (SyncType.SYNC.getCode().equals(taskModel.getSyncType())
                            || SyncType.COMMANDDUMPUP.getCode().equals(taskModel.getSyncType())) {
                        if (replication.getConfig().getReplId() != null &&replication.getConfig().getReplOffset() >=-1L) {
                            DbDataCommitQueue.put(OffSetCommitEntity.builder().taskId(taskId)
                                    .replId(replication.getConfig().getReplId())
                                    .offset(replication.getConfig().getReplOffset())
                                    .build());
                        }
                    }
                }
            }

            //继续执行下一Filter节点
            toNext(replication, eventEntity,taskModel);
        }catch (Exception e){
            throw new StartegyNodeException(e.getMessage() + "->OffsetUpdateStrategy", e.getCause());
        }
    }

    @Override
    public void toNext(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        if (null != next) {
            next.run(replication, eventEntity,taskModel);
        }
    }

    @Override
    public void setNext(CommonProcessingStrategy nextStrategy) {
        this.next = nextStrategy;
    }

}
