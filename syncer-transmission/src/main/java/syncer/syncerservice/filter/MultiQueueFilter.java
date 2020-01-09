package syncer.syncerservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.RedisBranchTypeEnum;
import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.entity.dto.RedisSyncDataDto;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.compensator.ISyncerCompensator;
import syncer.syncerservice.compensator.ISyncerCompensatorFactory;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.sync.SendCommandTask;
import syncer.syncerservice.util.HashUtils;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisClientFactory;
import syncer.syncerservice.util.KVUtils;
import syncer.syncerservice.util.queue.DbMapLocalDiskMemoryQueue;
import syncer.syncerservice.util.queue.LocalDiskMemoryQueue;
import syncer.syncerservice.util.queue.LocalMemoryQueue;
import syncer.syncerservice.util.queue.SyncerQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MultiQueueFilter implements CommonFilter {
    private CommonFilter next;
    private JDRedisClient client;
    private volatile boolean status=true;
    private RedisBranchTypeEnum branchTypeEnum;
    private RedisSyncDataDto syncDataDto;
    private String type;
    private Replicator r;
    private String taskId;
    private int batchSize;
    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private volatile Map<Integer, SyncerQueue<KeyValueEventEntity>> queueMap = new ConcurrentHashMap<>();

    private volatile Map<Integer, ISyncerCompensator> iSyncerCompensatorMap = new ConcurrentHashMap<>();
    private final Integer QUEUE_SIZE=1;
    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }

    public MultiQueueFilter(RedisBranchTypeEnum branchTypeEnum, RedisSyncDataDto syncDataDto, String type, Replicator r, String taskId,int batchSize) {
        this.branchTypeEnum = branchTypeEnum;
        this.syncDataDto = syncDataDto;
        this.type = type;
        this.r = r;
        this.taskId = taskId;
        this.batchSize=batchSize;
        for (int i = 0; i < QUEUE_SIZE; i++) {
            //Configuration sourceCon = Configuration.valueOf(turi);
            JDRedisClient client = JDRedisClientFactory.createJDRedisClient(branchTypeEnum, syncDataDto.getTargetHost(), syncDataDto.getTargetPort(), syncDataDto.getTargetPassword(), batchSize, taskId);
            //JDRedisClient client=new JDRedisJedisPipeLineClient(turi.getHost(),turi.getPort(),sourceCon.getAuthPassword(),batchSize,taskId);

            List<CommonFilter> commonFilterList = new ArrayList<>();

            //根据type生成相对节点List [List顺序即为filter节点执行顺序]
            assemble_the_list(commonFilterList, type, taskId, syncDataDto, client);

//            KeyValueRunFilterChain filterChain = KeyValueRunFilterChain.builder().commonFilterList(commonFilterList).build();

            SyncerQueue<KeyValueEventEntity> queue = new LocalMemoryQueue<>(taskId, i);
//            SyncerQueue<KeyValueEventEntity> queue = new DbMapLocalDiskMemoryQueue<>(taskId, i);
//            SyncerQueue<KeyValueEventEntity> queue = new LocalDiskMemoryQueue<>(taskId, i);
            queueMap.put(i, queue);
            ISyncerCompensator syncerCompensator=ISyncerCompensatorFactory.createJDRedisClient(branchTypeEnum,taskId,client);
            iSyncerCompensatorMap.put(i,syncerCompensator);
            threadPoolTaskExecutor.execute(SendCommandTask
                    .builder()
                    .r(r)
                    .filterChain(KeyValueRunFilterChain.builder().commonFilterList(commonFilterList).build())
                    .taskId(taskId)
                    .queue(queue)
                    .syncerCompensator(syncerCompensator)
                    .build());
        }

    }

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) {
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
//                    queueMap.get(0).put(node);
                    queueMap.get(HashUtils.getHash(key, QUEUE_SIZE)).put(node);
                }
//                queueMap.get(0).put(node);
            } catch (InterruptedException e) {
                log.warn("【{}】中的key[{}]加入队列失败", taskId, KVUtils.getKey(event));
            }
        } else {
            String key = KVUtils.getKey(event);
            try {
//                queueMap.get(0).put(node);
                queueMap.get(HashUtils.getHash(key, QUEUE_SIZE)).put(node);
//                System.out.println("加入队列：" + queueMap.get(HashUtils.getHash(key, 3)).size());
            } catch (InterruptedException e) {
                log.warn("【{}】中的key[{}]加入队列失败", taskId, KVUtils.getKey(event));
            }
        }
    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) {
        if(null!=next){
            next.run(replicator,eventEntity);
        }
    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }

    /**
     * 按照Type组装List节点
     *
     * @param commonFilterList
     * @param type
     * @param taskId
     * @param syncDataDto
     * @param client
     */
    public void assemble_the_list(List<CommonFilter> commonFilterList, String type, String taskId, RedisSyncDataDto syncDataDto, JDRedisClient client) {
        //全量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.STOCKONLY)) {
            commonFilterList.add(KeyValueTimeCalculationFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueDataAnalysisFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueEventDBMappingFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueRdbSyncEventFilter.builder().taskId(taskId).client(client).redisVersion(syncDataDto.getRedisVersion()).build());
        }

        //增量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.INCREMENTONLY)) {
            commonFilterList.add(KeyValueEventDBMappingFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueCommandSyncEventFilter.builder().taskId(taskId).client(client).build());
        }


        //全量+增量
        if (TaskRunTypeEnum.valueOf(type.trim().toUpperCase()).equals(TaskRunTypeEnum.TOTAL)) {
            commonFilterList.add(KeyValueTimeCalculationFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueDataAnalysisFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueEventDBMappingFilter.builder().taskId(taskId).client(client).build());
            commonFilterList.add(KeyValueRdbSyncEventFilter.builder().taskId(taskId).client(client).redisVersion(syncDataDto.getRedisVersion()).build());
            commonFilterList.add(KeyValueCommandSyncEventFilter.builder().taskId(taskId).client(client).build());
        }


    }


}
