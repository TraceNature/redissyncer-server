package com.i1314i.syncerplusservice.service.rdb;
import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.entity.dto.common.SyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.task.singleTask.sameVersion.defaultVersion.RdbSameVersionRestoreTask;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import static redis.clients.jedis.Protocol.Command.SELECT;
import static redis.clients.jedis.Protocol.toByteArray;

@Slf4j
public class SendDumpKeySameVersionCommand {
    private boolean status = true;
    final AtomicInteger dbnum = new AtomicInteger(-1);

    public SendDumpKeySameVersionCommand() {
        if (status) {
            this.status = false;
        }
    }

    public void sendRestoreDumpData(Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor, String threadName, SyncDataDto syncDataDto){

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


            // Step1: select db
            DB db = kv.getDb();


            int dbbnum= (int) db.getDbNumber();

            if(null!=syncDataDto.getDbNum()&&syncDataDto.getDbNum().size()>0){
                if(syncDataDto.getDbNum().containsKey((int)db.getDbNumber())){
                    dbbnum=syncDataDto.getDbNum().get((int)db.getDbNumber());
                }else {
                    return;
                }
            }


            RedisClient redisClient = null;
            int index;
            try {
                redisClient = pool.borrowResource();
            } catch (Exception e) {
                log.info("RDB复制：从池中获取RedisClient失败 ：{}" , e.getMessage());
            }
            if (db != null && (index = dbbnum) !=(int)redisClient.getDbNum()) {
                status = true;

                try {
//                    redisClient.send(SELECT, toByteArray(index));
                    redisClient.selectDB(index);
                } catch (Exception e) {
                    log.info("RDB复制： 从池中获取链接失败 : {}" ,e.getMessage());
                }
                dbnum.set(index);
                info.append("SELECT:");
                info.append(index);
                log.info(info.toString());
            }
            info.setLength(0);
            //threadPoolTaskExecutor.execute(new SyncTask(replicator,kv,target,dbnum));
            // Step2: restore dump data
            DumpKeyValuePair mkv =kv;

            if (mkv.getExpiredMs() == null) {
                threadPoolTaskExecutor.submit(new RdbSameVersionRestoreTask(mkv, 0L, redisClient, pool, true, info));
            } else {
                long ms = mkv.getExpiredMs() - System.currentTimeMillis();
                if (ms <= 0) return;
//                      threadPoolTaskExecutor.submit(new RdbRestoreTask(mkv, ms, redisClient,pool, true,info,targetJedisplus,sourceJedisplus));
                threadPoolTaskExecutor.submit(new RdbSameVersionRestoreTask(mkv, ms, redisClient, pool, true, info));
            }


        }

    }


}
