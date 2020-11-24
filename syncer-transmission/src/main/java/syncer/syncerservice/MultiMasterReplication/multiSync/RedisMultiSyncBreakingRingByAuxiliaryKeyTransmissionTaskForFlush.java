package syncer.syncerservice.MultiMasterReplication.multiSync;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerjedis.Jedis;
import syncer.syncerjedis.Protocol;
import syncer.syncerpluscommon.util.ThreadPoolUtils;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.OffsetPlace;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.EventListener;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.SyncTypeUtils;
import syncer.syncerservice.cmd.ClusterProtocolCommand;
import syncer.syncerplusredis.entity.muli.multisync.MultiTaskModel;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.JDRedisClient.MulitJedisPipeLineClient;
import syncer.syncerservice.util.JDRedisClient.RedisMigrator;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.circle.MultiSyncCircle;
import syncer.syncerservice.util.common.Strings;


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
 *
 *  支持flushall 必须改两端线程命令为同步....
 * @Dae 2020/9/16
 */
@Slf4j
public class RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTaskForFlush implements Runnable {
    private MultiTaskModel multiTaskModel;
    private String sourceNodeId;
    private String targetNodeId;
    private MultiSyncCircle circle;
    JDRedisClient client;
    private int db;


    public RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTaskForFlush(MultiTaskModel multiTaskModel, String sourceNodeId, String targetNodeId, MultiSyncCircle circle) {
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

            replicator = new JDRedisReplicator(suri, false);

            //注册增量命令解析器
            final Replicator replicationHandler = RedisMigrator.newBacthedCommandDress(replicator);
            //注册RDB全量解析器
            replicationHandler.setRdbVisitor(new ValueDumpIterableRdbVisitor(replicationHandler, 9));

            //获取runid和offset
            String[] data = RedisUrlCheckUtils.selectSyncerBuffer(multiTaskModel.getRedisAddress(), SyncTypeUtils.getOffsetPlace(OffsetPlace.ENDBUFFER.getCode()).getOffsetPlace());

            replicationHandler.getConfiguration().setReplOffset(Long.parseLong(data[0]));
            replicationHandler.getConfiguration().setReplId(data[1]);


            //事件监听
            replicationHandler.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {

                    //增量同步开始
                    if (event instanceof PreCommandSyncEvent) {
                        log.info("[{}]增量开始...", multiTaskModel.getHost());
                    }
                    //命令解析器
                    if (event instanceof DefaultCommand) {
                        DefaultCommand command = (DefaultCommand) event;
                        String stringComand=Strings.byteToString(command.getCommand()).toUpperCase().trim();
                        //屏蔽辅助key DEL命令
                        if (circle.isCircleKey(command, sourceNodeId) || circle.isCircleKey(command, targetNodeId)) {
                            if ("DEL".equalsIgnoreCase(Strings.byteToString(command.getCommand()).trim())) {
                                return;
                            }
                        }






                        /**

                        //屏蔽flushall和flushdb
                        if (stringComand.equalsIgnoreCase(Protocol.Command.FLUSHALL.toString()) || stringComand.equalsIgnoreCase(Protocol.Command.FLUSHDB.toString())) {
                            log.warn("[{}]-[{}]-[{}]已屏蔽[{}]命令",multiTaskModel.getParentId(),multiTaskModel.getNodeId(),sourceNodeId,stringComand);
                            return;
                        }

                         **/






                        //非自身辅助key
                        if (circle.isCircleKey(command, targetNodeId)) {
                            String md5 = Strings.byteToStrings(command.getArgs()[0]);
                            circle.addDataMap(sourceNodeId, md5);
                            return;
                        }


                        if(circle.getFlushCommandStatus().get(targetNodeId).getStatus().get()&&!stringComand.equalsIgnoreCase(Protocol.Command.FLUSHALL.toString())){
                            System.out.println(sourceNodeId+"抛弃"+ db+ ": "+Strings.byteToString(command.getArgs()[0]));
                            return;
                        }




                        //flushall命令处理
                        if (!circle.isCircleKey(command, targetNodeId)&&!circle.isCircleKey(command, sourceNodeId)) {
                            if(stringComand.equalsIgnoreCase("FLUSHALL")){

                                if(circle.getFlushCommandStatus().get(targetNodeId).getStatus().get()){
                                    System.out.println("目标存在删除...");
                                    int num=circle.getFlushCommandStatus().get(targetNodeId).getNum().decrementAndGet();
                                    if(num<=0){
                                        circle.getFlushCommandStatus().get(targetNodeId).getStatus().set(false);
                                        circle.getFlushCommandStatus().get(targetNodeId).setType(-1);
                                        circle.getFlushCommandStatus().get(targetNodeId).getNum().set(0);

                                    }
                                    return;
                                }

                                System.out.println(sourceNodeId+": 出现flushall");
                                if(circle.getFlushCommandStatus().get(sourceNodeId).getStatus().get()){
                                    circle.getFlushCommandStatus().get(sourceNodeId).getNum().incrementAndGet();
                                }else {
                                    circle.getFlushCommandStatus().get(sourceNodeId).getStatus().set(true);
                                    circle.getFlushCommandStatus().get(sourceNodeId).setType(1);
                                    circle.getFlushCommandStatus().get(sourceNodeId).getNum().incrementAndGet();
                                }
                            }
                        }





                        if (!circle.isCircleKey(command, targetNodeId) && !circle.isCircleKey(command, sourceNodeId)) {

                            String md5 = circle.getMd5(command, targetNodeId);



                            if (stringComand.equalsIgnoreCase(Protocol.Command.SELECT.toString())) {

                               db = Integer.valueOf(Strings.byteToString(command.getArgs()[0]).trim());
                                return;
                            }

//                            if (circle.getDataMap(sourceNodeId).containsKey(md5)) {
//                                circle.removeDataMap(sourceNodeId, md5);
//                                return;
//                            }


                            if (circle.getDataMap(sourceNodeId).containsKey(md5)&&!stringComand.equalsIgnoreCase(Protocol.Command.FLUSHALL.toString())) {
                                circle.removeDataMap(sourceNodeId, md5);
                                return;
                            }




//                            if(circle.getFlushCommandStatus().get(targetNodeId).)


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
                }
            });

            replicationHandler.open();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
