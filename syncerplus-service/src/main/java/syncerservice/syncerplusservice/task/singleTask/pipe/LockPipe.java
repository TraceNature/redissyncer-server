package syncerservice.syncerplusservice.task.singleTask.pipe;

import syncerservice.syncerpluscommon.config.ThreadPoolConfig;
import syncerservice.syncerpluscommon.util.spring.SpringUtil;
import syncerservice.syncerplusredis.entity.RedisURI;
import syncerservice.syncerplusservice.compensator.single.PipelineCompensator;
import syncerservice.syncerplusredis.entity.EventEntity;
import syncerservice.syncerplusredis.entity.SyncTaskEntity;

import syncerservice.syncerplusservice.rdbtask.single.pipeline.PipelineLock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;

    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }

    private  volatile int dbNum;

    public  synchronized void syncpipe(Pipeline pipelined,SyncTaskEntity taskEntity,int num,boolean type){
        if (pipelined!=null){

            if(type){
                if(taskEntity.getSyncNums()>=num){
//                    pipelined.sync();
                    List<Object>resultList=pipelined.syncAndReturnAll();
//                    System.out.println(JSON.toJSONString(resultList));
                    resultList.clear();
                    log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());



                    taskEntity.clear();
                    date=new Date();
                }
            }else {
                time=new Date().getTime()-date.getTime();
                if(taskEntity.getSyncNums()>=num&&time>5000){
//                    pipelined.sync();
                    List<Object>resultList=pipelined.syncAndReturnAll();
//                    System.out.println(JSON.toJSONString(resultList));
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


    public  synchronized void syncpipe(PipelineLock pipelineLock, SyncTaskEntity taskEntity, int num, boolean type, RedisURI suri, RedisURI turi){

        if (pipelineLock!=null){

            if(type){
                if(taskEntity.getSyncNums()>=num){
//                    pipelined.sync();
                    List<Object>resultList=pipelineLock.syncAndReturnAll();
//                    System.out.println(JSON.toJSONString(resultList));

                    List<EventEntity>eventEntities=new ArrayList<>();
                    eventEntities.clear();
                    eventEntities.addAll(taskEntity.getKeys());

                    taskEntity.getKeys().clear();

                    threadPoolTaskExecutor.execute(new  PipelineCompensator(new ArrayList<>(resultList),eventEntities,suri,turi,pipelineLock.getTaskId()));

//                    PipelineCompensator.singleCompensator(resultList,eventEntities,suri,turi,pipelineLock.getTaskId());

                    resultList.clear();
                    date=new Date();
                }
            }else {
                time=new Date().getTime()-date.getTime();
                if(taskEntity.getSyncNums()>=num&&time>5000){
//                    pipelined.sync();
                    List<Object>resultList=pipelineLock.syncAndReturnAll();
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
