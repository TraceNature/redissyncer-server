package com.i1314i.syncerplusservice.task.clusterTask.command;


import com.i1314i.syncerplusredis.cmd.impl.DefaultCommand;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import lombok.extern.slf4j.Slf4j;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;


import java.util.concurrent.Callable;


/**
 * cluster命令传播Task
 */
@Slf4j
public class CommitClusterSendTask implements Callable<Object> {

    private DefaultCommand command;
    private JedisClusterPlus redisClient;
    private StringBuffer info;


    public CommitClusterSendTask(DefaultCommand command, JedisClusterPlus redisClient, StringBuffer info) {
        this.command = command;
        this.redisClient = redisClient;
        this.info = info;
    }

    /**
     * 缺少校验
     * @return
     * @throws Exception
     */
    @Override
    public Object call() throws Exception {
        try {
            Object r=null;



            ClusterProtocolCommand protocolCommand=new ClusterProtocolCommand(command.getCommand());


            r= redisClient.sendCommand(command.getArgs()[0], protocolCommand, command.getArgs());



            info.append(new String(command.getCommand()));
            info.append(":");
            for (byte[] arg : command.getArgs()) {
                info.append(" [");
                info.append(new String(arg));
                info.append("]");
            }


            info.append("->");
            if(r instanceof Long){
                info.append(r);
            }else if(r instanceof Integer){
                info.append(r);
            }else if (r instanceof byte[]){
                info.append(new String((byte[]) r));
            }else if(r instanceof String){
                info.append(r);
            }else {
                info.append(r);
            }

            log.info(info.toString());
            return r;
        }finally {
//            redisClient.close();
        }



    }


}
