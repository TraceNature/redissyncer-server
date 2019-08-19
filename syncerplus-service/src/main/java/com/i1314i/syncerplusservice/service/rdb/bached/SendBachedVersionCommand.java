package com.i1314i.syncerplusservice.service.rdb.bached;

import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.task.singleTask.diffVersion.defaultVersion.RdbDiffVersionInsertPlusRestoreTask;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 大key-value数据迁移
 */
@Slf4j
public class SendBachedVersionCommand {
    private boolean status = true;
    final AtomicInteger dbnum = new AtomicInteger(-1);

    public SendBachedVersionCommand() {
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


    }
}
