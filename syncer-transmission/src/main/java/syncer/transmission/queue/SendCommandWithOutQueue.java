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

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.replica.cmd.impl.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.replica.rdb.sync.datatype.DumpKeyValuePair;
import syncer.replica.replication.Replication;
import syncer.replica.util.objectutil.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.constants.RedisCommandTypeEnum;
import syncer.transmission.model.AbandonCommandModel;
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


    public SendCommandWithOutQueue(Replication replication, RedisClient client, ProcessingRunStrategyChain filterChain, String taskId, boolean status, ISyncerCompensator syncerCompensator) {
        this.replication = replication;
        this.client = client;
        this.filterChain = filterChain;
        this.taskId = taskId;
        this.status = status;
        this.syncerCompensator = syncerCompensator;
    }

    public SendCommandWithOutQueue(Replication replication, ProcessingRunStrategyChain filterChain, String taskId, boolean status, ISyncerCompensator syncerCompensator) {
        this.replication = replication;
        this.filterChain = filterChain;
        this.taskId = taskId;
        this.status  = true;
        this.syncerCompensator = syncerCompensator;
    }

    public void run(KeyValueEventEntity keyValueEventEntity){
            try {
                keyValueEventEntity.setISyncerCompensator(syncerCompensator);
                if(null!=keyValueEventEntity){
                    filterChain.run(replication,keyValueEventEntity);
                }
                DataCleanUtils.cleanData(keyValueEventEntity);
            }catch (Exception e){
                Event event=keyValueEventEntity.getEvent();
                String keyName=null;
                String command=null;
                String value=null;
                int dataType=12;
                long ttl=-1L;
                if(event instanceof DefaultCommand){
                    DefaultCommand defaultCommand= (DefaultCommand) event;
                    command= Strings.byteToString(defaultCommand.getCommand());
                    if(defaultCommand.getArgs().length>0){
                        keyName= Strings.byteToString(((DefaultCommand) event).getCommand())+Strings.byteToString(((DefaultCommand) event).getArgs()[0]);
                    }else{
                        keyName= Strings.byteToString(((DefaultCommand) event).getCommand());
                    }
                }else if(event instanceof DumpKeyValuePair){
                    DumpKeyValuePair dumpKeyValuePair= (DumpKeyValuePair) event;
                    keyName= Strings.byteToString(dumpKeyValuePair.getKey());
                    command="RestoreReplace";
                    if(dumpKeyValuePair.getExpiredMs()!=null){
                        ttl=dumpKeyValuePair.getExpiredMs();
                    }
                    dataType= DataTypeUtils.getType(dumpKeyValuePair.getDataType());
                }else if(event instanceof BatchedKeyValuePair){
                    BatchedKeyValuePair batchedKeyValuePair= (BatchedKeyValuePair) event;
                    keyName=Strings.toString(batchedKeyValuePair.getKey());
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
                log.error("[{}]抛弃key:{} ,class:[{}]:原因[{}]",taskId, keyName,event.getClass().toString(),e.getMessage());
                DataCleanUtils.cleanData(keyValueEventEntity,event);
                e.printStackTrace();
            }
    }
}
