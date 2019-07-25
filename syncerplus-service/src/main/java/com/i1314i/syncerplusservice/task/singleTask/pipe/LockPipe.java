package com.i1314i.syncerplusservice.task.singleTask.pipe;

import com.i1314i.syncerplusservice.entity.SyncTaskEntity;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Pipeline;

import java.util.Date;

@Slf4j
public class LockPipe {
    private  Date date=new Date();
    private  long time;

    public  synchronized void syncpipe(Pipeline pipelined,SyncTaskEntity taskEntity,int num,boolean type){
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
                }
            }

        }
    }
}
