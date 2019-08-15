package com.i1314i.syncerplusservice.task.clusterTask.cluster;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.entity.dto.common.SyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.task.singleTask.sameVersion.defaultVersion.RdbSameVersionRestoreTask;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.JedisCluster;

import java.util.concurrent.atomic.AtomicInteger;



@Slf4j
public class SendClusterDumpKeySameVersionCommand {
    private boolean status = true;
    final AtomicInteger dbnum = new AtomicInteger(-1);

    public SendClusterDumpKeySameVersionCommand() {
        if (status) {
            this.status = false;
        }
    }

    public void sendRestoreDumpData(Event event, Replicator r, JedisClusterPlus redisClient, ThreadPoolTaskExecutor threadPoolTaskExecutor, String threadName, SyncDataDto syncDataDto){

        if(event instanceof PreRdbSyncEvent){
            log.info("{} :全量同步启动",threadName);
        }

        if(event instanceof PostRdbSyncEvent){
            log.info("{} :全量同步结束",threadName);
        }


        if (event instanceof DumpKeyValuePair) {
            DumpKeyValuePair kv = (DumpKeyValuePair) event;

            StringBuffer info = new StringBuffer();

            RedisUrlUtils.doCheckTask(r, Thread.currentThread());

            if (RedisUrlUtils.doThreadisCloseCheckTask())
                return;


            DB db=kv.getDb();

            int dbbnum= (int) db.getDbNumber();

            if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                    dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                }else {
                    return;
                }
            }

            // Step1: select db

            DumpKeyValuePair mkv =kv;

            info.setLength(0);



            if (mkv.getExpiredMs() == null) {
                threadPoolTaskExecutor.submit(new RdbClusterSameVersionRestoreTask(mkv, 0L, redisClient, true, info));
            } else {
                long ms = mkv.getExpiredMs() - System.currentTimeMillis();

                if (ms <= 0) return;
//                      threadPoolTaskExecutor.submit(new RdbRestoreTask(mkv, ms, redisClient,pool, true,info,targetJedisplus,sourceJedisplus));
                threadPoolTaskExecutor.submit(new RdbClusterSameVersionRestoreTask(mkv, ms, redisClient, true, info));
            }


        }

    }

}
