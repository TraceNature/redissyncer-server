package com.i1314i.syncerplusservice.task.clusterTask;

import com.i1314i.syncerplusservice.pool.RedisMigrator;
import com.i1314i.syncerplusservice.util.Jedis.IJedisClient;
import com.i1314i.syncerplusservice.util.Jedis.ObjectUtils;
import com.i1314i.syncerplusservice.util.Jedis.StringUtils;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.moilioncircle.redis.replicator.CloseListener;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;

import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;

import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.datatype.Module;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * RedisCluster 数据迁移同步线程
 * 从原生Cluste集群往JDCloud集群迁移同步
 * redisCluster集群同步分两种情况,redisCluster由于时3.0之后推出的，所以无需考虑restore 无replace 问题
 * 由于JDCloud基于2.8版本，所以需要考虑restore replace相关问题
 * 只需考虑 跨版本和同版本迁移同步问题
 */
@Slf4j
public class ClusterRdbSameVersionJDCloudRestoreTask implements Callable<Integer> {

    private String sourceUri;  //源redis地址
    private String targetUri;  //目标redis地址


    private IJedisClient jedisClient;

    public ClusterRdbSameVersionJDCloudRestoreTask(IJedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }

    public static void main(String[] args) throws ParseException, IOException, URISyntaxException {
//        SyncJedisClusterClient clusterClient=new SyncJedisClusterClient("114.67.100.240:8002,114.67.100.239:8002,114.67.100.238:8002,114.67.100.240:8003,114.67.100.239:8003,114.67.100.238:8003","",10,1,1500,10000);
//        JedisCluster jedisCluster=clusterClient.jedisCluster();
//        System.out.println(jedisCluster.get("key:000005594226"));
//
//
//        final Replicator replicator = new RedisReplicator("redis://114.67.81.232:6379?authPassword=redistest0102");


    }

    @Override
    public Integer call() throws Exception {

        final AtomicInteger dbnum = new AtomicInteger(-1);
        Replicator r =  RedisMigrator.dress((new RedisReplicator("redis://127.0.0.1:6480")));

        return null;

    }





    /**
     * 获取byte[]类型Key
     * @param key
     * @return
     */
    public static  byte[] getBytesKey(Object object){
        if(object instanceof String){
            return StringUtils.getBytes((String)object);
        }else{
            return ObjectUtils.serialize(object);
        }
    }

    /**
     * Object转换byte[]类型
     * @param key
     * @return
     */
    public static  byte[] toBytes(Object object){
        return ObjectUtils.serialize(object);
    }

    /**
     * byte[]型转换Object
     * @param key
     * @return
     */
    public static Object toObject(byte[] bytes){
        return ObjectUtils.unserialize(bytes);
    }
}
