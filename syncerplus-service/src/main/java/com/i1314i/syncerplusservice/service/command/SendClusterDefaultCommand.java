package com.i1314i.syncerplusservice.service.command;


import com.i1314i.syncerplusservice.task.clusterTask.command.CommitClusterSendTask;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * 增量传输
 */
@Slf4j
public class SendClusterDefaultCommand {



    public void sendDefaultCommand(Event event, Replicator r, JedisClusterPlus redisClient, ThreadPoolTaskExecutor threadPoolTaskExecutor){
        /**
         * 命令同步
         */
        Object res=null;

        if (event instanceof DefaultCommand) {

            RedisUrlUtils.doCommandCheckTask(r);
            if (RedisUrlUtils.doThreadisCloseCheckTask()) {
                return;
            }

            StringBuffer info = new StringBuffer();
            // Step3: sync aof command
            DefaultCommand dc = (DefaultCommand) event;

            if(!new String(dc.getCommand()).trim().toUpperCase().equals("SELECT")){

                threadPoolTaskExecutor.submit(new CommitClusterSendTask(dc, redisClient, info));

            }
        }
    }


}
