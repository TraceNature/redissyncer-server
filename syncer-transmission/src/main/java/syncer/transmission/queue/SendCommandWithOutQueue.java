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

package syncer.transmission.queue;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.event.iter.datatype.BatchedKeyValuePairEvent;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.constants.RedisCommandTypeEnum;
import syncer.transmission.model.AbandonCommandModel;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.ProcessingRunStrategyChain;
import syncer.transmission.util.DataCleanUtils;
import syncer.transmission.util.DataTypeUtils;
import syncer.transmission.util.RedisCommandTypeUtils;
import syncer.transmission.util.TaskGetUtils;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/9
 */

@Slf4j
@Builder
public class SendCommandWithOutQueue {
    private Replication replication;
    private RedisClient client;
    private ProcessingRunStrategyChain filterChain;
    private String taskId;
    private boolean status = true;
    private ISyncerCompensator syncerCompensator;
    private TaskModel taskModel;

    public SendCommandWithOutQueue(Replication replication, RedisClient client, ProcessingRunStrategyChain filterChain, String taskId, boolean status, ISyncerCompensator syncerCompensator,TaskModel taskModel) {
        this.replication = replication;
        this.client = client;
        this.filterChain = filterChain;
        this.taskId = taskId;
        this.status = status;
        this.syncerCompensator = syncerCompensator;
        this.taskModel=taskModel;
    }

    public SendCommandWithOutQueue(Replication replication, ProcessingRunStrategyChain filterChain, String taskId, boolean status, ISyncerCompensator syncerCompensator,TaskModel taskModel) {
        this.replication = replication;
        this.filterChain = filterChain;
        this.taskId = taskId;
        this.status  = true;
        this.syncerCompensator = syncerCompensator;
        this.taskModel=taskModel;
    }

    public void run(KeyValueEventEntity keyValueEventEntity){
            try {
                keyValueEventEntity.setISyncerCompensator(syncerCompensator);
                if(null!=keyValueEventEntity){
                    filterChain.run(replication,keyValueEventEntity,taskModel);
                }
//                DataCleanUtils.cleanData(keyValueEventEntity);
            }catch (Exception e){
                Event event=keyValueEventEntity.getEvent();
                String keyName=null;
                String command=null;
                String value="";
                int dataType=12;
                long ttl=-1L;
                if(event instanceof DefaultCommand){
                    DefaultCommand defaultCommand= (DefaultCommand) event;
                    command= Strings.byteToString(defaultCommand.getCommand());
                    if(defaultCommand.getArgs().length>0){
                        keyName= Strings.byteToString(((DefaultCommand) event).getCommand())+Strings.byteToString(((DefaultCommand) event).getArgs()[0]);
                        String []values=Strings.byteToString(((DefaultCommand) event).getArgs());
                        for (int i=0 ;i<values.length;i++){
                            value=value+" "+values[i];
                        }
                    }else{
                        keyName= Strings.byteToString(((DefaultCommand) event).getCommand());
                        value="";
                    }

                }else if(event instanceof DumpKeyValuePairEvent){
                    DumpKeyValuePairEvent dumpKeyValuePair= (DumpKeyValuePairEvent) event;
                    keyName= Strings.byteToString(dumpKeyValuePair.getKey());
                    value=Strings.byteToString(dumpKeyValuePair.getValue());
                    command="RestoreReplace";
                    if(dumpKeyValuePair.getExpiredMs()!=null){
                        ttl=dumpKeyValuePair.getExpiredMs();
                    }
                    dataType= DataTypeUtils.getType(dumpKeyValuePair.getDataType());
                }else if(event instanceof BatchedKeyValuePairEvent){
                    BatchedKeyValuePairEvent batchedKeyValuePair= (BatchedKeyValuePairEvent) event;
                    keyName=Strings.toString(batchedKeyValuePair.getKey());
                    value= BatchedKeyValuePairEvent.class+":"+JSON.toJSONString(batchedKeyValuePair.getValue());
                    RedisCommandTypeEnum typeEnum= RedisCommandTypeUtils.getRedisCommandTypeEnum(batchedKeyValuePair.getValueRdbType());
                    command=typeEnum.name();
                    if(batchedKeyValuePair.getExpiredMs()!=null){
                        ttl=batchedKeyValuePair.getExpiredMs();
                    }
                    dataType= DataTypeUtils.getType(batchedKeyValuePair.getDataType());
                }

                if(StringUtils.isEmpty(command)){
                    return;
                }

                //写入数据库抛弃key
                try {
                    if(keyName==null){
                        keyName="";
                    }
                    String message;
                    if(e.getMessage()==null){
                        message="";
                    }else{
                        message=e.getMessage();
                    }
                    String desc;
                    if(event==null){
                        desc="";

                    }else {
                        desc=event.getClass().toString();
                    }


                    SqlOPUtils.insertSimpleAbandonCommandModel(AbandonCommandModel
                            .builder()
                            .command(command)
                            .exception(message)
                            .key(keyName)
                            .taskId(taskId)
                            .type(dataType)
                            .desc(desc)
                            .ttl(ttl)
                            .groupId(TaskGetUtils.getRunningTaskGroupId(taskId))
                            .build());
                }catch (Exception ez){

                    log.error("[{}]抛弃key:{}信息写入数据库失败,原因[{}]",taskId, keyName,ez.getMessage());
                    ez.printStackTrace();
                }
                long errorCount= SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getTaskModel().getErrorCount();
                if (errorCount >= 0) {
                    long error= SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getErrorNums().incrementAndGet();
                    if (error >= errorCount) {
                        SingleTaskDataManagerUtils.brokenStatusAndLog("被抛弃key数量到达阈值[" + errorCount + "],exception reason["+e.getMessage()+"]", this.getClass(), taskId);
                    }
                }
                String stringCommand=command;
                if(event instanceof DefaultCommand){
                    try {
                        DefaultCommand defaultCommand= (DefaultCommand) event;
                        command= Strings.byteToString(defaultCommand.getCommand());
                        String [] args=Strings.byteToString(defaultCommand.getArgs());
                        StringBuilder commands=new StringBuilder();
                        commands.append(" ").append(command);
                        for (int i=0;i<args.length;i++){
                            String key=args[i];
                            commands.append(" ").append(key);
                        }
                        stringCommand=commands.toString();
                    }catch (Exception exq){
                        log.error("error command log error");
                    }

                }
                log.error("[{}]抛弃 command:[{}] key:{} value [{}] ,class:[{}]:原因[{}] ",taskId,stringCommand, keyName,value,event.getClass().toString(),e.getMessage());
                DataCleanUtils.cleanData(keyValueEventEntity,event);
                e.printStackTrace();
            }finally {
                if (null != keyValueEventEntity) {
                    DataCleanUtils.cleanData(keyValueEventEntity);
                }
            }
    }
}
