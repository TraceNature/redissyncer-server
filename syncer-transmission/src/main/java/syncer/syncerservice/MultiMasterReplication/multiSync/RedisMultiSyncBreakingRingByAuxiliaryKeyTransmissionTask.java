package syncer.syncerservice.MultiMasterReplication.multiSync;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerjedis.Protocol;
import syncer.syncerpluscommon.util.taskType.SyncerTaskType;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.OffsetPlace;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.entity.muli.multisync.MultiTaskModel;
import syncer.syncerplusredis.event.*;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import syncer.syncerplusredis.rdb.datatype.DB;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.MultiSyncTaskManagerutils;
import syncer.syncerplusredis.util.SyncTypeUtils;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.JDRedisClient.MulitJedisPipeLineClient;
import syncer.syncerservice.util.JDRedisClient.RedisMigrator;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.TaskCheckUtils;
import syncer.syncerservice.util.circle.MultiSyncCircle;
import syncer.syncerservice.util.common.Strings;

import java.io.IOException;


/**
 * @author zhanenqiang
 * @Description 通过辅助key破环
 * 缺点：会根据每条命令生成辅助key对数据侵入性较大
 * 优点：同步服务不会通过对冲存储数据来破环，同步服务无需临时存储破环数据
 * 思考：当有多个不同节点，每个节点都有不同的db库，每个db库内可能存在相同key名和value，此时key-value md5一致，辅助key该如何区分属于每个库？
 * 是否将db号加入构造辅助key中？ 如何保证破环操作不出错误？
 * 当连续收到多条相同命令对冲机制如何存储，辅助key如何破环
 *
 * 不支持flushall 和 flushdb
 *  当a执行flushall  a--->删除数据  --->b收到后删除数据  此时b中的数据已同步至同步程序的数据会写回a，但此时
 *  b中这部分数据已被清除  ---> 此时可能会出现a中数据比b中多的情况（a中多出的数据来自于b）
 *  若支持（第一个收到本命令的节点需要阻塞各个节点同步写入）
 * @Dae 2020/9/16
 */
@Slf4j
public class RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask implements Runnable {
    private MultiTaskModel multiTaskModel;
    private String sourceNodeId;
    private String targetNodeId;
    private MultiSyncCircle circle;
    JDRedisClient client;
    private int db;
    private boolean nodeStatus=false;


    public RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(MultiTaskModel multiTaskModel, String sourceNodeId, String targetNodeId, MultiSyncCircle circle) {
        this.multiTaskModel = multiTaskModel;
        this.targetNodeId = targetNodeId;
        this.sourceNodeId = sourceNodeId;
        this.circle = circle;
        client = new MulitJedisPipeLineClient(multiTaskModel.getHost(), multiTaskModel.getPort(), multiTaskModel.getPassword(), 50, 1, multiTaskModel.getTaskId(), multiTaskModel.getNodeId(), multiTaskModel.getParentId(),circle.getNodeSuccessStatus(),circle.getNodeSuccessStatusType());
        this.db = -1;
        //        client=new Jedis(multiTaskModel.getHost(),multiTaskModel.getPort());
//        if(multiTaskModel.getPassword()!=null){
//            client.auth(multiTaskModel.getPassword());
////        client=new MulitJDJedisPipeLineClient(multiTaskModel.getHost(),multiTaskModel.getPort(),multiTaskModel.getPassword(),10,-1,"qqqq")
//        }
    }

    @Override
    public void run() {
        try {
            System.out.println("---------open");
            //注册增量命令

            Replicator replicator = null;

            RedisURI suri = new RedisURI(multiTaskModel.getRedisAddress());

            replicator = new JDRedisReplicator(suri, true);
            int rdbVersion =TaskCheckUtils.getRdbVersionByRedisVersion(multiTaskModel.getRedisAddress(),String.valueOf(multiTaskModel.getRedisVersion()));
            //注册增量命令解析器
            final Replicator replicationHandler = RedisMigrator.newBacthedCommandDress(replicator);
            //注册RDB全量解析器
            replicationHandler.setRdbVisitor(new ValueDumpIterableRdbVisitor(replicationHandler, rdbVersion));

            //获取runid和offset
//            String[] data = RedisUrlCheckUtils.selectSyncerBuffer(multiTaskModel.getRedisAddress(), SyncTypeUtils.getOffsetPlace(OffsetPlace.ENDBUFFER.getCode()).getOffsetPlace());
//            replicationHandler.getConfiguration().setReplOffset(Long.parseLong(data[0]));
//            replicationHandler.getConfiguration().setReplId(data[1]);

            String globalTaskId=SyncerTaskType.globalTaskId(multiTaskModel.getTaskId(),multiTaskModel.getParentId(),multiTaskModel.getNodeId());

            //事件监听
            replicationHandler.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {

                    if (event instanceof PreRdbSyncEvent) {
                        log.info("[{}]全量开始...", multiTaskModel.getHost());
                        if (!nodeStatus) {
                            circle.getNodeStatus().incrementAndGet();
                            nodeStatus = true;
                        }

                        while (!circle.isContinueTask()&&circle.getNodeSuccessStatus().get()){
                            try {
                                Thread.sleep(100);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        if(circle.getNodeSuccessStatusType().get()!=-1){
                            circle.getNodeSuccessStatusType().set(1);
                        }
                        MultiSyncTaskManagerutils.setGlobalNodeStatus(globalTaskId,"RDBRUNING", TaskStatusType.RDBRUNING);

                        log.info("[{}]节点装载成功...",sourceNodeId);
                    }



                    //增量同步开始
                    if (event instanceof PreCommandSyncEvent) {
                        log.info("[{}]增量开始...", multiTaskModel.getHost());
                        if(!nodeStatus){
                            circle.getNodeStatus().incrementAndGet();
                            nodeStatus=true;
                        }

                        while (!circle.isContinueTask()&&circle.getNodeSuccessStatus().get()){
                            try {
                                Thread.sleep(100);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        if(circle.getNodeSuccessStatusType().get()!=-1){
                            circle.getNodeSuccessStatusType().set(1);
                        }
                        MultiSyncTaskManagerutils.setGlobalNodeStatus(globalTaskId,"", TaskStatusType.COMMANDRUNING);
                        log.info("[{}]节点装载成功...",sourceNodeId);
                    }

                    if(!circle.getNodeSuccessStatus().get()&&circle.getNodeSuccessStatusType().get()!=0){
                        try {
                            circle.getNodeSuccessStatusType().set(-1);
                            replicationHandler.close();
                            log.info("[{}]节点关闭...",sourceNodeId);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    /**
                    if (event instanceof DumpKeyValuePair) {
                        DumpKeyValuePair command = (DumpKeyValuePair) event;
                        Long ms = command.getExpiredMs();
                        DB dbS = command.getDb();
                        Long duNum = dbS.getDbNumber();
                        long ttl = ms;


                        String md5 = circle.getMd5(command, targetNodeId);

                        db = (int) dbS.getDbNumber();

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

                    **/
                    //命令解析器
                    if (event instanceof DefaultCommand) {
                        DefaultCommand command = (DefaultCommand) event;
                        String stringComand=Strings.byteToString(command.getCommand()).trim();
                        //屏蔽辅助key DEL命令
                        if (circle.isCircleKey(command, sourceNodeId) || circle.isCircleKey(command, targetNodeId)) {
                            if ("DEL".equalsIgnoreCase(Strings.byteToString(command.getCommand()).trim())) {
                                return;
                            }
                        }








                        //屏蔽flushall和flushdb
                        if (stringComand.equalsIgnoreCase(Protocol.Command.FLUSHALL.toString()) || stringComand.equalsIgnoreCase(Protocol.Command.FLUSHDB.toString())) {
                            log.warn("[{}]-[{}]-[{}]已屏蔽[{}]命令",multiTaskModel.getTaskId(),multiTaskModel.getParentId(),sourceNodeId,stringComand);
                            return;
                        }



                        //非自身辅助key
                        if (circle.isCircleKey(command, targetNodeId)) {
                            String md5 = Strings.byteToStrings(command.getArgs()[0]);
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
                    if (event instanceof DumpKeyValuePair) {
                        DumpKeyValuePair valueDump = (DumpKeyValuePair) event;
                        Long ms=valueDump.getExpiredMs();
                        DB vdb=valueDump.getDb();
                        Long dbNum=vdb.getDbNumber();
                        db = dbNum.intValue();
                        long ttl = (ms==null||ms<0)?0:ms;
                        String[] data = new String[]{circle.getRdbDumpMd5(valueDump, sourceNodeId,3.0), "1", "1"};
                        byte[][] ndata = new byte[][]{data[0].getBytes(), data[1].getBytes(), data[2].getBytes()};
                        client.send("PSETEX".getBytes(), ndata);
                        client.restoreReplace(dbNum,valueDump.getKey(), ttl,valueDump.getValue(),true);
                    }


                    /**
                     * 大Key拆分数据  全量
                     */
                    if (event instanceof BatchedKeyValuePair<?, ?>) {

                    }


                }
            });

            replicationHandler.open(SyncerTaskType.globalTaskId(multiTaskModel.getTaskId(),multiTaskModel.getParentId(),multiTaskModel.getNodeId()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
