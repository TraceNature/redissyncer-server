package syncer.transmission.task;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.config.RedisURI;
import syncer.replica.constant.RedisType;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.event.start.PreRdbSyncEvent;
import syncer.replica.parser.syncer.DumpRdbParser;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.register.DefaultCommandRegister;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.replica.util.SyncTypeUtils;
import syncer.replica.util.TaskRunTypeEnum;
import syncer.transmission.checkpoint.breakpoint.BreakPoint;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.RedisClientFactory;
import syncer.transmission.entity.OffSetEntity;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.ExpandTaskModel;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.task.circle.MultiSyncCircle;
import syncer.transmission.util.SyncerTaskTypeUtils;
import syncer.transmission.util.redis.RedisVersionUtil;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

/**
 * 双向复制任务， 基于辅助key破环
 *
 * @author : susongyan
 * @since : 2021/9/7
 **/
@Slf4j
public class RedisSyncFilterByAuxKeyTransmissionTask implements Runnable {

    private final TaskModel taskModel;
    private final String sourceRedisName;
    private final String targetRedisName;
    private final MultiSyncCircle circle;
    RedisClient client;
    private int db;
    private boolean nodeStatus = false;
    RedisVersionUtil redisVersionUtil;
    BreakPoint breakPoint;

    public RedisSyncFilterByAuxKeyTransmissionTask(TaskModel taskModel) {
        this.taskModel = taskModel;
        this.sourceRedisName = taskModel.getSourceRedisName();
        this.targetRedisName = taskModel.getTargetRedisName();
        this.circle = MultiSyncCircle.getInsance();
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
                    taskModel.getRedisVersion());
            //注册增量命令解析器
            final Replication replicationHandler = DefaultCommandRegister.addCommandParser(replication);
            replicationHandler.getConfig().setTaskId(taskModel.getTaskId());
            //注册RDB全量解析器
            replicationHandler.setRdbParser(new DumpRdbParser(replicationHandler, taskModel.getRdbVersion()));
            //断点续传
            OffSetEntity baseOffset = setOffset(replicationHandler);
            //任务进度管理
            SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getTaskId())
                    .setReplication(replicationHandler);

            //事件监听
            replicationHandler.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replication replicator, Event event) {
                    // log.debug("event " + event);
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
                            log.info("[{}]节点关闭...");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // 增量命令
                    if (event instanceof DefaultCommand) {
                        DefaultCommand command = (DefaultCommand) event;
                        String key = Strings.byteToString(command.getArgs())[0];

                        String stringComand = Strings.byteToString(command.getCommand()).trim();
                        log.info("replicate... [{}] to [{}] command: [{}] key: [{}]", sourceRedisName, targetRedisName,
                                stringComand, key);

                        boolean sourceRedisCircleKey = circle.isCircleKey(command, sourceRedisName);
                        boolean targetRedisCircleKey = circle.isCircleKey(command, targetRedisName);
                        boolean circleAuxKey = sourceRedisCircleKey || targetRedisCircleKey;
                        // 辅助key的处理
                        if (circleAuxKey) {
                            // psetex 辅助key 过期后会产生del命令
                            if ("DEL".equalsIgnoreCase(Strings.byteToString(command.getCommand()).trim())) {
                                log.info("屏蔽 circle aux key [{}]的 del 操作", key);
                                return;
                            }

                            // 缓存起来，屏蔽命令用
                            String md5 = Strings.byteToString(command.getArgs()[0]);
                            circle.addDataMap(sourceRedisName, md5);
                            log.info("缓存 circle aux key [{}]", key);
                            return;
                        }
                        //屏蔽flushall和flushdb
                        if (stringComand.equalsIgnoreCase(Protocol.Command.FLUSHALL.toString())
                                || stringComand.equalsIgnoreCase(Protocol.Command.FLUSHDB.toString())) {
                            log.warn("[{}]-[{}]-[{}]已屏蔽[{}]命令", taskModel.getTaskId(), taskModel.getGroupId(),
                                    sourceRedisName, stringComand);
                            return;
                        }

                        //非辅助key
                        if (!circleAuxKey) {
                            String md5 = circle.getMd5(command, targetRedisName);
                            if (stringComand.equalsIgnoreCase(Protocol.Command.SELECT.toString())) {
                                db = Integer.parseInt(Strings.byteToString(command.getArgs()[0]).trim());
                                return;
                            }

                            // 判断是否复制过来的命令
                            // 如果是其他节点写过来的命令，那么在辅助key缓存里边应该有，移除缓存，不再复制过去
                            if (circle.getDataMap(sourceRedisName).containsKey(md5)) {
                                log.info("found replicated command of key [{}] by circle aux key [{}], ignoring", key,
                                        md5);
                                circle.removeDataMap(sourceRedisName, md5);
                                return;
                            }

                            if (db != -1) {
                                byte[][] selectNum = new byte[][] { String.valueOf(db).getBytes() };
                                DefaultCommand selectComand = new DefaultCommand(Protocol.Command.SELECT.getRaw(),
                                        selectNum);
                                String[] data = new String[] { circle.getMd5(selectComand, sourceRedisName), "1", "1" };
                                byte[][] ndata = new byte[][] { data[0].getBytes(), data[1].getBytes(),
                                        data[2].getBytes() };
                                try {
                                    client.send("PSETEX".getBytes(), ndata);
                                    client.send(selectComand.getCommand(), selectComand.getArgs());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                db = -1;
                            }

                            String[] data = new String[] { circle.getMd5(command, sourceRedisName), "1", "1" };
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

                    //全量命令 RESTORE
                    if (event instanceof DumpKeyValuePairEvent) {
                        DumpKeyValuePairEvent valueDump = (DumpKeyValuePairEvent) event;
                        Long ms = valueDump.getExpiredMs();
                        RedisDB vdb = valueDump.getDb();
                        Long dbNum = vdb.getCurrentDbNumber();
                        db = dbNum.intValue();
                        long ttl = (ms == null || ms < 0) ? 0 : ms;
                        String rdbDumpMd5 = circle.getRdbDumpMd5(valueDump, sourceRedisName, 3.0);
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

                    // 更新offset
                    updateOffset(taskModel, baseOffset, replicationHandler, event);
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
    private OffSetEntity setOffset(Replication replicationHandler) {
        OffSetEntity offset = null;
        offset = breakPoint.checkPointOffset(taskModel);

        /** old version
        
         TaskDataEntity taskDataEntity=SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getTaskId());
         if(Objects.nonNull(taskDataEntity)){
         offset = taskDataEntity.getOffSetEntity();
         }
         */

        if (offset == null) {
            offset = new OffSetEntity();
            SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getTaskId()).setOffSetEntity(offset);
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

        return offset;
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

    /**
     * 计算offset
     *
     * @param taskId
     * @param replicationHandler
     * @param node
     */
    private void updateOffset(TaskModel taskModel, OffSetEntity baseoffset, Replication replicationHandler,
            Event event) {
        KeyValueEventEntity node = KeyValueEventEntity.builder().event(event).dbMapper(taskModel.loadDbMapping())
                .redisVersion(taskModel.getRedisVersion()).baseOffSet(baseoffset)
                .replId(replicationHandler.getConfig().getReplId())
                .replOffset(replicationHandler.getConfig().getReplOffset())
                .taskRunTypeEnum(SyncTypeUtils.getTaskType(taskModel.getTaskType()).getType())
                .fileType(SyncTypeUtils.getSyncType(taskModel.getSyncType()).getFileType()).build();
        try {
            TaskDataEntity data = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskModel.getTaskId());
            if (data.getOffSetEntity() == null) {
                data.setOffSetEntity(OffSetEntity.builder().replId(replicationHandler.getConfig().getReplId()).build());
            }
            try {
                ExpandTaskModel expandTaskModel = data.getExpandTaskModel();
                expandTaskModel.readFileSize.set(replicationHandler.getConfig().getReadFileSize());
                expandTaskModel.fileSize.set(replicationHandler.getConfig().getFileSize());
                taskModel.updateExpandJson(expandTaskModel);
            } catch (Exception e) {
                
            }

            // 全量同步结束、增量开始、命令同步
            if (event instanceof PostRdbSyncEvent || event instanceof DefaultCommand
                    || event instanceof PreCommandSyncEvent) {
                data.getOffSetEntity().setReplId(replicationHandler.getConfig().getReplId());
                data.getOffSetEntity().getReplOffset().set(replicationHandler.getConfig().getReplOffset());
                try {
                    ExpandTaskModel expandTaskModel = data.getExpandTaskModel();
                    expandTaskModel.readFileSize.set(replicationHandler.getConfig().getFileSize());
                    taskModel.updateExpandJson(expandTaskModel);
                } catch (Exception e) {

                }
                if (node.getTaskRunTypeEnum().equals(TaskRunTypeEnum.STOCKONLY)
                        || event instanceof PreCommandSyncEvent) {
                    SqlOPUtils.updateOffsetAndReplId(taskModel.getTaskId(),
                            replicationHandler.getConfig().getReplOffset(), replicationHandler.getConfig().getReplId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("[{}]update offset fail,replid[{}],offset[{}]", taskModel.getTaskId(),
                    replicationHandler.getConfig().getReplId(), replicationHandler.getConfig().getReplOffset());
        }
    }
}