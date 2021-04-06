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

package syncer.transmission.strategy.commandprocessing;

import lombok.extern.slf4j.Slf4j;
import syncer.common.util.ThreadPoolUtils;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.replication.Replication;
import syncer.replica.util.RedisBranchTypeEnum;
import syncer.replica.util.TaskRunTypeEnum;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.RedisClientFactory;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.compensator.ISyncerCompensatorFactory;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.queue.SyncerQueue;
import syncer.transmission.queue.impl.LocalMemoryQueue;
import syncer.transmission.strategy.commandprocessing.impl.*;
import syncer.transmission.task.SendCommandTask;
import syncer.transmission.util.hash.HashUtils;
import syncer.transmission.util.kv.KVUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MultiQueueFilter implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private volatile boolean status=true;
    private RedisBranchTypeEnum branchTypeEnum;
    private double redisVersion;
    private String type;
    private Replication replication;
    private String taskId;
    private int batchSize;
    private volatile Map<Integer, SyncerQueue<KeyValueEventEntity>> queueMap = new ConcurrentHashMap<>();

    private volatile Map<Integer, ISyncerCompensator> iSyncerCompensatorMap = new ConcurrentHashMap<>();
    private final Integer QUEUE_SIZE=1;


    public MultiQueueFilter(RedisBranchTypeEnum branchTypeEnum, double redisVersion, String type, Replication replication, String taskId, int batchSize) {
        this.branchTypeEnum = branchTypeEnum;
        this.redisVersion = redisVersion;
        this.type = type;
        this.replication = replication;
        this.taskId = taskId;
        this.batchSize=batchSize;
        for (int i = 0; i < QUEUE_SIZE; i++) {
            //Configuration sourceCon = Configuration.valueOf(turi);
//            RedisClient client = RedisClientFactory.createRedisClient(branchTypeEnum, syncDataDto.getTargetHost(), syncDataDto.getTargetPort(), syncDataDto.getTargetPassword(), batchSize,syncDataDto.getErrorCount(), taskId,null,null);
            //JDRedisClient client=new JDRedisJedisPipeLineClient(turi.getHost(),turi.getPort(),sourceCon.getAuthPassword(),batchSize,taskId);
            List<CommonProcessingStrategy> commonFilterList = new ArrayList<>();
            //根据type生成相对节点List [List顺序即为filter节点执行顺序]
            assemble_the_list(commonFilterList, type, taskId, redisVersion, client);
            SyncerQueue<KeyValueEventEntity> queue = new LocalMemoryQueue<>(taskId, i);
            queueMap.put(i, queue);
            ISyncerCompensator syncerCompensator= ISyncerCompensatorFactory.createRedisClient(branchTypeEnum,taskId,client);
            iSyncerCompensatorMap.put(i,syncerCompensator);
            ThreadPoolUtils.exec(SendCommandTask
                    .builder()
                    .replication(replication)
                    .filterChain(ProcessingRunStrategyChain.builder().commonFilterList(commonFilterList).build())
                    .taskId(taskId)
                    .queue(queue)
                    .syncerCompensator(syncerCompensator)
                    .build());
        }

    }

    @Override
    public void run(Replication replicator, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        try {
        Event event=eventEntity.getEvent();
        KeyValueEventEntity node=eventEntity;
        if (event instanceof DefaultCommand) {
            try {
                DefaultCommand defaultCommand = (DefaultCommand) event;
                if(Arrays.equals(defaultCommand.getCommand(),"SELECT".getBytes())
                        ||Arrays.equals(defaultCommand.getCommand(),"FLUSHALL".getBytes())
                        ||Arrays.equals(defaultCommand.getCommand(),"FLUSHDB".getBytes())) {
                    for (Map.Entry<Integer,SyncerQueue<KeyValueEventEntity>>queue:queueMap.entrySet()
                    ) {
                        queue.getValue().put(node);
                    }
                }else {
                    String key = KVUtils.getKey(event);
                    queueMap.get(HashUtils.getHash(key, QUEUE_SIZE)).put(node);
                }
            } catch (InterruptedException e) {
                log.warn("【{}】中的key[{}]加入队列失败", taskId, KVUtils.getKey(event));
            }
        } else {
            String key = KVUtils.getKey(event);
            try {
                queueMap.get(HashUtils.getHash(key, QUEUE_SIZE)).put(node);
            } catch (InterruptedException e) {
                log.warn("【{}】中的key[{}]加入队列失败", taskId, KVUtils.getKey(event));
            }
        }

        }catch (Exception e){
            throw new StartegyNodeException(e.getMessage()+"->MultiQueueFilter",e.getCause());
        }
    }

    @Override
    public void toNext(Replication replicator, KeyValueEventEntity eventEntity,TaskModel taskModel) throws StartegyNodeException {
        if(null!=next){
            next.run(replicator,eventEntity,taskModel);
        }
    }

    @Override
    public void setNext(CommonProcessingStrategy nextFilter) {
        this.next=nextFilter;
    }

    /**
     * 按照Type组装List节点
     *
     * @param commonFilterList
     * @param type
     * @param taskId
     * @param redisVersion
     * @param client
     */
    public void assemble_the_list(List<CommonProcessingStrategy> commonFilterList, String type, String taskId, double redisVersion, RedisClient client) {
        //全量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.STOCKONLY)) {
            commonFilterList.add(CommandProcessingTimeCalculationStrategy.builder().taskId(taskId).client(client).build());
            commonFilterList.add(CommandProcessingDataAnalysisStrategy.builder().taskId(taskId).client(client).build());
            commonFilterList.add(CommandProcessingDbMappingStrategy.builder().taskId(taskId).client(client).build());
            commonFilterList.add(CommandProcessingRdbCommandSendStrategy.builder().taskId(taskId).client(client).redisVersion(redisVersion).build());
        }

        //增量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.INCREMENTONLY)) {
            commonFilterList.add(CommandProcessingDataAnalysisStrategy.builder().taskId(taskId).client(client).build());
            commonFilterList.add(CommandProcessingDbMappingStrategy.builder().taskId(taskId).client(client).build());
            commonFilterList.add(CommandProcessingAofCommandSendStrategy.builder().taskId(taskId).client(client).build());
        }


        //全量+增量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.TOTAL)) {
            commonFilterList.add(CommandProcessingTimeCalculationStrategy.builder().taskId(taskId).client(client).build());
            commonFilterList.add(CommandProcessingDataAnalysisStrategy.builder().taskId(taskId).client(client).build());
            commonFilterList.add(CommandProcessingDbMappingStrategy.builder().taskId(taskId).client(client).build());
            commonFilterList.add(CommandProcessingRdbCommandSendStrategy.builder().taskId(taskId).client(client).redisVersion(redisVersion).build());
            commonFilterList.add(CommandProcessingAofCommandSendStrategy.builder().taskId(taskId).client(client).build());
        }


    }


}
