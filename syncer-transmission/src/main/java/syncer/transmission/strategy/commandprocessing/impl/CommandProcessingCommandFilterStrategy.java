package syncer.transmission.strategy.commandprocessing.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.event.iter.datatype.*;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.constants.CommandKeyFilterType;
import syncer.transmission.constants.RedisCommandTypeEnum;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.util.RedisCommandTypeUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 命令过滤策略
 * @author: Eq Zhan
 * @create: 2021-02-01
 **/
@Slf4j
@Builder
@NoArgsConstructor
public class CommandProcessingCommandFilterStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;
    private TaskModel taskModel;
    private Set<String>commandFilterSet=null;
    public CommandProcessingCommandFilterStrategy(CommonProcessingStrategy next, RedisClient client, String taskId,TaskModel taskModel) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.taskModel=taskModel;
    }

    public CommandProcessingCommandFilterStrategy(CommonProcessingStrategy next, RedisClient client, String taskId, TaskModel taskModel, Set<String> commandFilterSet) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.taskModel = taskModel;
        this.commandFilterSet = commandFilterSet;
    }

    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {

        if(Objects.isNull(taskModel.getFilterType())){
            toNext(replication,eventEntity,taskModel);
            return;
        }

        if(Objects.isNull(commandFilterSet)){
            commandFilterSet=taskModel.getCommandFilter()==null? Sets.newHashSet(): Sets.newHashSet(Arrays.asList(taskModel.getCommandFilter().split(",")).stream().map(s->{
                return s.trim().toUpperCase();
            }).collect(Collectors.toList()));
        }
        CommandKeyFilterType filterType=taskModel.getFilterType()==null?CommandKeyFilterType.NONE:taskModel.getFilterType();
        if(CommandKeyFilterType.NONE.equals(taskModel.getFilterType())){
            toNext(replication,eventEntity,taskModel);
            return;
        }
        String keyFilter=taskModel.getKeyFilter()==null?"":taskModel.getKeyFilter();
        Event event = eventEntity.getEvent();
        try {

            if (event instanceof DumpKeyValuePairEvent) {

                if(filterType.equals(CommandKeyFilterType.COMMAND_FILTER_REFUSE)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_REFUSE)){
                    if(commandFilterSet.contains("RESTORE")){
                        log.debug("TASKID[{}] command RESTORE refused",taskId);
                        return;
                    }
                }
                if(filterType.equals(CommandKeyFilterType.COMMAND_FILTER_ACCEPT)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_ACCEPT)){
                    if(!commandFilterSet.contains("RESTORE")){
                        log.debug("TASKID[{}] no command RESTORE refused",taskId);
                        return;
                    }
                }

                DumpKeyValuePairEvent dumpKeyValuePair= (DumpKeyValuePairEvent) event;
                String stringKey= Strings.toString(dumpKeyValuePair.getKey());
                //key
                if(filterType.equals(CommandKeyFilterType.KEY_FILTER_REFUSE)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_REFUSE)){

                    if(Pattern.matches(keyFilter,stringKey)){
                        log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                        return;
                    }
                }

                if(filterType.equals(CommandKeyFilterType.KEY_FILTER_ACCEPT)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_ACCEPT)){
                    if(!Pattern.matches(keyFilter,stringKey)){
                        log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                        return;
                    }
                }


                if(filterType.equals(CommandKeyFilterType.COMMAND_AND_KEY_FILTER_ACCEPT)){
                    if(!commandFilterSet.contains("RESTORE")||!Pattern.matches(keyFilter,stringKey)){
                        log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                        return;
                    }
                }

                if(filterType.equals(CommandKeyFilterType.COMMAND_AND_KEY_FILTER_REFUSE)){
                    if(commandFilterSet.contains("RESTORE")&&Pattern.matches(keyFilter,stringKey)){
                        log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                        return;
                    }
                }


            }

            if (event instanceof BatchedKeyValuePairEvent<?, ?>) {
                BatchedKeyValuePairEvent batchedKeyValuePair= (BatchedKeyValuePairEvent) event;
                RedisCommandTypeEnum typeEnum= RedisCommandTypeUtils.getRedisCommandTypeEnum(batchedKeyValuePair.getValueRdbType());
                if(filterType.equals(CommandKeyFilterType.COMMAND_FILTER_REFUSE)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_REFUSE)){
                    if(commandFilterSet.contains(getCommand(typeEnum))){
                        log.debug("command {} refused",getCommand(typeEnum));
                        return;
                    }
                }
                if(filterType.equals(CommandKeyFilterType.COMMAND_FILTER_ACCEPT)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_ACCEPT)){
                    if(!commandFilterSet.contains(getCommand(typeEnum))){
                        log.debug("command {} refused",getCommand(typeEnum));
                        return;
                    }
                }
                String stringKey=Strings.byteToString((byte[]) batchedKeyValuePair.getKey());
                //key
                if(filterType.equals(CommandKeyFilterType.KEY_FILTER_REFUSE)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_REFUSE)){

                    if(Pattern.matches(keyFilter,stringKey)){
                        log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                        return;
                    }
                }
                if(filterType.equals(CommandKeyFilterType.KEY_FILTER_ACCEPT)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_ACCEPT)){
                    if(!Pattern.matches(keyFilter,stringKey)){
                        log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                        return;
                    }
                }
                if(filterType.equals(CommandKeyFilterType.COMMAND_AND_KEY_FILTER_ACCEPT)){
                    if(!commandFilterSet.contains(getCommand(typeEnum))||!Pattern.matches(keyFilter,stringKey)){
                        log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                        return;
                    }
                }
                if(filterType.equals(CommandKeyFilterType.COMMAND_AND_KEY_FILTER_REFUSE)){
                    if(commandFilterSet.contains(getCommand(typeEnum))&&Pattern.matches(keyFilter,stringKey)){
                        log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                        return;
                    }
                }


            }

            if (event instanceof DefaultCommand) {
                DefaultCommand command= (DefaultCommand) event;
                String stringCommand= Strings.byteToString(command.getCommand()).trim().toUpperCase();
                if(filterType.equals(CommandKeyFilterType.COMMAND_FILTER_REFUSE)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_REFUSE)){
                    if(commandFilterSet.contains(stringCommand)){
                        log.debug("command {} refused",stringCommand);
                        return;
                    }
                }

                if(filterType.equals(CommandKeyFilterType.COMMAND_FILTER_ACCEPT)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_ACCEPT)){
                    if(!commandFilterSet.contains(stringCommand)){
                        log.debug("command {} refused",stringCommand);
                        return;
                    }
                }

                if(Objects.nonNull(command.getArgs())&&command.getArgs().length>0){
                    String stringKey=Strings.byteToString((byte[]) command.getArgs()[0]);
                    //key
                    if(filterType.equals(CommandKeyFilterType.KEY_FILTER_REFUSE)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_REFUSE)){

                        if(Pattern.matches(keyFilter,stringKey)){
                            log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                            return;
                        }
                    }

                    if(filterType.equals(CommandKeyFilterType.KEY_FILTER_ACCEPT)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_ACCEPT)){
                        if(!Pattern.matches(keyFilter,stringKey)){
                            log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                            return;
                        }
                    }

                    if(filterType.equals(CommandKeyFilterType.COMMAND_AND_KEY_FILTER_ACCEPT)){
                        if(!commandFilterSet.contains(stringCommand)||!Pattern.matches(keyFilter,stringKey)){
                            log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                            return;
                        }
                    }

                    if(filterType.equals(CommandKeyFilterType.COMMAND_AND_KEY_FILTER_REFUSE)){
                        if(commandFilterSet.contains(stringCommand)&&Pattern.matches(keyFilter,stringKey)){
                            log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                            return;
                        }
                    }

                }
            }

            if(event instanceof BatchedKeyStringValueStringEvent){
                BatchedKeyStringValueStringEvent stringEvent= (BatchedKeyStringValueStringEvent) event;
                String stringKey=Strings.byteToString(stringEvent.getKey());
                if(isRefuse(stringEvent.getValueRdbType(),filterType,stringKey,keyFilter)){
                    return;
                }
            }

            if(event instanceof BatchedKeyStringValueHashEvent) {
                BatchedKeyStringValueHashEvent hashEvent= (BatchedKeyStringValueHashEvent) event;
                String stringKey=Strings.byteToString(hashEvent.getKey());
                if(isRefuse(hashEvent.getValueRdbType(),filterType,stringKey,keyFilter)){
                    return;
                }
            }

            if (event instanceof BatchedKeyStringValueSetEvent) {
                BatchedKeyStringValueSetEvent setEvent= (BatchedKeyStringValueSetEvent) event;
                String stringKey=Strings.byteToString(setEvent.getKey());
                if(isRefuse(setEvent.getValueRdbType(),filterType,stringKey,keyFilter)){
                    return;
                }
            }

            if (event instanceof BatchedKeyStringValueListEvent) {
                BatchedKeyStringValueListEvent listEvent= (BatchedKeyStringValueListEvent) event;
                String stringKey=Strings.byteToString(listEvent.getKey());
                if(isRefuse(listEvent.getValueRdbType(),filterType,stringKey,keyFilter)){
                    return;
                }
            }

            if (event instanceof BatchedKeyStringValueZSetEvent) {
                BatchedKeyStringValueZSetEvent  zSetEvent= (BatchedKeyStringValueZSetEvent) event;
                String stringKey=Strings.byteToString(zSetEvent.getKey());
                if(isRefuse(zSetEvent.getValueRdbType(),filterType,stringKey,keyFilter)){
                    return;
                }
            }
            if (event instanceof BatchedKeyStringValueModuleEvent) {
                BatchedKeyStringValueModuleEvent moduleEvent= (BatchedKeyStringValueModuleEvent) event;
                String stringKey=Strings.byteToString(moduleEvent.getKey());
                if(isRefuse(moduleEvent.getValueRdbType(),filterType,stringKey,keyFilter)){
                    return;
                }
            }

            if (event instanceof BatchedKeyStringValueStreamEvent) {
                BatchedKeyStringValueStreamEvent streamEvent= (BatchedKeyStringValueStreamEvent) event;
                String stringKey=Strings.byteToString(streamEvent.getKey());
                if(isRefuse(streamEvent.getValueRdbType(),filterType,stringKey,keyFilter)){
                    return;
                }
            }


            toNext(replication,eventEntity,taskModel);
        }catch (Exception e){
            throw new StartegyNodeException(e.getMessage() + "->CommandFilterStrategy", e.getCause());
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


    String getCommand(RedisCommandTypeEnum typeEnum){
        if(typeEnum.equals(RedisCommandTypeEnum.STRING)){
            return "APPEND";
        }else if(typeEnum.equals(RedisCommandTypeEnum.LIST)){
            return "RPUSH";
        }else if(typeEnum.equals(RedisCommandTypeEnum.SET)){
            return "SADD";
        }else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
            return "ZADD";
        }else if(typeEnum.equals(RedisCommandTypeEnum.HASH)){
            return "HMSET";
        }
        return "";
    }


    boolean isRefuse(int valueRdbType,CommandKeyFilterType filterType,String stringKey,String keyFilter){
        RedisCommandTypeEnum typeEnum= RedisCommandTypeUtils.getRedisCommandTypeEnum(valueRdbType);
        if(filterType.equals(CommandKeyFilterType.COMMAND_FILTER_REFUSE)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_REFUSE)){
            if(commandFilterSet.contains(getCommand(typeEnum))){
                log.debug("command {} refused",getCommand(typeEnum));
                return true;
            }
        }
        if(filterType.equals(CommandKeyFilterType.COMMAND_FILTER_ACCEPT)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_ACCEPT)){
            if(!commandFilterSet.contains(getCommand(typeEnum))){
                log.debug("command {} refused",getCommand(typeEnum));
                return true;
            }
        }

        //key
        if(filterType.equals(CommandKeyFilterType.KEY_FILTER_REFUSE)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_REFUSE)){

            if(Pattern.matches(keyFilter,stringKey)){
                log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                return true;
            }
        }
        if(filterType.equals(CommandKeyFilterType.KEY_FILTER_ACCEPT)||filterType.equals(CommandKeyFilterType.COMMAND_OR_KEY_FILTER_ACCEPT)){
            if(!Pattern.matches(keyFilter,stringKey)){
                log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                return true;
            }
        }
        if(filterType.equals(CommandKeyFilterType.COMMAND_AND_KEY_FILTER_ACCEPT)){
            if(!commandFilterSet.contains(getCommand(typeEnum))||!Pattern.matches(keyFilter,stringKey)){
                log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                return true;
            }
        }
        if(filterType.equals(CommandKeyFilterType.COMMAND_AND_KEY_FILTER_REFUSE)){
            if(commandFilterSet.contains(getCommand(typeEnum))&&Pattern.matches(keyFilter,stringKey)){
                log.debug("TASKID[{}] command  key {} refused",taskId,stringKey);
                return true;
            }
        }
        return false;
    }

}
