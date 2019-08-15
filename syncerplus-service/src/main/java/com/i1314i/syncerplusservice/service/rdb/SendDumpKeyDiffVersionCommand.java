package com.i1314i.syncerplusservice.service.rdb;

import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.task.singleTask.diffVersion.defaultVersion.RdbDiffVersionInsertPlusRestoreTask;
import com.i1314i.syncerplusservice.task.singleTask.sameVersion.defaultVersion.RdbSameVersionRestoreTask;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.*;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static redis.clients.jedis.Protocol.Command.SELECT;
import static redis.clients.jedis.Protocol.toByteArray;

@Slf4j
public class SendDumpKeyDiffVersionCommand {
    private boolean status = true;
    final AtomicInteger dbnum = new AtomicInteger(-1);

    public SendDumpKeyDiffVersionCommand() {
        if (status) {
            this.status = false;
        }
    }

    public void sendRestoreDumpData(Event event, Replicator r, ThreadPoolTaskExecutor threadPoolTaskExecutor, JDJedisClientPool targetJedisClientPool, String threadName, Map<Integer,Integer> dbMap) {

        if(event instanceof PreRdbSyncEvent){
            log.info("{} :全量同步启动",threadName);
        }

        if(event instanceof PostRdbSyncEvent){
            log.info("{} :全量同步结束",threadName);
        }


        if(event instanceof KeyStringValueString) {
            RedisUrlUtils.doCheckTask(r, Thread.currentThread());
            if (RedisUrlUtils.doThreadisCloseCheckTask())
                return;
            KeyStringValueString kv = (KeyStringValueString) event;
            if (kv.getDb() == null)
                return;
            DB db = kv.getDb();


            int dbbnum= (int) db.getDbNumber();

            if(null!=dbMap&&dbMap.size()>0){
                if(dbMap.containsKey((int)db.getDbNumber())){
                    dbbnum=dbMap.get((int)db.getDbNumber());
                }else {
                    return;
                }
            }


            StringBuffer info = new StringBuffer();
            int index;
            JDJedis targetJedisplus = null;

            try {
                targetJedisplus = targetJedisClientPool.getResource();
            } catch (Exception e) {
                log.warn("RDB复制 ：从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                try {
                    targetJedisplus = targetJedisClientPool.getResource();
                }catch (Exception ex){
                    log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                }

            }




//            db.setDbNumber();
//            (index = (int) db.getDbNumber()) != dbnum.get()

            if (db != null && (index =dbbnum) != targetJedisplus.getDbNum()) {
                try {
                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                } catch (Exception e) {
                    log.warn("RDB复制： 从池中获取链接 失败(重试): {}" , e.getMessage());
                    try {
                        targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                    }catch (Exception exx){
                        log.warn("RDB复制： 从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                    }
                }
                dbnum.set(index);
                info.append("SELECT:");
                info.append(index);
                log.info(info.toString());
            }


            threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.STRING));


        } else if (event instanceof KeyStringValueList) {
            RedisUrlUtils.doCheckTask(r, Thread.currentThread());
            if (RedisUrlUtils.doThreadisCloseCheckTask())
                return;
            KeyStringValueList kv = (KeyStringValueList) event;
            if (kv.getDb() == null)
                return;
            DB db = kv.getDb();

            int dbbnum= (int) db.getDbNumber();

            if(null!=dbMap&&dbMap.size()>0){
                if(dbMap.containsKey((int)db.getDbNumber())){
                    dbbnum=dbMap.get((int)db.getDbNumber());
                }else {
                    return;
                }
            }


            StringBuffer info = new StringBuffer();
            int index;
            JDJedis targetJedisplus = null;
            try {
                targetJedisplus =  targetJedisClientPool.getResource();
            } catch (Exception e) {
                log.warn("RDB复制： 从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                try {
                    targetJedisplus =  targetJedisClientPool.getResource();
                }catch (Exception ex){
                    log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                }

            }

            if (db != null && (index =dbbnum) != targetJedisplus.getDbNum()) {
                try {
                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                } catch (Exception e) {
                    log.warn("RDB复制：  从池中获取链接 失败(重试): {}" , e.getMessage());
                    try {
                        targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                    }catch (Exception exx){
                        log.warn("RDB复制： 从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                    }
                }
                dbnum.set(index);
                info.append("SELECT:");
                info.append(index);
                log.info(info.toString());
            }


            threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.LIST));


        } else if (event instanceof KeyStringValueSet) {
            RedisUrlUtils.doCheckTask(r, Thread.currentThread());
            if (RedisUrlUtils.doThreadisCloseCheckTask())
                return;
            KeyStringValueSet kv = (KeyStringValueSet) event;
            if (kv.getDb() == null)
                return;
            DB db = kv.getDb();

            int dbbnum= (int) db.getDbNumber();

            if(null!=dbMap&&dbMap.size()>0){
                if(dbMap.containsKey((int)db.getDbNumber())){
                    dbbnum=dbMap.get((int)db.getDbNumber());
                }else {
                    return;
                }
            }

            StringBuffer info = new StringBuffer();
            int index;
            JDJedis targetJedisplus = null;
            try {
                targetJedisplus =  targetJedisClientPool.getResource();
            } catch (Exception e) {
                log.warn("RDB复制：从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                try {
                    targetJedisplus = targetJedisClientPool.getResource();
                }catch (Exception ex){
                    log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                }

            }


            if (db != null && (index = dbbnum) != targetJedisplus.getDbNum()) {
                try {
                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                } catch (Exception e) {

                    log.warn("RDB复制：从池中获取链接 失败(重试): {}" , e.getMessage());
                    try {
                        targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                    }catch (Exception exx){
                        log.warn("RDB复制： 从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                    }
                }
                dbnum.set(index);
                info.append("SELECT:");
                info.append(index);
                log.info(info.toString());
            }


            threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.SET));



        }else if (event instanceof KeyStringValueZSet) {
            RedisUrlUtils.doCheckTask(r, Thread.currentThread());
            if (RedisUrlUtils.doThreadisCloseCheckTask())
                return;
            KeyStringValueZSet kv = (KeyStringValueZSet) event;
            if (kv.getDb() == null)
                return;
            DB db = kv.getDb();

            int dbbnum= (int) db.getDbNumber();

            if(null!=dbMap&&dbMap.size()>0){
                if(dbMap.containsKey((int)db.getDbNumber())){
                    dbbnum=dbMap.get((int)db.getDbNumber());
                }else {
                    return;
                }
            }

            StringBuffer info = new StringBuffer();
            int index;
            JDJedis targetJedisplus = null;
            try {
                targetJedisplus = targetJedisClientPool.getResource();
            } catch (Exception e) {
                log.warn("RDB复制：从池中获取RedisClient失败（准备重试） ：" + e.getMessage());
                try {
                    targetJedisplus = targetJedisClientPool.getResource();
                }catch (Exception ex){
                    log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                }

            }

            if (db != null && (index =dbbnum) != targetJedisplus.getDbNum()) {

                try {
                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                } catch (Exception e) {
                    log.warn("RDB复制： 从池中获取链接 失败(重试): {}" , e.getMessage());
                    try {
                        targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                    }catch (Exception exx){
                        log.warn("RDB复制：从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                    }
                }
//                dbnum.set(index);
                info.append("SELECT:");
                info.append(index);
                log.info(info.toString());
            }


            threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.ZSET));



        }else if (event instanceof KeyStringValueHash) {

            RedisUrlUtils.doCheckTask(r, Thread.currentThread());
            if (RedisUrlUtils.doThreadisCloseCheckTask())
                return;
            KeyStringValueHash kv = (KeyStringValueHash) event;
            if (kv.getDb() == null)
                return;
            DB db = kv.getDb();

            int dbbnum= (int) db.getDbNumber();

            if(null!=dbMap&&dbMap.size()>0){
                if(dbMap.containsKey((int)db.getDbNumber())){
                    dbbnum=dbMap.get((int)db.getDbNumber());
                }else {
                    return;
                }
            }

            StringBuffer info = new StringBuffer();
            int index;
            JDJedis targetJedisplus = null;
            try {
                targetJedisplus = targetJedisClientPool.getResource();
            } catch (Exception e) {
                log.warn("RDB复制:从池中获取RedisClient失败（准备重试）：" + e.getMessage());
                try {
                    targetJedisplus = targetJedisClientPool.getResource();
                }catch (Exception ex){
                    log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
                }

            }


            if (db != null && (index =dbbnum) != targetJedisplus.getDbNum()) {
                try {
                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                } catch (Exception e) {
                    log.warn("RDB复制：从池中获取链接 失败(重试): {}" , e.getMessage());
                    try {
                        targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                    }catch (Exception exx){
                        log.warn("RDB复制：从池中获取链接 失败(重试失败): {}" , exx.getMessage());
                    }
                }
                dbnum.set(index);
                info.append("SELECT:");
                info.append(index);
                log.info(info.toString());
            }


            threadPoolTaskExecutor.submit(new RdbDiffVersionInsertPlusRestoreTask(event, kv.getExpiredMs(),new String(kv.getKey()) ,info,targetJedisplus, RedisCommandTypeEnum.HASH));




        } else if (event instanceof KeyStringValueModule) {
            log.warn("暂不支持Module");
        } else if (event instanceof KeyStringValueStream) {
            log.warn("暂不支持Stream");
        }
    }
}
