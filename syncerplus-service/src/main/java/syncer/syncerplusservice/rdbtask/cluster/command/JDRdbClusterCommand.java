package syncer.syncerplusservice.rdbtask.cluster.command;


import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.PostRdbSyncEvent;
import syncer.syncerplusredis.event.PreRdbSyncEvent;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;

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
