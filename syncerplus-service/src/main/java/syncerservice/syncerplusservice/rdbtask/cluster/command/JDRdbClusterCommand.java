package syncerservice.syncerplusservice.rdbtask.cluster.command;


import syncerservice.syncerplusredis.event.Event;
import syncerservice.syncerplusredis.event.PostRdbSyncEvent;
import syncerservice.syncerplusredis.event.PreRdbSyncEvent;
import syncerservice.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncerservice.syncerplusredis.replicator.Replicator;
import syncerservice.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Slf4j
public class JDRdbClusterCommand {
    private boolean status = true;

    public JDRdbClusterCommand() {
        if (status) {
            this.status = false;
        }
    }
//    SendClusterRdbCommand clusterRdbCommand=new SendClusterRdbCommand();
    public void sendRestoreDumpData(Event event, Replicator r, ThreadPoolTaskExecutor threadPoolTaskExecutor, JedisClusterPlus redisClient, String threadName, double redisVersion) {

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

                try {
                    long newTime=0L;
                    if(event1.getExpiredMs()!=null){
                        newTime=event1.getExpiredMs()-System.currentTimeMillis();
                    }

                    if(event1.getValue()!=null){

//                        clusterRdbCommand.sendCommand(newTime,RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,redisClient, new String((byte[]) event1.getKey()));
                    }



                } catch (Exception e) {
                    //mapping映射中不存在关系，放弃当前 kv数据同步
                }




        }

    }




}
