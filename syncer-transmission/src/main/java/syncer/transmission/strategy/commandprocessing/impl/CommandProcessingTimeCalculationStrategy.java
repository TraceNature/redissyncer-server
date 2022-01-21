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
import syncer.replica.event.Event;
import syncer.replica.event.iter.datatype.*;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.transmission.client.RedisClient;
import syncer.transmission.exception.KeyWeed0utException;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;

import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 全量kv剩余过期时间计算节点
 * @Date 2020/12/22
 */
@Builder
@Getter
@Setter
@Slf4j
public class CommandProcessingTimeCalculationStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;
    private TaskModel taskModel;

    public CommandProcessingTimeCalculationStrategy(CommonProcessingStrategy next, RedisClient client, String taskId,TaskModel taskModel) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.taskModel=taskModel;
    }

    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        try{
            Event event=eventEntity.getEvent();
            if (event instanceof DumpKeyValuePairEvent) {
                DumpKeyValuePairEvent dumpKeyValuePair= (DumpKeyValuePairEvent) event;
                Long time=dumpKeyValuePair.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }


            if (event instanceof BatchedKeyValuePairEvent<?, ?>) {
                BatchedKeyValuePairEvent batchedKeyValuePair = (BatchedKeyValuePairEvent) event;
                if(batchedKeyValuePair.getBatch()==0&&null==batchedKeyValuePair.getValue()){
                    return;
                }
                Long time=batchedKeyValuePair.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }


            if(event instanceof BatchedKeyStringValueStringEvent){
                BatchedKeyStringValueStringEvent stringEvent= (BatchedKeyStringValueStringEvent) event;
                if(stringEvent.getBatch()==0&&null==stringEvent.getValue()){
                    return;
                }
                Long time=stringEvent.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }

            if(event instanceof BatchedKeyStringValueHashEvent) {
                BatchedKeyStringValueHashEvent hashEvent= (BatchedKeyStringValueHashEvent) event;
                if(hashEvent.getBatch()==0&&null==hashEvent.getValue()){
                    return;
                }
                Long time=hashEvent.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }

            if (event instanceof BatchedKeyStringValueSetEvent) {
                BatchedKeyStringValueSetEvent setEvent= (BatchedKeyStringValueSetEvent) event;
                if(setEvent.getBatch()==0&&null==setEvent.getValue()){
                    return;
                }
                Long time=setEvent.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }

            if (event instanceof BatchedKeyStringValueListEvent) {
                BatchedKeyStringValueListEvent listEvent= (BatchedKeyStringValueListEvent) event;
                if(listEvent.getBatch()==0&&null==listEvent.getValue()){
                    return;
                }
                Long time=listEvent.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }

            if (event instanceof BatchedKeyStringValueZSetEvent) {
                BatchedKeyStringValueZSetEvent  zSetEvent= (BatchedKeyStringValueZSetEvent) event;
                if(zSetEvent.getBatch()==0&&null==zSetEvent.getValue()){
                    return;
                }
                Long time=zSetEvent.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }
            if (event instanceof BatchedKeyStringValueModuleEvent) {
                BatchedKeyStringValueModuleEvent moduleEvent= (BatchedKeyStringValueModuleEvent) event;
                if(moduleEvent.getBatch()==0&&null==moduleEvent.getValue()){
                    return;
                }
                Long time=moduleEvent.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }

            if (event instanceof BatchedKeyStringValueStreamEvent) {
                BatchedKeyStringValueStreamEvent streamEvent= (BatchedKeyStringValueStreamEvent) event;
                if(streamEvent.getBatch()==0&&null==streamEvent.getValue()){
                    return;
                }
                Long time=streamEvent.getExpiredMs();
                try {
                    timeCalculation(eventEntity,time);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }


            //继续执行下一Filter节点
            toNext(replication,eventEntity,taskModel);
        }catch (Exception e){
            throw new StartegyNodeException(e.getMessage()+"->CommandProcessingTimeCalculationStrategy",e.getCause());
        }
    }

    @Override
    public void toNext(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        if(null!=next){
            next.run(replication,eventEntity,taskModel);
        }
    }

    @Override
    public void setNext(CommonProcessingStrategy nextStrategy) {
        this.next=nextStrategy;
    }

    void timeCalculation(KeyValueEventEntity eventEntity,Long time) throws KeyWeed0utException {
        Long ms=0L;
        if (Objects.isNull(time)) {
            ms = 0L;
        } else {
            ms = time - System.currentTimeMillis();

            //ttl校准
            if(Objects.nonNull(taskModel)&&Objects.nonNull(taskModel.getTimeDeviation())&&taskModel.getTimeDeviation()!=0L){
                if(ms<=0L){
                    if(taskModel.getTimeDeviation()>0L){
                        ms=ms+taskModel.getTimeDeviation();
                    }
                }
                if(ms>0L){
                    ms=ms+taskModel.getTimeDeviation();
                }
            }

            if(ms<0L){
                //key已经过期 忽略本key
                throw new KeyWeed0utException("key过期被抛弃");
            }
        }
        eventEntity.setMs(ms);
    }
}
