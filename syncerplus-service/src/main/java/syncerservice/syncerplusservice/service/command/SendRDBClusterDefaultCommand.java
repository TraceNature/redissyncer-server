package syncerservice.syncerplusservice.service.command;


import syncerservice.syncerplusredis.cmd.impl.DefaultCommand;
import syncerservice.syncerplusredis.event.Event;
import syncerservice.syncerplusredis.replicator.Replicator;
import syncerservice.syncerplusredis.entity.thread.OffSetEntity;
import syncerservice.syncerplusservice.task.clusterTask.command.CommitClusterSendTask;
import syncerservice.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 增量传输
 */
@Slf4j
public class SendRDBClusterDefaultCommand {



    public void sendDefaultCommand(Event event, Replicator r, JedisClusterPlus redisClient, ThreadPoolTaskExecutor threadPoolTaskExecutor, String taskId, OffSetEntity baseOffSet, Map<Integer, Integer> getDbNum,
                                   AtomicBoolean commandDbStatus){
        /**
         * 命令同步
         */
        Object res=null;

        if (event instanceof DefaultCommand) {

            baseOffSet.setReplId(r.getConfiguration().getReplId());
            baseOffSet.getReplOffset().set(r.getConfiguration().getReplOffset());
            DefaultCommand dc = (DefaultCommand) event;

            if(Arrays.equals(dc.getCommand(),"SELECT".getBytes())) {
                int commDbNum = Integer.parseInt(new String(dc.getArgs()[0]));
                if (getDbNum == null || getDbNum.size() == 0) {
                    commandDbStatus.set(true);
                } else {
                    if (getDbNum.containsKey(commDbNum)) {
                        commandDbStatus.set(true);
                        return;
                    } else {
                        commandDbStatus.set(false);
                        return;
                    }

                }

            }

            if(!commandDbStatus.get()){
                return;
            }
            StringBuffer info = new StringBuffer();


//            if(!new String(dc.getCommand()).trim().toUpperCase().equals("SELECT")){

                threadPoolTaskExecutor.submit(new CommitClusterSendTask(dc, redisClient, info));

//            }
        }
    }


}
