package com.i1314i.syncerplusservice.task.singleTask.pipe;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.compensator.single.PipelineCompensator;
import com.i1314i.syncerplusservice.entity.EventEntity;
import com.i1314i.syncerplusservice.entity.SyncTaskEntity;

import com.i1314i.syncerplusservice.rdbtask.single.pipeline.PipelineLock;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class LockPipe {
    private  Date date=new Date();
    private  long time;
    List<EventEntity> eventEntityList=new ArrayList<>();


    private  volatile int dbNum;

    public  synchronized void syncpipe(Pipeline pipelined,SyncTaskEntity taskEntity,int num,boolean type){
        if (pipelined!=null){

            if(type){
                if(taskEntity.getSyncNums()>=num){
//                    pipelined.sync();
                    List<Object>resultList=pipelined.syncAndReturnAll();
                    System.out.println(JSON.toJSONString(resultList));
                    resultList.clear();
                    log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());
                    List<EventEntity> eventEntityList=new ArrayList<>();

//                    BeanUtils.copyProperties();
                    taskEntity.clear();
                    date=new Date();
                }
            }else {
                time=new Date().getTime()-date.getTime();
                if(taskEntity.getSyncNums()>=num&&time>5000){
//                    pipelined.sync();
                    List<Object>resultList=pipelined.syncAndReturnAll();
                    System.out.println(JSON.toJSONString(resultList));
                    resultList.clear();
                    log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());
                    taskEntity.clear();
                    date=new Date();
                }else if(taskEntity.getSyncNums()==0&&time>4000){
                    Response<String>r= pipelined.ping();
                    pipelined.sync();
                    taskEntity.clear();
                    log.info("ping->{}", r.get());
                }

//                else if(taskEntity.getSyncNums()==0&&time>180000){
////                    pipelined.close();
//                    log.info("pipelined is close}");
//
//                }
            }

        }
    }


    public  synchronized void syncpipe(PipelineLock pipelineLock, SyncTaskEntity taskEntity, int num, boolean type, RedisURI suri,RedisURI turi){
        if (pipelineLock!=null){

            if(type){
                if(taskEntity.getSyncNums()>=num){
//                    pipelined.sync();
                    List<Object>resultList=pipelineLock.syncAndReturnAll();
                    System.out.println("result: "+resultList.size());
//                    System.out.println(JSON.toJSONString(resultList));
                    List<EventEntity>eventEntities=new ArrayList<>();
                    eventEntities.addAll(taskEntity.getKeys());

                    //BeanUtils.copyProperties(taskEntity.getKeys(),eventEntities);
                    taskEntity.getKeys().clear();

                    PipelineCompensator.singleCompensator(resultList,eventEntities,suri,turi,pipelineLock.getTaskId());
                    System.out.println("keys: "+eventEntities.size());
//                    System.out.println(JSON.toJSONString(eventEntities));



                    resultList.clear();

                    List<EventEntity> eventEntityList=new ArrayList<>();

//                    BeanUtils.copyProperties();
                    date=new Date();
                }
            }else {
                time=new Date().getTime()-date.getTime();
                if(taskEntity.getSyncNums()>=num&&time>5000){
//                    pipelined.sync();
                    List<Object>resultList=pipelineLock.syncAndReturnAll();
                    System.out.println(resultList.size());
                    System.out.println(taskEntity.getKeys().size());
                    resultList.clear();
//                    log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());

                    date=new Date();
                }else if(taskEntity.getSyncNums()==0&&time>4000){
                    Response<String>r= pipelineLock.ping();
                    pipelineLock.sync();
                    log.info("ping->{}", r.get());
                }

//                else if(taskEntity.getSyncNums()==0&&time>180000){
////                    pipelined.close();
//                    log.info("pipelined is close}");
//
//                }
            }

        }
    }
    public int getDbNum() {
        return dbNum;
    }

    public synchronized void setDbNum(int dbNum) {
        this.dbNum = dbNum;
    }
}
