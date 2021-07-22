 package syncer.transmission.strategy.commandprocessing.impl;

 import com.alibaba.fastjson.JSON;
 import lombok.Builder;
 import lombok.Getter;
 import lombok.Setter;
 import lombok.extern.slf4j.Slf4j;
 import syncer.replica.constant.RedisType;
 import syncer.replica.datatype.command.DefaultCommand;
 import syncer.replica.datatype.command.pubsub.PublishCommand;
 import syncer.replica.event.Event;
 import syncer.replica.parser.command.pubsub.PublishCommandParser;
 import syncer.replica.register.DefaultCommandNames;
 import syncer.replica.replication.Replication;
 import syncer.replica.util.strings.Strings;
 import syncer.transmission.client.RedisClient;
 import syncer.transmission.exception.StartegyNodeException;
 import syncer.transmission.model.TaskModel;
 import syncer.transmission.po.entity.KeyValueEventEntity;
 import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;

 /**
  * sentinel集群 sentinel PubSub 命令过滤，格式如下
  * PUBLISH:[__sentinel__:hello 10.0.1.31,26381,1b3cafd0f11c67a69b8b6a8924621db73db7617b,5,local-master,114.67.76.82,6380,5]
  */
 @Builder
 @Getter
 @Setter
 @Slf4j
 public class CommandProcessingSentinelCommandFileterStrategy implements CommonProcessingStrategy {
     private CommonProcessingStrategy next;
     private RedisClient client;
     private String taskId;
     private TaskModel taskModel;
     @Override
     public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
         try {
             if(RedisType.SENTINEL.getCode().equals(taskModel.getSourceRedisType())){
                 Event command =eventEntity.getEvent();
                 if(command instanceof DefaultCommand){
                     DefaultCommand defaultCommand= (DefaultCommand) command;
                     if(DefaultCommandNames.PUBLISH.equalsIgnoreCase(Strings.byteToString(defaultCommand.getCommand()))){
                         PublishCommandParser parser=new PublishCommandParser();
                         PublishCommand publishCommand=parser.parse(defaultCommand.getArgs());
                         if("__sentinel__:hello".equalsIgnoreCase(Strings.byteToString(publishCommand.getChannel()))){
                             return;
                         }

                     }
                 }
             }



             //继续执行下一Filter节点
             toNext(replication,eventEntity,taskModel);
         }catch (Exception e){
             throw new StartegyNodeException(e.getMessage()+"->SentinelCommandFileterStrategy",e.getCause());
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
