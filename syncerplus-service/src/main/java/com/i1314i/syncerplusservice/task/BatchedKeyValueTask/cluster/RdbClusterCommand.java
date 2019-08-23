package com.i1314i.syncerplusservice.task.BatchedKeyValueTask.cluster;


import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import com.moilioncircle.redis.replicator.Constants;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.iterable.datatype.BatchedKeyValuePair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;
import java.util.Map;


@Slf4j
public class RdbClusterCommand {
    private boolean status = true;

    public RdbClusterCommand() {
        if (status) {
            this.status = false;
        }
    }

    public void sendRestoreDumpData(Event event, Replicator r, ThreadPoolTaskExecutor threadPoolTaskExecutor, JedisClusterPlus redisClient, String threadName, Map<Integer, Integer> dbMap) {

        if (event instanceof PreRdbSyncEvent) {
            log.info("{} :全量同步启动", threadName);
        }

        if (event instanceof PostRdbSyncEvent) {
            log.info("{} :全量同步结束", threadName);
        }

        if (event instanceof BatchedKeyValuePair<?, ?>) {
            BatchedKeyValuePair event1 = (BatchedKeyValuePair) event;
                if (event1.getDb() == null)
                    return;
                StringBuffer info = new StringBuffer();

                JDJedis targetJedisplus= null;
                try {
                    long newTime=0L;
                    if(event1.getExpiredMs()!=null){
                        newTime=event1.getExpiredMs()-System.currentTimeMillis();
                    }

                    if(event1.getValue()!=null){
                        threadPoolTaskExecutor.submit(new BatchedClusterRestoreTask(event, newTime, new String((byte[]) event1.getKey()), info, redisClient, getRedisCommandTypeEnum(event1.getValueRdbType())));
                    }



                } catch (Exception e) {
                    //mapping映射中不存在关系，放弃当前 kv数据同步
                }




        }

    }

    RedisCommandTypeEnum getRedisCommandTypeEnum(int rdbType){
        if (rdbType == Constants.RDB_TYPE_SET){
            return RedisCommandTypeEnum.SET;
        }else if(rdbType== Constants.RDB_TYPE_ZSET){
            return RedisCommandTypeEnum.ZSET;
        }else if(rdbType== Constants.RDB_TYPE_STRING){
            return RedisCommandTypeEnum.STRING;
        }else if(rdbType== Constants.RDB_TYPE_HASH){
            return RedisCommandTypeEnum.HASH;
        }else if(rdbType== Constants.RDB_TYPE_LIST){
            return RedisCommandTypeEnum.LIST;
        }

        return RedisCommandTypeEnum.STRING;

    }


    public synchronized static JDJedis getJDJedis( JDJedisClientPool targetJedisClientPool,  BatchedKeyValuePair event,Map<Integer, Integer> dbMap) throws Exception {

        DB db = event.getDb();
        int dbbnum = (int) db.getDbNumber();
        if (null != dbMap && dbMap.size() > 0) {
            if (dbMap.containsKey((int) db.getDbNumber())) {
                dbbnum = dbMap.get((int) db.getDbNumber());
            } else {
               throw new Exception("mapping映射对应信息不存在，当前数据不执行");
            }
        }

        StringBuffer info = new StringBuffer();
        int index;

        JDJedis targetJedisplus = null;
        try {
            targetJedisplus = targetJedisClientPool.getResource();
        } catch (Exception e) {
            log.warn("RDB复制 ：从池中获取RedisClient失败（准备重试）" + e.getMessage());
            try {
                targetJedisplus = targetJedisClientPool.getResource();
            } catch (Exception ex) {
                log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
            }

        }
        
        if (db != null && (index = dbbnum) != targetJedisplus.getDbNum()) {
            try {
                targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
            } catch (Exception e) {
                log.warn("RDB复制：从池中获取链接 失败(重试): {}", e.getMessage());
                try {
                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                } catch (Exception exx) {
                    log.warn("RDB复制: 从池中获取链接 失败(重试失败): {}", exx.getMessage());
                }
            }

            info.append("SELECT:");
            info.append(index);
            log.info(info.toString());
        }

        return targetJedisplus;
    }


}
