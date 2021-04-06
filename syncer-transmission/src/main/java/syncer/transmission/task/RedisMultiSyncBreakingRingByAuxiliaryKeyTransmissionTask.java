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

package syncer.transmission.task;

import lombok.extern.slf4j.Slf4j;
import syncer.jedis.Protocol;
import syncer.replica.config.RedisURI;
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
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.MultiTaskModel;
import syncer.transmission.task.circle.MultiSyncCircle;
import syncer.transmission.util.SyncerTaskTypeUtils;
import syncer.transmission.util.redis.RedisVersionUtil;

import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description 通过辅助key破环
 * 缺点：会根据每条命令生成辅助key对数据侵入性较大
 * 优点：同步服务不会通过对冲存储数据来破环，同步服务无需临时存储破环数据
 * 思考：当有多个不同节点，每个节点都有不同的db库，每个db库内可能存在相同key名和value，此时key-value md5一致，辅助key该如何区分属于每个库？
 * 是否将db号加入构造辅助key中？ 如何保证破环操作不出错误？
 * 当连续收到多条相同命令对冲机制如何存储，辅助key如何破环
 * <p>
 * 不支持flushall 和 flushdb
 * 当a执行flushall  a--->删除数据  --->b收到后删除数据  此时b中的数据已同步至同步程序的数据会写回a，但此时
 * b中这部分数据已被清除  ---> 此时可能会出现a中数据比b中多的情况（a中多出的数据来自于b）
 * 若支持（第一个收到本命令的节点需要阻塞各个节点同步写入）
 */
@Slf4j
public class RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask implements Runnable {
    private MultiTaskModel multiTaskModel;
    private String sourceNodeId;
    private String targetNodeId;
    private MultiSyncCircle circle;
    RedisClient client;
    private int db;
    private boolean nodeStatus = false;
    RedisVersionUtil redisVersionUtil;

    public RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(MultiTaskModel multiTaskModel, String sourceNodeId, String targetNodeId, MultiSyncCircle circle) {
        this.multiTaskModel = multiTaskModel;
        this.targetNodeId = targetNodeId;
        this.sourceNodeId = sourceNodeId;
        this.circle = circle;
//        client = new MulitJedisPipeLineClient(multiTaskModel.getHost(), multiTaskModel.getPort(), multiTaskModel.getPassword(), 50, 1, multiTaskModel.getTaskId(), multiTaskModel.getNodeId(), multiTaskModel.getParentId(), circle.getNodeSuccessStatus(), circle.getNodeSuccessStatusType());
        this.db = -1;
        this.redisVersionUtil = new RedisVersionUtil();
    }

    @Override
    public void run() {
        try {
            String globalTaskId = SyncerTaskTypeUtils.globalTaskId(multiTaskModel.getTaskId(), multiTaskModel.getParentId(), multiTaskModel.getNodeId());

            Replication replication = null;
            RedisURI suri = new RedisURI(multiTaskModel.getRedisAddress());
            replication = new RedisReplication(suri, true);
            replication.getConfig().setTaskId(globalTaskId);
            int rdbVersion = redisVersionUtil.getRdbVersionByRedisVersion(multiTaskModel.getRedisAddress(), String.valueOf(multiTaskModel.getRedisVersion()));
            //注册增量命令解析器
            final Replication replicationHandler = DefaultCommandRegister.addCommandParser(replication);
            //注册RDB全量解析器
            replicationHandler.setRdbParser(new ValueDumpIterableRdbParser(replicationHandler, rdbVersion));
            //事件监听
            replicationHandler.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replication replicator, Event event) {
                    if (event instanceof PreRdbSyncEvent) {
                        log.info("[{}]全量开始...", multiTaskModel.getHost());
                        waitLoadingNode();
                    }

                    //增量同步开始
                    if (event instanceof PreCommandSyncEvent) {
                        log.info("[{}]增量开始...", multiTaskModel.getHost());
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
                        String stringComand = Strings.byteToString(command.getCommand()).trim();
                        //屏蔽辅助key DEL命令
                        if (circle.isCircleKey(command, sourceNodeId) || circle.isCircleKey(command, targetNodeId)) {
                            if ("DEL".equalsIgnoreCase(Strings.byteToString(command.getCommand()).trim())) {
                                return;
                            }
                        }
                        //屏蔽flushall和flushdb
                        if (stringComand.equalsIgnoreCase(Protocol.Command.FLUSHALL.toString()) || stringComand.equalsIgnoreCase(Protocol.Command.FLUSHDB.toString())) {
                            log.warn("[{}]-[{}]-[{}]已屏蔽[{}]命令", multiTaskModel.getTaskId(), multiTaskModel.getParentId(), sourceNodeId, stringComand);
                            return;
                        }
                        //非自身辅助key
                        if (circle.isCircleKey(command, targetNodeId)) {
                            String md5 = Strings.byteToString(command.getArgs()[0]);
                            circle.addDataMap(sourceNodeId, md5);
                            return;
                        }

                        if (!circle.isCircleKey(command, targetNodeId) && !circle.isCircleKey(command, sourceNodeId)) {
                            String md5 = circle.getMd5(command, targetNodeId);
                            if (stringComand.equalsIgnoreCase(Protocol.Command.SELECT.toString())) {
                                db = Integer.valueOf(Strings.byteToString(command.getArgs()[0]).trim());
                                return;
                            }

                            if (circle.getDataMap(sourceNodeId).containsKey(md5)) {
                                circle.removeDataMap(sourceNodeId, md5);
                                return;
                            }

                            if (db != -1) {
                                byte[][] selectNum = new byte[][]{String.valueOf(db).getBytes()};
                                DefaultCommand selectComand = new DefaultCommand(Protocol.Command.SELECT.getRaw(), selectNum);
                                String[] data = new String[]{circle.getMd5(selectComand, sourceNodeId), "1", "1"};
                                byte[][] ndata = new byte[][]{data[0].getBytes(), data[1].getBytes(), data[2].getBytes()};
                                client.send("PSETEX".getBytes(), ndata);
                                client.send(selectComand.getCommand(), selectComand.getArgs());
                                db = -1;
                            }

                            String[] data = new String[]{circle.getMd5(command, sourceNodeId), "1", "1"};
                            byte[][] ndata = new byte[][]{data[0].getBytes(), data[1].getBytes(), data[2].getBytes()};
                            client.send("PSETEX".getBytes(), ndata);
                            client.send(command.getCommand(), command.getArgs());
                        }
                    }


                    /**
                     * RESTORE 全量
                     */
                    if (event instanceof DumpKeyValuePairEvent) {
                        DumpKeyValuePairEvent valueDump = (DumpKeyValuePairEvent) event;
                        Long ms = valueDump.getExpiredMs();
                        RedisDB vdb = valueDump.getDb();
                        Long dbNum = vdb.getCurrentDbNumber();
                        db = dbNum.intValue();
                        long ttl = (ms == null || ms < 0) ? 0 : ms;
                        String[] data = new String[]{circle.getRdbDumpMd5(valueDump, sourceNodeId, 3.0), "1", "1"};
                        byte[][] ndata = new byte[][]{data[0].getBytes(), data[1].getBytes(), data[2].getBytes()};
                        client.send("PSETEX".getBytes(), ndata);
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
                    return null;
                }
            });
            replicationHandler.open();

        } catch (Exception e) {
            log.error("sync break ring by auxiliary key start error , {}",e.getMessage());
        }
    }


    void waitLoadingNode(){
        if (!nodeStatus) {
            circle.getNodeStatus().incrementAndGet();
            nodeStatus = true;
        }

        while (!circle.isContinueTask() && circle.getNodeSuccessStatus().get()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (circle.getNodeSuccessStatusType().get() != -1) {
            circle.getNodeSuccessStatusType().set(1);
        }
        // MultiSyncTaskManagerutils.setGlobalNodeStatus(globalTaskId,"", TaskStatusType.COMMANDRUNING);
        log.info("[{}]节点装载成功...", sourceNodeId);
    }
}
