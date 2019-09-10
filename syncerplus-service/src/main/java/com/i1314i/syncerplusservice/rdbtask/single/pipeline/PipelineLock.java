package com.i1314i.syncerplusservice.rdbtask.single.pipeline;

import com.i1314i.syncerplusservice.entity.SyncTaskEntity;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.params.SetParams;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
@Slf4j
public class PipelineLock {
    private   Lock lock=new ReentrantLock();
    private Pipeline pipeline;
    private SyncTaskEntity taskEntity;
    private JDJedis targetJedisplus;
    private  JDJedisClientPool targetJedisClientPool;
    @Getter
    private String taskId;
    public PipelineLock(Pipeline pipeline,SyncTaskEntity taskEntity,String taskId,JDJedis targetJedisplus, JDJedisClientPool targetJedisClientPool) {
        this.pipeline = pipeline;
        this.taskEntity=taskEntity;
        this.taskId=taskId;
        this.targetJedisplus=targetJedisplus;
        this.targetJedisClientPool=targetJedisClientPool;
    }

    public  List<Object> syncAndReturnAll(){
        try {
            lock.lock();
            log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());
            taskEntity.clear();
           return pipeline.syncAndReturnAll();

        }finally {
            lock.unlock();
        }
    }
    public  void sync(){
        try {
            lock.lock();
//            log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());
            taskEntity.clear();
            pipeline.sync();
        }finally {
            lock.unlock();
        }
    }

    public  Response<Long> append(byte[] key, byte[] value){
        try {
            lock.lock();
            return pipeline.append(key,value);
        }finally {
            lock.unlock();
        }
    }

    public  Response<String>  set(byte[] key, byte[] value){
        try {
            lock.lock();
            return pipeline.set(key,value);
        }finally {
            lock.unlock();
        }
    }
    public  Response<String>  set(byte[] key, byte[] value, SetParams params){
        try {
            lock.lock();
            return pipeline.set(key,value,params);
        }finally {
            lock.unlock();
        }
    }



    public  Response<Long> lpush(byte[] key, byte[]... string){
        try {
            lock.lock();
            return pipeline.lpush(key,string);
        }finally {
            lock.unlock();
        }
    }

    public  Response<Long> sadd(byte[] key, byte[]... member){
        try {
            lock.lock();
            return pipeline.sadd(key,member);
        }finally {
            lock.unlock();
        }
    }


    public void close(){
        pipeline.sync();
        pipeline.close();
        if(targetJedisplus!=null){
            targetJedisplus.close();
        }

        if(targetJedisClientPool!=null){
            targetJedisClientPool.closePool();
        }

    }


    public  Response<Long> zadd(byte[] key,   Map<byte[], Double> scoreMembers){
        try {
            lock.lock();
            return pipeline.zadd(key,scoreMembers);
        }finally {
            lock.unlock();
        }
    }
    public  Response<Object> sendCommand(ProtocolCommand var1, byte[]... var2){
        try {
            lock.lock();
            return pipeline.sendCommand(var1,var2);
        }finally {
            lock.unlock();
        }
    }


    public  Response<Long> del(byte[] key){
        try {
            lock.lock();
            return pipeline.del(key);
        }finally {
            lock.unlock();
        }
    }

    public  Response<Long> pexpire(byte[] key,long seconds){
        try {
            lock.lock();
            return pipeline.pexpire(key,seconds);
        }finally {
            lock.unlock();
        }
    }

    public  Response<String>  hmset(byte[] var1, Map<byte[], byte[]> var2){
        try {
            lock.lock();
            return pipeline.hmset(var1,var2);
        }finally {
            lock.unlock();
        }
    }

    public  Response<String> restore(byte[] key, int ttl, byte[] serializedValue) {
        try {
            lock.lock();
            return pipeline.restore(key,ttl,serializedValue);
        }finally {
            lock.unlock();
        }
    }
    public  Response<String> restoreReplace(byte[] key, int ttl, byte[] serializedValue) {
        try {
            lock.lock();
            return pipeline.restoreReplace(key,ttl,serializedValue);
        }finally {
            lock.unlock();
        }
    }

    public  Response<String> ping() {
        try {
            lock.lock();
            return pipeline.ping();
        }finally {
            lock.unlock();
        }
    }

    public  void clear() {
        try {
            lock.lock();
          pipeline.clear();
        }finally {
            lock.unlock();
        }
    }
}
