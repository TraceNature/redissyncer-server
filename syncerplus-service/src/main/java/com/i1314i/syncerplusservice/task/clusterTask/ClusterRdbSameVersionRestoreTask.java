package com.i1314i.syncerplusservice.task.clusterTask;

import com.i1314i.syncerplusservice.util.Jedis.ObjectUtils;
import com.i1314i.syncerplusservice.util.Jedis.StringUtils;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.concurrent.Callable;


/**
 * RedisCluster 数据迁移同步线程
 * 从原生Cluste集群往JDCloud集群迁移同步
 * redisCluster集群同步分两种情况,redisCluster由于时3.0之后推出的，所以无需考虑restore 无replace 问题
 * 由于JDCloud基于2.8版本，所以需要考虑restore replace相关问题
 * 只需考虑 跨版本和同版本迁移同步问题
 */
@Slf4j
public class ClusterRdbSameVersionRestoreTask implements Callable<Integer> {

    private String sourceUri;  //源redis地址
    private String targetUri;  //目标redis地址

    private JedisCluster target;




    public static void main(String[] args) throws ParseException, IOException, URISyntaxException {
        SyncJedisClusterClient clusterClient=new SyncJedisClusterClient("114.67.100.240:8002,114.67.100.239:8002,114.67.100.238:8002,114.67.100.240:8003,114.67.100.239:8003,114.67.100.238:8003","",10,1,1500,10000);
        JedisCluster jedisCluster=clusterClient.jedisCluster();
        System.out.println(jedisCluster.get("key:000005594226"));


        final Replicator replicator = new RedisReplicator("redis://114.67.81.232:6379?authPassword=redistest0102");

    }

    @Override
    public Integer call() throws Exception {

//        target=new SyncJedisClusterClient().jedisCluster();

//        final AtomicInteger dbnum = new AtomicInteger(-1);
//        Replicator r =  RedisMigrator.dress((new RedisReplicator(suri));
//        r.addRdbListener(new RdbListener.Adaptor() {
//            @Override
//            public void handle(Replicator replicator, KeyValuePair<?> kv) {
//                if (!(kv instanceof DumpKeyValuePair)) return;
//                // Step1: select db
//                DB db = kv.getDb();
//                int index;
//                if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
//
//                    target.send(SELECT, toByteArray(index));
//                    dbnum.set(index);
//                    System.out.println("SELECT:" + index);
//                }
//
//                // Step2: restore dump data
//                DumpKeyValuePair mkv = (DumpKeyValuePair) kv;
//                if (mkv.getExpiredMs() == null) {
//                    Object r = target.restore(mkv.getRawKey(), 0L, mkv.getValue(), true);
//                    System.out.println(r);
//                } else {
//                    long ms = mkv.getExpiredMs() - System.currentTimeMillis();
//                    if (ms <= 0) return;
//                    Object r = target.restore(mkv.getRawKey(), ms, mkv.getValue(), true);
//                    System.out.println(r);
//                }
//            }
//        });
//        r.addCommandListener(new CommandListener() {
//            @Override
//            public void handle(Replicator replicator, Command command) {
//                if (!(command instanceof DefaultCommand)) return;
//                // Step3: sync aof command
//                DefaultCommand dc = (DefaultCommand) command;
//                Object r = target.send(dc.getCommand(), dc.getArgs());
//                System.out.println(r);
//            }
//        });
//        r.addCloseListener(new CloseListener() {
//            @Override
//            public void handle(Replicator replicator) {
//
//            }
//        });
//        r.open();



        return null;
    }





}
