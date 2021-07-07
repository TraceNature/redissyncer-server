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

package syncer.transmission.strategy.commandprocessing.kafka;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.common.util.TimeUtils;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.datatype.command.common.PingCommand;
import syncer.replica.datatype.rdb.stream.Stream;
import syncer.replica.datatype.rdb.stream.StreamEntry;
import syncer.replica.datatype.rdb.stream.StreamID;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.AuxField;
import syncer.replica.event.Event;
import syncer.replica.event.end.PostCommandSyncEvent;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.iter.datatype.*;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.event.start.PreRdbSyncEvent;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.replica.status.TaskStatus;
import syncer.replica.type.FileType;
import syncer.replica.util.TaskRunTypeEnum;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.type.ExpiredType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.constants.RedisCommandTypeEnum;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.mq.kafka.KafkaProducerClient;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.util.RedisCommandTypeUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhanenqiang
 * @Description 存量命令写入
 * @Date 2020/12/23
 */
@Builder
@Slf4j
@Getter
@Setter
public class CommandProcessingKafkaRdbCommandSendStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private String taskId;
    private Date date;
    private TaskModel taskModel;
    private Long dbNum = -1L;
    private KafkaProducerClient kafkaProducerClient;
    public CommandProcessingKafkaRdbCommandSendStrategy(CommonProcessingStrategy next, String taskId, Date date, TaskModel taskModel, Long dbNum, KafkaProducerClient kafkaProducerClient) {
        this.next = next;
        this.taskId = taskId;
        this.date = new Date();
        this.taskModel=taskModel;
        this.kafkaProducerClient=kafkaProducerClient;
    }


    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        try {
            Event event=eventEntity.getEvent();
            AtomicReference<String> stringCommand=new AtomicReference<>();
            //全量同步开始
            if (event instanceof PreRdbSyncEvent) {
                log.warn("taskId为[{}]的全量数据到达同步程序同步开始..，当前时间：{}",taskId, TimeUtils.getNowTimeString());
                if(eventEntity.getFileType().equals(FileType.ONLINERDB)
                        ||eventEntity.getFileType().equals(FileType.RDB)
                        ||eventEntity.getFileType().equals(FileType.ONLINEAOF)
                        ||eventEntity.getFileType().equals(FileType.AOF)
                        ||eventEntity.getFileType().equals(FileType.ONLINEMIXED)
                        ||eventEntity.getFileType().equals(FileType.MIXED)){
                    log.warn("taskId为[{}]的文件任务全量同步开始..",taskId);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"[KAFKA]文件全量同步开始[同步任务启动]");
                }else{
                    log.warn("taskId为[{}]的任务全量同步开始..",taskId);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"[KAFKA]全量同步开始[同步任务启动]");
                }
            }else if (event instanceof PostRdbSyncEvent) {
                String time=(System.currentTimeMillis()-date.getTime())/(1000)+":s";
                if(eventEntity.getFileType().equals(FileType.ONLINERDB)
                        ||eventEntity.getFileType().equals(FileType.RDB)
                        ||eventEntity.getFileType().equals(FileType.ONLINEAOF)
                        ||eventEntity.getFileType().equals(FileType.AOF)
                        ||eventEntity.getFileType().equals(FileType.ONLINEMIXED)
                        ||eventEntity.getFileType().equals(FileType.MIXED)){
                    log.warn("[KAFKA]taskId为[{}]的文件任务全量同步结束[任务完成]..时间为:"+time,taskId);
                    SingleTaskDataManagerUtils.updateThreadMsg(taskId,"文件同步结束[任务完成] 时间(ms)："+time);
//                    SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "文件同步结束[任务完成] 时间(ms)："+time,TaskStatusType.STOP);
                }else {
                    if(eventEntity.getTaskRunTypeEnum().equals(TaskRunTypeEnum.TOTAL)){
                        log.warn("[KAFKA]taskId为[{}]的任务全量同步结束..进入增量同步模式 time:[{}] ",taskId,time);
                        SingleTaskDataManagerUtils.updateThreadMsg(taskId,"[KAFKA]全量同步结束进入增量同步 时间(ms)："+time+" 进入增量状态");
//                        SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "全量同步结束进入增量同步 时间(ms)："+time+" 进入增量状态",TaskStatusType.COMMANDRUNING);
                    }else if(eventEntity.getTaskRunTypeEnum().equals(TaskRunTypeEnum.STOCKONLY)){
                        log.warn("[KAFKA]taskId为[{}]的任务全量同步结束[任务完成]..",taskId);
                        SingleTaskDataManagerUtils.updateThreadStatusAndMsg(taskId, "[KAFKA]全量同步结束[任务完成] 时间(ms)："+time, TaskStatus.STOP);
//                        SingleTaskDataManagerUtils.updateThreadMsg(taskId,"全量同步结束[任务完成] 时间(ms)："+time);
                    }
                }
                return;
            }else if (event instanceof BatchedKeyStringValueStringEvent) {
                BatchedKeyStringValueStringEvent stringEvent = (BatchedKeyStringValueStringEvent) event;
                String key = Strings.byteToString(stringEvent.getKey());
                String value = Strings.byteToString(stringEvent.getValue());
                StringBuilder command = new StringBuilder();
                if (stringEvent.getBatch() == 0 && stringEvent.isLast()) {
                    command.append("SET ").append(key).append(" ").append(value);
                } else {
                    command.append("APPAND ").append(key).append(" ").append(value);
                }
                sendSelectDbCommand(stringEvent.getDb().getCurrentDbNumber(), key, kafkaProducerClient, taskModel.getTopicName());
                stringCommand.set(command.toString().trim());
                kafkaProducerClient.send(taskModel.getTopicName(), key, stringCommand.get());
                sendExpireCommand(event,key,kafkaProducerClient,taskModel.getTopicName());
            }else if (event instanceof BatchedKeyStringValueHashEvent) {
                BatchedKeyStringValueHashEvent hashEvent = (BatchedKeyStringValueHashEvent) event;
                if (hashEvent.getBatch() == 0 && !hashEvent.isLast()) {
                    return;
                }
                String key = Strings.byteToString(hashEvent.getKey());
                Map<byte[], byte[]> hashValue = hashEvent.getValue();
                StringBuilder command = new StringBuilder("HMSET ").append(key).append(" ");
                hashValue.entrySet().stream().forEach(value -> {
                    command.append(Strings.byteToString(value.getKey())).append(" ").append(Strings.byteToString(value.getValue())).append(" ");
                });
                sendSelectDbCommand(hashEvent.getDb().getCurrentDbNumber(), key, kafkaProducerClient, taskModel.getTopicName());
                stringCommand.set(command.toString().trim());
                kafkaProducerClient.send(taskModel.getTopicName(), key, stringCommand.get());
                sendExpireCommand(event,key,kafkaProducerClient,taskModel.getTopicName());
            }else if (event instanceof BatchedKeyStringValueSetEvent) {
                BatchedKeyStringValueSetEvent setEvent = (BatchedKeyStringValueSetEvent) event;
                if (setEvent.getBatch() == 0 && !setEvent.isLast()) {
                    return;
                }
                String key = Strings.byteToString(setEvent.getKey());
                StringBuilder command = new StringBuilder("SADD ").append(key).append(" ");
                Set<byte[]> setValue = setEvent.getValue();
                setValue.stream().forEach(value -> {
                    command.append(Strings.byteToString(value)).append(" ");
                });
                sendSelectDbCommand(setEvent.getDb().getCurrentDbNumber(), key, kafkaProducerClient, taskModel.getTopicName());
                stringCommand.set(command.toString().trim());
                kafkaProducerClient.send(taskModel.getTopicName(), key, stringCommand.get());
                sendExpireCommand(event,key,kafkaProducerClient,taskModel.getTopicName());
            }else if (event instanceof BatchedKeyStringValueListEvent) {
                BatchedKeyStringValueListEvent listEvent = (BatchedKeyStringValueListEvent) event;
                if (listEvent.getBatch() == 0 && !listEvent.isLast()) {
                    return;
                }
                String key = Strings.byteToString(listEvent.getKey());
                StringBuilder command = new StringBuilder("RPUSH ").append(key).append(" ");
                List<byte[]> setValue = listEvent.getValue();
                setValue.stream().forEach(value -> {
                    command.append(Strings.byteToString(value)).append(" ");
                });
                sendSelectDbCommand(listEvent.getDb().getCurrentDbNumber(), key, kafkaProducerClient, taskModel.getTopicName());
                stringCommand.set(command.toString().trim());
                kafkaProducerClient.send(taskModel.getTopicName(), key, stringCommand.get());
                sendExpireCommand(event,key,kafkaProducerClient,taskModel.getTopicName());
            }else if (event instanceof BatchedKeyStringValueZSetEvent) {
                BatchedKeyStringValueZSetEvent zSetEvent = (BatchedKeyStringValueZSetEvent) event;
                if (zSetEvent.getBatch() == 0 && !zSetEvent.isLast()) {
                    return;
                }
                String key = Strings.byteToString(zSetEvent.getKey());
                Set<ZSetEntry> zSetEntries = zSetEvent.getValue();
                StringBuilder command = new StringBuilder("ZADD ").append(key).append(" ");
                zSetEntries.stream().forEach(zSetEntry -> {
                    command.append(zSetEntry.getScore()).append(" ").append(Strings.byteToString(zSetEntry.getElement())).append(" ");
                });
                sendSelectDbCommand(zSetEvent.getDb().getCurrentDbNumber(), key, kafkaProducerClient, taskModel.getTopicName());
                stringCommand.set(command.toString().trim());
                kafkaProducerClient.send(taskModel.getTopicName(), key, stringCommand.get());
                sendExpireCommand(event,key,kafkaProducerClient,taskModel.getTopicName());
            }else if (event instanceof BatchedKeyStringValueModuleEvent) {
                BatchedKeyStringValueModuleEvent moduleEvent = (BatchedKeyStringValueModuleEvent) event;
                System.out.println("命令订阅全量数据暂不支持module结构,key:[" + Strings.byteToString(moduleEvent.getKey()) + "]");
            }else if (event instanceof BatchedKeyStringValueStreamEvent) {
                BatchedKeyStringValueStreamEvent streamEvent = (BatchedKeyStringValueStreamEvent) event;
                if (streamEvent.getBatch() == 0 && !streamEvent.isLast()) {
                    return;
                }
                String key = Strings.byteToString(streamEvent.getKey());
                Stream streamValue = streamEvent.getValue();
                Map<StreamID, StreamEntry> streamEntryNavigableMap = streamValue.getEntries();
                streamEntryNavigableMap.entrySet().stream().forEach(streamIDStreamEntryEntry -> {
                    StringBuilder commands = new StringBuilder();
                    commands.append("XADD ").append(key).append(" ").append(streamIDStreamEntryEntry.getValue().getId()).append(" ");
                    streamIDStreamEntryEntry.getValue().getFields().entrySet().stream().forEach(entry -> {
                        commands.append(Strings.byteToString(entry.getKey())).append(" ").append(Strings.byteToString(entry.getValue())).append(" ");
                    });
                    sendSelectDbCommand(streamEvent.getDb().getCurrentDbNumber(), key, kafkaProducerClient, taskModel.getTopicName());
                    stringCommand.set(commands.toString().trim());
                    kafkaProducerClient.send(taskModel.getTopicName(), key, stringCommand.get());
                    sendExpireCommand(event,key,kafkaProducerClient,taskModel.getTopicName());
                });
                streamValue.getGroups().stream().forEach(streamGroup -> {
                    StringBuilder commands = new StringBuilder("XGROUP CREATE ");
                    commands.append(key).append(" ").append(Strings.byteToString(streamGroup.getName())).append(" ").append(streamGroup.getLastId().toString());
                    sendSelectDbCommand(streamEvent.getDb().getCurrentDbNumber(), key, kafkaProducerClient, taskModel.getTopicName());
                    stringCommand.set(commands.toString().trim());
                    kafkaProducerClient.send(taskModel.getTopicName(), key, stringCommand.get());
                    sendExpireCommand(event,key,kafkaProducerClient,taskModel.getTopicName());
                });
            }else if (event instanceof AuxField) {
                return;
            }else if (event instanceof PreRdbSyncEvent) {
                return;
            }else if (event instanceof PingCommand) {
                return;
            }else if (event instanceof PostRdbSyncEvent) {
                return;
            }else if (event instanceof PreCommandSyncEvent) {
                return;
            }else if (event instanceof PostCommandSyncEvent) {
                return;
            }else if (event instanceof DefaultCommand) {
                DefaultCommand defaultCommand = (DefaultCommand) event;

                StringBuilder commands = new StringBuilder(Strings.byteToString(defaultCommand.getCommand())).append(" ");
                Arrays.stream(defaultCommand.getArgs()).forEach(command -> {
                    commands.append(Strings.byteToString(command)).append(" ");
                });
                stringCommand.set(commands.toString().trim());
                if (Objects.nonNull(defaultCommand.getArgs()) && defaultCommand.getArgs().length >= 1) {
                    kafkaProducerClient.send(taskModel.getTopicName(), Strings.byteToString(defaultCommand.getArgs()[0]), stringCommand.get());
                } else {
                    kafkaProducerClient.send(taskModel.getTopicName(), Strings.byteToString(defaultCommand.getCommand()), stringCommand.get());
                }
            } else if(event instanceof PreCommandSyncEvent){

            } else if(event instanceof PostCommandSyncEvent){

            } else if(event instanceof DefaultCommand){

            }else {
                System.out.println("命令订阅全量数据暂不支持此结构");
                System.out.println(JSON.toJSONString(event));
            }


            //继续执行下一Filter节点
            toNext(replication,eventEntity,taskModel);
        }catch (Exception e){
            throw new StartegyNodeException(e.getMessage()+"->RdbCommandSendStrategy",e.getCause());
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


    void sendSelectDbCommand(Long currentDb, String key, KafkaProducerClient kafkaProducerClient, String topicName) {
        if (Objects.nonNull(currentDb)) {
            if (!dbNum.equals(currentDb)) {
                dbNum = currentDb;
                StringBuilder command = new StringBuilder("SELECT ").append(currentDb);
                kafkaProducerClient.send(topicName, key, command.toString());
            }
        }
    }


    void sendExpireCommand(Event event, String key, KafkaProducerClient kafkaProducerClient, String topicName) {
        String command=null;
        if (event instanceof BatchedKeyStringValueStringEvent) {
            BatchedKeyStringValueStringEvent stringEvent = (BatchedKeyStringValueStringEvent) event;
            if (ExpiredType.SECOND.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandSec(stringEvent.getExpiredSeconds(), key);

            } else if (ExpiredType.MS.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandMs(stringEvent.getExpiredMs(), key);
            } else {
                return;
            }
        } else if (event instanceof BatchedKeyStringValueHashEvent) {
            BatchedKeyStringValueHashEvent stringEvent = (BatchedKeyStringValueHashEvent) event;
            if (ExpiredType.SECOND.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandSec(stringEvent.getExpiredSeconds(), key);
            } else if (ExpiredType.MS.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandMs(stringEvent.getExpiredMs(), key);
            } else {
                return;
            }
        } else if (event instanceof BatchedKeyStringValueSetEvent) {
            BatchedKeyStringValueSetEvent stringEvent = (BatchedKeyStringValueSetEvent) event;
            if (ExpiredType.SECOND.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandSec(stringEvent.getExpiredSeconds(), key);
            } else if (ExpiredType.MS.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandMs(stringEvent.getExpiredMs(), key);
            } else {
                return;
            }
        } else if (event instanceof BatchedKeyStringValueListEvent) {
            BatchedKeyStringValueListEvent stringEvent = (BatchedKeyStringValueListEvent) event;
            if (ExpiredType.SECOND.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandSec(stringEvent.getExpiredSeconds(), key);
            } else if (ExpiredType.MS.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandMs(stringEvent.getExpiredMs(), key);
            } else {
                return;
            }
        } else if (event instanceof BatchedKeyStringValueZSetEvent) {
            BatchedKeyStringValueZSetEvent stringEvent = (BatchedKeyStringValueZSetEvent) event;
            if (ExpiredType.SECOND.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandSec(stringEvent.getExpiredSeconds(), key);
            } else if (ExpiredType.MS.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandMs(stringEvent.getExpiredMs(), key);
            } else {
                return;
            }
        } else if (event instanceof BatchedKeyStringValueStreamEvent) {
            BatchedKeyStringValueStreamEvent stringEvent = (BatchedKeyStringValueStreamEvent) event;
            if (ExpiredType.SECOND.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandSec(stringEvent.getExpiredSeconds(), key);
            } else if (ExpiredType.MS.equals(stringEvent.getExpiredType())) {
                command = getExpireCommandMs(stringEvent.getExpiredMs(), key);
            } else {
                return;
            }
        }
        if(Objects.nonNull(command)){
            kafkaProducerClient.send(topicName,key,command);
        }
    }

    String getExpireCommandSec(Integer time, String key) {
        StringBuilder command = new StringBuilder("expire ").append(key).append(" ").append(time);
        return command.toString();
    }


    String getExpireCommandMs(Long time, String key) {
        StringBuilder command = new StringBuilder("pexpire ").append(key).append(" ").append(time);
        return command.toString();
    }
}
