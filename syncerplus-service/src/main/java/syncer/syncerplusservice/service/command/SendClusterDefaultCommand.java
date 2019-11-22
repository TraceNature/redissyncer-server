package syncer.syncerplusservice.service.command;


import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusservice.task.clusterTask.command.CommitClusterSendTask;
import syncer.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;

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


            StringBuffer info = new StringBuffer();
            // Step3: sync aof command
            DefaultCommand dc = (DefaultCommand) event;

            if(!new String(dc.getCommand()).trim().toUpperCase().equals("SELECT")){

                threadPoolTaskExecutor.submit(new CommitClusterSendTask(dc, redisClient, info));

            }
        }
    }


}
