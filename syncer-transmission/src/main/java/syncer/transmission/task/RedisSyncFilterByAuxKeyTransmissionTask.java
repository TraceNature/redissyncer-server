package syncer.transmission.task;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import syncer.jedis.Protocol;
import syncer.replica.config.RedisURI;
import syncer.replica.constant.RedisType;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.Event;
import syncer.replica.event.iter.datatype.BatchedKeyValuePairEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.event.start.PreRdbSyncEvent;
import syncer.replica.listener.EventListener;
import syncer.replica.parser.syncer.ValueDumpIterableRdbParser;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.register.DefaultCommandRegister;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.replica.util.SyncTypeUtils;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.RedisClientFactory;
import syncer.transmission.model.TaskModel;
import syncer.transmission.task.circle.MultiSyncCircle;
import syncer.transmission.util.SyncerTaskTypeUtils;
import syncer.transmission.util.redis.RedisVersionUtil;


/**
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

    public RedisSyncFilterByAuxKeyTransmissionTask(TaskModel taskModel, MultiSyncCircle circle) {
        this.taskModel = taskModel;
        this.targetNodeId = taskModel.getTargetNodeId();
        this.sourceNodeId = taskModel.getNodeId();
        this.circle = circle;
//        client = new MulitJedisPipeLineClient(multiTaskModel.getHost(), multiTaskModel.getPort(), multiTaskModel.getPassword(), 50, 1, multiTaskModel.getTaskId(), multiTaskModel.getNodeId(), multiTaskModel.getParentId(), circle.getNodeSuccessStatus(), circle.getNodeSuccessStatusType());

        //TODO 先处理 redis-cluster， 其他的再考虑
        RedisType redisType = SyncTypeUtils.getRedisType(taskModel.getTargetRedisType());
        client = RedisClientFactory.createRedisClient(RedisType.CLUSTER, taskModel.getTargetRedisAddress(), taskModel.getTargetPort(),
                taskModel.getTargetPassword(), "", 0, 0, taskModel.getErrorCount(), taskModel.getTaskId(), null, null);

        this.db = -1;
        this.redisVersionUtil = new RedisVersionUtil();
    }

    @Override
    public void run() {
        try {
            String globalTaskId = SyncerTaskTypeUtils.globalTaskId(taskModel.getTaskId(), taskModel.getGroupId(), taskModel.getNodeId());

            Replication replication = null;
            RedisURI suri = new RedisURI(taskModel.getSourceUri());
            replication = new RedisReplication(suri, true);
            replication.getConfig().setTaskId(globalTaskId);
            int rdbVersion = redisVersionUtil.getRdbVersionByRedisVersion(taskModel.getSourceRedisAddress(), String.valueOf(taskModel.getRedisVersion()));
            //注册增量命令解析器
            final Replication replicationHandler = DefaultCommandRegister.addCommandParser(replication);
            //注册RDB全量解析器
            replicationHandler.setRdbParser(new ValueDumpIterableRdbParser(replicationHandler, rdbVersion));
            //事件监听
            replicationHandler.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replication replicator, Event event) {
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

                    //命令解析器
                    if (event instanceof DefaultCommand) {
                        DefaultCommand command = (DefaultCommand) event;
                        String key = Strings.byteToString(command.getArgs())[0];

                        String stringComand = Strings.byteToString(command.getCommand()).trim();
                        log.info("replicate... command: [{}] key: [{}]", stringComand, key);
                        //屏蔽辅助key DEL命令
                        if (circle.isCircleKey(command, sourceNodeId) || circle.isCircleKey(command, targetNodeId)) {
                            // psetex 辅助key 过期后会产生del命令
                            if ("DEL".equalsIgnoreCase(Strings.byteToString(command.getCommand()).trim())) {
                                log.info("屏蔽circle aux key [{}]的 del 操作", key);
                                return;
                            }

                        }
                        //屏蔽flushall和flushdb
                        if (stringComand.equalsIgnoreCase(Protocol.Command.FLUSHALL.toString()) || stringComand.equalsIgnoreCase(Protocol.Command.FLUSHDB.toString())) {
                            log.warn("[{}]-[{}]-[{}]已屏蔽[{}]命令", taskModel.getTaskId(), taskModel.getGroupId(), sourceNodeId, stringComand);
                            return;
                        }
                        //非自身辅助key, 说明是反向任务同步过来的, 先把 circle-key 缓存一下 用来后面做过滤用
                        if (circle.isCircleKey(command, targetNodeId)) {
                            String md5 = Strings.byteToString(command.getArgs()[0]);
                            circle.addDataMap(sourceNodeId, md5);
                            log.info("缓存 circle aux key [{}]", key);
                            return;
                        }

                        if (!circle.isCircleKey(command, targetNodeId) && !circle.isCircleKey(command, sourceNodeId)) {
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
                                byte[][] selectNum = new byte[][]{String.valueOf(db).getBytes()};
                                DefaultCommand selectComand = new DefaultCommand(Protocol.Command.SELECT.getRaw(), selectNum);
                                String[] data = new String[]{circle.getMd5(selectComand, sourceNodeId), "1", "1"};
                                byte[][] ndata = new byte[][]{data[0].getBytes(), data[1].getBytes(), data[2].getBytes()};
                                try {
                                    client.send("PSETEX".getBytes(), ndata);
                                    client.send(selectComand.getCommand(), selectComand.getArgs());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                db = -1;
                            }

                            String[] data = new String[]{circle.getMd5(command, sourceNodeId), "1", "1"};
                            byte[][] ndata = new byte[][]{data[0].getBytes(), data[1].getBytes(), data[2].getBytes()};
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
                        String[] data = new String[]{rdbDumpMd5, "1", "1"};
                        byte[][] ndata = new byte[][]{data[0].getBytes(), data[1].getBytes(), data[2].getBytes()};
                        try {
                            log.info("restore...PSETEX aux key [{}]", rdbDumpMd5);
                            client.send("PSETEX".getBytes(), ndata);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        client.restoreReplace(dbNum, valueDump.getKey(), ttl, valueDump.getValue(), true);
                    }

                    /**
                     * 大Key拆分数据  全量
                     */
                    if (event instanceof BatchedKeyValuePairEvent<?, ?>) {

                    }

                }

                @Override
                public String eventListenerName() {
                    return "redis-circle-task-listener";
                }
            });
            replicationHandler.open();

        } catch (Exception e) {
            //TODO 自动重启
            log.error("sync break ring by auxiliary key start error , {}", e.getMessage());
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
        log.info("[{}]节点装载成功...", sourceNodeId);
    }
}
