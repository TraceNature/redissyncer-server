// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.strategy.commandprocessing.impl;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.event.end.PostCommandSyncEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.replication.Replication;
import syncer.replica.type.FileType;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

/**
 * 断点续传2.0
 * 增量数据同步节点
 */
@Builder
@Getter
@Setter
@Slf4j
public class CommandProcessingAofMultiCommandSendStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;
    private TaskModel taskModel;

    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
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
                client.updateLastReplidAndOffset(replication.getConfig().getReplId(),replication.getConfig().getReplOffset());
                client.send(dc.getCommand(),dc.getArgs());
                eventEntity.getBaseOffSet().setReplId(eventEntity.getReplId());
                eventEntity.getBaseOffSet().getReplOffset().set(eventEntity.getReplOffset());
            }

            //继续执行下一Filter节点
            toNext(replication,eventEntity,taskModel);

        }catch (Exception e){
            if(eventEntity.getEvent() instanceof DefaultCommand){
                DefaultCommand dc = (DefaultCommand) eventEntity.getEvent();
            }
            e.printStackTrace();
            throw new StartegyNodeException(e.getMessage()+"->AofCommandSendStrategy",e.getCause());
        }
    }

    @Override
    public void toNext(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        if(null!=next){
            next.run(replication,eventEntity, taskModel);
        }
    }

    @Override
    public void setNext(CommonProcessingStrategy nextStrategy) {
        this.next=nextStrategy;
    }
}
