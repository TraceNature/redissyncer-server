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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.common.util.TimeUtils;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.iter.datatype.*;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.replication.Replication;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.type.KvDataType;
import syncer.transmission.client.RedisClient;
import syncer.transmission.constants.RedisDataTypeAnalysisConstant;
import syncer.transmission.entity.SqliteCommitEntity;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.model.BigKeyModel;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.queue.DbDataCommitQueue;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 命令分析节点
 * @Date 2020/12/22
 */
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandProcessingDataAnalysisStrategy implements CommonProcessingStrategy {
    private CommonProcessingStrategy next;
    private RedisClient client;
    private String taskId;
    @Builder.Default
    private Map<String, Long> analysisMap = new ConcurrentHashMap<>();
    private TaskModel taskModel;
    public CommandProcessingDataAnalysisStrategy(CommonProcessingStrategy next, RedisClient client, String taskId,TaskModel taskModel) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.taskModel=taskModel;
        this.analysisMap = new ConcurrentHashMap<>();
    }

    @Override
    public void run(Replication replication, KeyValueEventEntity eventEntity, TaskModel taskModel) throws StartegyNodeException {
        try {
            Event event = eventEntity.getEvent();
            TaskDataEntity dataEntity = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId);

            //记录任务最后一次update时间
            try {
                if (event instanceof DefaultCommand || event instanceof DumpKeyValuePairEvent || event instanceof BatchedKeyValuePairEvent<?, ?>) {
                    dataEntity.getTaskModel().setLastKeyUpdateTime(System.currentTimeMillis());
                }


            } catch (Exception e) {
                log.error("[{}] update last key update time error", taskId);
            }
            //全量同步结束
            if (event instanceof PostRdbSyncEvent) {
                try {

                    if (analysisMap.containsKey(KvDataType.FRAGMENTATION_NUM.toString())) {
                        Long keyValueNum=analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM)==null?0L:analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM);
                        Long fragmentationNum=analysisMap.get(KvDataType.FRAGMENTATION_NUM.toString())==null?0L:analysisMap.get(KvDataType.FRAGMENTATION_NUM.toString());
                        Long fragmentation=analysisMap.get(KvDataType.FRAGMENTATION.toString())==null?0L:analysisMap.get(KvDataType.FRAGMENTATION.toString());
                        Long result=keyValueNum+fragmentationNum-fragmentation;
                        analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM, result);
                    }else {
                        analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM,analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM)==null?0L:analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM));
                    }

                    analysisMap.put("time", TimeUtils.getNowTimeMills());
                    SqlOPUtils.updateDataAnalysis(taskId, JSON.toJSONString(analysisMap));
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("全量数据分析报告入库失败");
                }


                log.warn("[{}]全量数据结构分析报告：\r\n{}", taskId, JSON.toJSONString(analysisMap, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                        SerializerFeature.WriteDateUseDateFormat));
            }
            if (event instanceof DumpKeyValuePairEvent) {
                dataEntity.getAllKeyCount().incrementAndGet();
                DumpKeyValuePairEvent dumpKeyValuePair = (DumpKeyValuePairEvent) event;
                addAnalysisMap(dumpKeyValuePair.getDataType());
            }

            if (event instanceof BatchedKeyValuePairEvent<?, ?>) {
                BatchedKeyValuePairEvent batchedKeyValuePair = (BatchedKeyValuePairEvent) event;
                if (batchedKeyValuePair.isLast()) {
                    addAnalysisMap(batchedKeyValuePair.getDataType());
                }

                if (batchedKeyValuePair.getBatch() == 1) {
                    dataEntity.getAllKeyCount().incrementAndGet();
                }
                //计算分片数量
                if (batchedKeyValuePair.getBatch() == 0) {

                    if (!StringUtils.isEmpty(batchedKeyValuePair.getKey())) {
                        try {

                            BigKeyModel bigKeyModel = BigKeyModel
                                    .builder()
                                    .command(Strings.toString(batchedKeyValuePair.getKey()))
                                    .taskId(taskId)
                                    .command_type(String.valueOf(batchedKeyValuePair.getDataType()))
                                    .build();
                            DbDataCommitQueue.put(SqliteCommitEntity.builder().type(10).object(bigKeyModel).msg("bigKeyInsert").build());
//                        BigKeyMapper bigKeyMapper=SpringUtil.getBean(BigKeyMapper.class);
//                        bigKeyMapper.insertBigKeyCommandModel(BigKeyModel
//                                .builder()
//                                .command(Strings.toString(batchedKeyValuePair.getKey()))
//                                .taskId(taskId)
//                                .command_type(String.valueOf(batchedKeyValuePair.getDataType()))
//                                .build());
                        } catch (Exception e) {
                            log.error("大key统计入库失败：[{}]", Strings.toString(batchedKeyValuePair.getKey()));
                        }
                        log.warn("大key统计：[{}],db: [{}]", Strings.toString(batchedKeyValuePair.getKey()), batchedKeyValuePair.getDb().getCurrentDbNumber());
                    }

                    addAnalysisMap(KvDataType.FRAGMENTATION);
                    addAnalysisMap(KvDataType.FRAGMENTATION_NUM);
                } else {

                    addAnalysisMap(KvDataType.FRAGMENTATION_NUM);
                }


            }

            //增量数据
            if (event instanceof DefaultCommand) {
                dataEntity.getAllKeyCount().incrementAndGet();
            }

            if(event instanceof BatchedKeyStringValueStringEvent){
                BatchedKeyStringValueStringEvent stringEvent= (BatchedKeyStringValueStringEvent) event;
                addAnalysisMap(stringEvent.getDataType());
            }

            if(event instanceof BatchedKeyStringValueHashEvent) {
                BatchedKeyStringValueHashEvent hashEvent= (BatchedKeyStringValueHashEvent) event;
                addAnalysisMap(hashEvent.getDataType());
            }

            if (event instanceof BatchedKeyStringValueSetEvent) {
                BatchedKeyStringValueSetEvent setEvent= (BatchedKeyStringValueSetEvent) event;
                addAnalysisMap(setEvent.getDataType());
            }

            if (event instanceof BatchedKeyStringValueListEvent) {
                BatchedKeyStringValueListEvent listEvent= (BatchedKeyStringValueListEvent) event;
                addAnalysisMap(listEvent.getDataType());
            }

            if (event instanceof BatchedKeyStringValueZSetEvent) {
                BatchedKeyStringValueZSetEvent  zSetEvent= (BatchedKeyStringValueZSetEvent) event;
                addAnalysisMap(zSetEvent.getDataType());
            }
            if (event instanceof BatchedKeyStringValueModuleEvent) {
                BatchedKeyStringValueModuleEvent moduleEvent= (BatchedKeyStringValueModuleEvent) event;
                addAnalysisMap(moduleEvent.getDataType());
            }

            if (event instanceof BatchedKeyStringValueStreamEvent) {
                BatchedKeyStringValueStreamEvent streamEvent= (BatchedKeyStringValueStreamEvent) event;
                addAnalysisMap(streamEvent.getDataType());
            }


            toNext(replication, eventEntity,taskModel);
        } catch (Exception e) {
            if (analysisMap.containsKey(RedisDataTypeAnalysisConstant.EROR_KEY)) {
                analysisMap.put(RedisDataTypeAnalysisConstant.EROR_KEY, analysisMap.get(RedisDataTypeAnalysisConstant.EROR_KEY) + 1);
            } else {
                analysisMap.put(RedisDataTypeAnalysisConstant.EROR_KEY, 1L);
            }

            throw new StartegyNodeException(e.getMessage() + "->DataAnalysisStrategy", e.getCause());
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

    void addAnalysisMap(KvDataType dataType) {
        if (Objects.isNull(dataType)) {
            return;
        }

        try {
            if (analysisMap.containsKey(dataType.toString())) {
                analysisMap.put(dataType.toString(), analysisMap.get(dataType.toString()) + 1);

            } else {
                analysisMap.put(dataType.toString(), 1L);
            }


//            if(analysisMap.containsKey(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM)){
//                analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM,analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM)+1);
//            }else {
//                analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM,1L);
//            }


            if (analysisMap.containsKey(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM)) {
                if (!dataType.equals(KvDataType.FRAGMENTATION) && !dataType.equals(KvDataType.FRAGMENTATION_NUM)) {
                    analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM, analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM) + 1);
                }
            } else {
                if (!dataType.equals(KvDataType.FRAGMENTATION) && !dataType.equals(KvDataType.FRAGMENTATION_NUM)) {
                    analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM, 1L);
                }

            }
        } catch (Exception e) {
            log.warn("数据分析节点数据计算出现错误[不影响下个节点数据迁移]{}", e.getMessage() + dataType);
        }

    }



}
