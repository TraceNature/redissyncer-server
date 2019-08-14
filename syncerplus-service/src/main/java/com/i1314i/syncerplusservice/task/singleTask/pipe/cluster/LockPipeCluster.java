package com.i1314i.syncerplusservice.task.singleTask.pipe.cluster;

import com.i1314i.syncerplusservice.entity.SyncTaskEntity;
import com.i1314i.syncerplusservice.util.Jedis.cluster.pipelineCluster.JedisClusterPipeline;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.Date;

@Slf4j
public class LockPipeCluster {
    private  Date date=new Date();
    private  long time;

    public  synchronized void syncpipe(JedisClusterPipeline pipelined, SyncTaskEntity taskEntity, int num, boolean type){
        if (pipelined!=null){

            if(type){
                if(taskEntity.getSyncNums()>=num){
                    pipelined.sync();
                    log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());
                    taskEntity.clear();
                    date=new Date();
                }
            }else {
                time=new Date().getTime()-date.getTime();
                if(taskEntity.getSyncNums()>=num&&time>5000){
                    pipelined.sync();
                    log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());
                    taskEntity.clear();
                    date=new Date();
                }else if(taskEntity.getSyncNums()==0&&time>4000){

//                    Response<String>r= pipelined.ping();
                    pipelined.sync();
                    taskEntity.clear();
//                    log.info("ping->{}", r.get());
                } else if(taskEntity.getSyncNums()==0&&time>180000){
                    pipelined.close();
                    log.info("pipelined is close}");

                }
            }

        }
    }
}
