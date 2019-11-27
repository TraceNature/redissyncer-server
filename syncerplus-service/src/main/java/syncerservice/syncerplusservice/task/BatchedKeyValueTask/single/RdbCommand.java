package syncerservice.syncerplusservice.task.BatchedKeyValueTask.single;


import syncerservice.syncerplusredis.event.Event;
import syncerservice.syncerplusredis.event.PostRdbSyncEvent;
import syncerservice.syncerplusredis.event.PreRdbSyncEvent;
import syncerservice.syncerplusredis.rdb.datatype.DB;
import syncerservice.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncerservice.syncerplusredis.replicator.Replicator;
import syncerservice.syncerplusservice.rdbtask.enums.RedisCommandType;
import syncerservice.syncerplusservice.util.Jedis.JDJedis;
import syncerservice.syncerplusservice.util.Jedis.pool.JDJedisClientPool;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;



@Slf4j
public class RdbCommand {
    private boolean status = true;

    public RdbCommand() {
        if (status) {
            this.status = false;
        }
    }

    public void sendRestoreDumpData(Event event, Replicator r, ThreadPoolTaskExecutor threadPoolTaskExecutor, JDJedisClientPool targetJedisClientPool, String threadName, Map<Integer, Integer> dbMap) {

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
                DB db=event1.getDb();

                JDJedis targetJedisplus= null;
                try {

                    targetJedisplus = RedisCommandType.getJDJedis(targetJedisClientPool,event1,dbMap);

                    long newTime=0L;
                    if(event1.getExpiredMs()!=null){
                        newTime=event1.getExpiredMs()-System.currentTimeMillis();
                    }

                    if(event1.getValue()!=null){

                        threadPoolTaskExecutor.submit(new BatchedRestoreTask(event, newTime, new String((byte[]) event1.getKey()), info, targetJedisplus, RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType())));
                    }


                } catch (Exception e) {
                    //mapping映射中不存在关系，放弃当前 kv数据同步
                }
        }

    }






}
