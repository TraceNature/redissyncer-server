package syncer.transmission.task;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import syncer.jedis.Protocol;
import syncer.replica.config.RedisURI;
import syncer.replica.constant.RedisType;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.Event;
import syncer.replica.event.iter.datatype.BatchedKeyStringValueHashEvent;
import syncer.replica.event.iter.datatype.BatchedKeyStringValueListEvent;
import syncer.replica.event.iter.datatype.BatchedKeyStringValueSetEvent;
import syncer.replica.event.iter.datatype.BatchedKeyStringValueStringEvent;
import syncer.replica.event.iter.datatype.BatchedKeyStringValueZSetEvent;
import syncer.replica.event.iter.datatype.BatchedKeyValuePairEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.event.start.PreRdbSyncEvent;
import syncer.replica.listener.EventListener;
import syncer.replica.parser.syncer.DumpRdbParser;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.register.DefaultCommandRegister;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.replica.util.SyncTypeUtils;
import syncer.replica.util.strings.Strings;
import syncer.transmission.checkpoint.breakpoint.BreakPoint;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.RedisClientFactory;
import syncer.transmission.constants.RedisCommandTypeEnum;
import syncer.transmission.entity.OffSetEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.task.circle.MultiSyncCircle;
import syncer.transmission.util.RedisCommandTypeUtils;
import syncer.transmission.util.SyncerTaskTypeUtils;
import syncer.transmission.util.redis.RedisVersionUtil;
import syncer.transmission.util.strings.StringUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

/**
 * 双向复制任务， 基于辅助key破环
 * 
 * @author susongyan
 **/
@Slf4j
public class RedisSyncFilterByAuxKeyTransmissionTask implements Runnable {
    private TaskModel taskModel;
    private String sourceNodeId;
    private String targetNodeId;
    private MultiSyncCircle circle;
    RedisClient client;
    private int db;
    private boolean nodeStatus = false;
    RedisVersionUtil redisVersionUtil;
    BreakPoint breakPoint;

    public RedisSyncFilterByAuxKeyTransmissionTask(TaskModel taskModel, MultiSyncCircle circle) {
        this.taskModel = taskModel;
        this.targetNodeId = taskModel.getTargetNodeId();
        this.sourceNodeId = taskModel.getNodeId();
        this.circle = circle;
        this.breakPoint = new BreakPoint();
        //        client = new MulitJedisPipeLineClient(multiTaskModel.getHost(), multiTaskModel.getPort(), multiTaskModel.getPassword(), 50, 1, multiTaskModel.getTaskId(), multiTaskModel.getNodeId(), multiTaskModel.getParentId(), circle.getNodeSuccessStatus(), circle.getNodeSuccessStatusType());

        //TODO 先处理 redis-cluster， 其他的再考虑
        RedisType redisType = SyncTypeUtils.getRedisType(taskModel.getTargetRedisType());
        client = RedisClientFactory.createRedisClient(RedisType.CLUSTER, taskModel.getTargetRedisAddress(),
                taskModel.getTargetPort(), taskModel.getTargetPassword(), "", 0, 0, taskModel.getErrorCount(),
                taskModel.getTaskId(), null, null);

        this.db = -1;
        this.redisVersionUtil = new RedisVersionUtil();
    }

    @Override
    public void run() {
        try {
            //TODO circleId save db
            String globalTaskId = SyncerTaskTypeUtils.globalTaskId(taskModel.getTaskId(), taskModel.getGroupId(),
                    taskModel.getNodeId());

            Replication replication = null;
            RedisURI suri = new RedisURI(taskModel.getSourceUri());
            replication = new RedisReplication(suri, true);
            replication.getConfig().setTaskId(taskModel.getTaskId());
            int rdbVersion = redisVersionUtil.getRdbVersionByRedisVersion(taskModel.getSourceRedisAddress(),
                    String.valueOf(taskModel.getRedisVersion()));
            //注册增量命令解析器
            final Replication replicationHandler = DefaultCommandRegister.addCommandParser(replication);
            replicationHandler.getConfig().setTaskId(taskModel.getTaskId());
            //注册RDB全量解析器
            // replicationHandler
            //         .setRdbParser(new ValueDumpIterableRdbParser(replicationHandler, taskModel.getRdbVersion()));

            replicationHandler
                    .setRdbParser(new DumpRdbParser(replicationHandler, taskModel.getRdbVersion()));
            //断点续传
            setOffset(replicationHandler);

            //任务进度管理
            SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId()).setReplication(replicationHandler);

            //事件监听
            replicationHandler
                .addEventListener(new EventListener() {
                    // 大key 拆分
                    // .addEventListener(new ValueDumpIterableEventListener(taskModel.getBatchSize(), new EventListener() {
                        @Override
                        public void onEvent(Replication replicator, Event event) {
                            log.debug("event " + event);
                            if (event instanceof PreRdbSyncEvent) {
                                log.info("[{}]全量开始...", taskModel.getSourceRedisAddress());
                                waitLoadingNode();
                            }

                            //增量同步开始
                            if (event instanceof PreCommandSyncEvent) {
                                log.info("[{}]增量开始...", taskModel.getSourceRedisAddress());
                                waitLoadingNode();
                            }

                            if (!circle.getNodeSuccessStatus().get() && circle.getNodeSuccessStatusType().get() != 0) {
                                try {
                                    circle.getNodeSuccessStatusType().set(-1);
                                    replicationHandler.close();
                                    log.info("[{}]节点关闭...", sourceNodeId);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            // 增量命令
                            if (event instanceof DefaultCommand) {
                                DefaultCommand command = (DefaultCommand) event;
                                String key = Strings.byteToString(command.getArgs())[0];

                                String stringComand = Strings.byteToString(command.getCommand()).trim();
                                log.info("replicate... command: [{}] key: [{}]", stringComand, key);
                                //屏蔽辅助key DEL命令
                                if (circle.isCircleKey(command, sourceNodeId)
                                        || circle.isCircleKey(command, targetNodeId)) {
                                    // psetex 辅助key 过期后会产生del命令
                                    if ("DEL".equalsIgnoreCase(Strings.byteToString(command.getCommand()).trim())) {
                                        log.info("屏蔽circle aux key [{}]的 del 操作", key);
                                        return;
                                    }

                                }
                                //屏蔽flushall和flushdb
                                if (stringComand.equalsIgnoreCase(Protocol.Command.FLUSHALL.toString())
                                        || stringComand.equalsIgnoreCase(Protocol.Command.FLUSHDB.toString())) {
                                    log.warn("[{}]-[{}]-[{}]已屏蔽[{}]命令", taskModel.getTaskId(), taskModel.getGroupId(),
                                            sourceNodeId, stringComand);
                                    return;
                                }
                                //非自身辅助key, 说明是反向任务同步过来的, 先把 circle-key 缓存一下 用来后面做过滤用
                                if (circle.isCircleKey(command, targetNodeId)) {
                                    String md5 = Strings.byteToString(command.getArgs()[0]);
                                    circle.addDataMap(sourceNodeId, md5);
                                    log.info("缓存 circle aux key [{}]", key);
                                    return;
                                }

                                if (!circle.isCircleKey(command, targetNodeId)
                                        && !circle.isCircleKey(command, sourceNodeId)) {
                                    String md5 = circle.getMd5(command, targetNodeId);
                                    if (stringComand.equalsIgnoreCase(Protocol.Command.SELECT.toString())) {
                                        db = Integer.valueOf(Strings.byteToString(command.getArgs()[0]).trim());
                                        return;
                                    }

                                    // 如果是其他节点写过来的命令，那么在辅助key缓存里边应该有，移除缓存，不再复制过去
                                    if (circle.getDataMap(sourceNodeId).containsKey(md5)) {
                                        log.info("从dataMap移除 circle aux key [{}]", md5);
                                        circle.removeDataMap(sourceNodeId, md5);
                                        return;
                                    }

                                    if (db != -1) {
                                        byte[][] selectNum = new byte[][] { String.valueOf(db).getBytes() };
                                        DefaultCommand selectComand = new DefaultCommand(
                                                Protocol.Command.SELECT.getRaw(), selectNum);
                                        String[] data = new String[] { circle.getMd5(selectComand, sourceNodeId), "1", "1" };
                                        byte[][] ndata = new byte[][] { data[0].getBytes(), data[1].getBytes(), data[2].getBytes() };
                                        try {
                                            client.send("PSETEX".getBytes(), ndata);
                                            client.send(selectComand.getCommand(), selectComand.getArgs());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        db = -1;
                                    }

                                    String[] data = new String[] { circle.getMd5(command, sourceNodeId), "1", "1" };
                                    byte[][] ndata = new byte[][] { data[0].getBytes(), data[1].getBytes(),
                                            data[2].getBytes() };
                                    try {
                                        log.info("PSETEX aux key [{}]", data[0]);
                                        client.send("PSETEX".getBytes(), ndata);
                                        client.send(command.getCommand(), command.getArgs());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }

                            //全量 RESTORE
                            if (event instanceof DumpKeyValuePairEvent) {
                                DumpKeyValuePairEvent valueDump = (DumpKeyValuePairEvent) event;
                                Long ms = valueDump.getExpiredMs();
                                RedisDB vdb = valueDump.getDb();
                                Long dbNum = vdb.getCurrentDbNumber();
                                db = dbNum.intValue();
                                long ttl = (ms == null || ms < 0) ? 0 : ms;
                                String rdbDumpMd5 = circle.getRdbDumpMd5(valueDump, sourceNodeId, 3.0);
                                String dumpKey = Strings.byteToString(valueDump.getKey());
                                String[] data = new String[] { rdbDumpMd5, "1", "1" };
                                byte[][] ndata = new byte[][] { data[0].getBytes(), data[1].getBytes(), data[2].getBytes() };
                                try {
                                    log.info("restore...PSETEX aux key [{}]", rdbDumpMd5);
                                    client.send("PSETEX".getBytes(), ndata);
                                } catch (Exception e) {
                                    log.error("restore key:[" + dumpKey + "] fail", e);
                                    e.printStackTrace();
                                }
                                client.restoreReplace(dbNum, valueDump.getKey(), ttl, valueDump.getValue(), true);
                            }
                        }

                        @Override
                        public String eventListenerName() {
                            return "redis-circle-task-listener";
                        }
                    });
            replicationHandler.open();

        } catch (Exception e) {
            log.error("circle sync by auxiliary key start error , {}", e.getMessage());
        }
    }

    /**
     * 设置上次同步进度 offset 
     * 
     * @param replicationHandler
     */
    private void setOffset(Replication replicationHandler) {
        OffSetEntity offset = null;
        offset = breakPoint.checkPointOffset(taskModel);

        /** old version
        
         TaskDataEntity taskDataEntity=SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId());
         if(Objects.nonNull(taskDataEntity)){
         offset = taskDataEntity.getOffSetEntity();
         }
         */

        if (offset == null) {
            offset = new OffSetEntity();
            SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getId()).setOffSetEntity(offset);
        } else {
            if (StringUtils.isEmpty(offset.getReplId())) {
                offset.setReplId(replicationHandler.getConfig().getReplId());
            } else if (offset.getReplOffset().get() > -1) {
                if (!taskModel.isAfresh()) {
                    replicationHandler.getConfig().setReplOffset(offset.getReplOffset().get());
                    replicationHandler.getConfig().setReplId(offset.getReplId());
                }
            }
        }
    }

    /**
     * 大Key拆分数据
     */
    //TODO 是否需要切？如果切的话list、set等操作需要先清空目标集群的key, del 大key本身也会卡住 不如也用 dump + restore 的方式重建key
    private void processBigKey(Replication replicator, Event event) {
        if (event instanceof BatchedKeyValuePairEvent<?, ?>) {
            BatchedKeyValuePairEvent batchedKeyValuePair = (BatchedKeyValuePairEvent) event;
            RedisDB db = batchedKeyValuePair.getDb();
            Long duNum = db.getCurrentDbNumber();
            Long ms = batchedKeyValuePair.getExpiredMs();
            RedisCommandTypeEnum typeEnum = RedisCommandTypeUtils
                    .getRedisCommandTypeEnum(batchedKeyValuePair.getValueRdbType());
            if (batchedKeyValuePair.getBatch() == 0 && null == batchedKeyValuePair.getValue()) {
                return;
            }

            //String类型
            if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                BatchedKeyStringValueStringEvent valueString = (BatchedKeyStringValueStringEvent) event;
                if (ms == null || ms <= 0L) {
                    client.updateLastReplidAndOffset(replicator.getConfig().getReplId(),
                            replicator.getConfig().getReplOffset());
                    Long res = client.append(duNum, valueString.getKey(), valueString.getValue());
                    // iSyncerCompensator.append(duNum,valueString.getKey(), valueString.getValue(),res);
                } else {
                    Long res = client.append(duNum, valueString.getKey(), valueString.getValue());
                    // iSyncerCompensator.append(duNum,valueString.getKey(), valueString.getValue(),res);
                }
            } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                client.updateLastReplidAndOffset(replicator.getConfig().getReplId(),
                        replicator.getConfig().getReplOffset());
                //list类型
                BatchedKeyStringValueListEvent valueList = (BatchedKeyStringValueListEvent) event;
                if (ms == null || ms <= 0L) {
                    Long res = client.rpush(duNum, valueList.getKey(), valueList.getValue());
                    // iSyncerCompensator.rpush(duNum,valueList.getKey(), valueList.getValue(),res);
                } else {
                    Long res = client.rpush(duNum, valueList.getKey(), ms, valueList.getValue());
                    // iSyncerCompensator.rpush(duNum,valueList.getKey(), ms,valueList.getValue(),res);
                }
            } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {
                client.updateLastReplidAndOffset(replicator.getConfig().getReplId(),
                        replicator.getConfig().getReplOffset());
                //set类型
                BatchedKeyStringValueSetEvent valueSet = (BatchedKeyStringValueSetEvent) event;
                if (ms == null || ms <= 0L) {
                    Long res = client.sadd(duNum, valueSet.getKey(), valueSet.getValue());
                    // iSyncerCompensator.sadd(duNum,valueSet.getKey(), valueSet.getValue(),res);
                } else {
                    Long res = client.sadd(duNum, valueSet.getKey(), ms, valueSet.getValue());
                    // iSyncerCompensator.sadd(duNum,valueSet.getKey(), ms,valueSet.getValue(),res);
                }
            } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                client.updateLastReplidAndOffset(replicator.getConfig().getReplId(),
                        replicator.getConfig().getReplOffset());
                //zset类型
                BatchedKeyStringValueZSetEvent valueZSet = (BatchedKeyStringValueZSetEvent) event;
                if (ms == null || ms <= 0L) {
                    Long res = client.zadd(duNum, valueZSet.getKey(), valueZSet.getValue());
                    // iSyncerCompensator.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),res);
                } else {
                    Long res = client.zadd(duNum, valueZSet.getKey(), valueZSet.getValue(), ms);
                    // iSyncerCompensator.zadd(duNum,valueZSet.getKey(), valueZSet.getValue(),ms,res);
                }
            } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {
                client.updateLastReplidAndOffset(replicator.getConfig().getReplId(),
                        replicator.getConfig().getReplOffset());
                //hash类型
                BatchedKeyStringValueHashEvent valueHash = (BatchedKeyStringValueHashEvent) event;
                if (ms == null || ms <= 0L) {
                    String res = client.hmset(duNum, valueHash.getKey(), valueHash.getValue());
                    // iSyncerCompensator.hmset(duNum,valueHash.getKey(), valueHash.getValue(),res);
                } else {
                    String res = client.hmset(duNum, valueHash.getKey(), valueHash.getValue(), ms);
                    // iSyncerCompensator.hmset(duNum,valueHash.getKey(), valueHash.getValue(),ms,res);
                }
            }
            System.out.println("");
        }
    }

    void waitLoadingNode() {
        if (!nodeStatus) {
            circle.getNodeStatus().incrementAndGet();
            nodeStatus = true;
        }

        //TODO 先不等待 所有节点任务状态
        // 首先任务节点都需要能把状态上报到 etcd/zk 上才行

        //        while (!circle.isContinueTask() && circle.getNodeSuccessStatus().get()) {
        //            try {
        //                Thread.sleep(100);
        //            } catch (Exception e) {
        //                e.printStackTrace();
        //            }
        //        }
        if (circle.getNodeSuccessStatusType().get() != -1) {
            circle.getNodeSuccessStatusType().set(1);
        }
        //        MultiSyncTaskManagerutils.setGlobalNodeStatus(globalTaskId,"", TaskStatusType.COMMANDRUNING);
        //        log.info("[{}]节点装载成功...", sourceNodeId);
    }
}
