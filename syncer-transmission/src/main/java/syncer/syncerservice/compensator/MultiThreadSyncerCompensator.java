package syncer.syncerservice.compensator;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.constant.CmdEnum;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.po.StringCompensatorEntity;
import syncer.syncerservice.util.CompensatorUtils;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerservice.util.queue.LocalMemoryQueue;
import syncer.syncerservice.util.queue.SyncerQueue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多线程补偿
 */

@Slf4j
public class MultiThreadSyncerCompensator implements ISyncerCompensator{

   private  String taskId;
   private Map<String,Long>incrMap=new ConcurrentHashMap<>();
   private Map<String, StringCompensatorEntity>appendMap=new ConcurrentHashMap<>();
   private CompensatorUtils compensatorUtils=new CompensatorUtils();
   private JDRedisClient client;

    public MultiThreadSyncerCompensator(String taskId, JDRedisClient client) {
        this.taskId = taskId;
        this.client = client;
    }

    @Override
    public void set(Long dbNum, byte[] key, byte[] value, String res) {
        if(compensatorUtils.isStringSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isStringSuccess(client.set(dbNum,key,value))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[set]---->value{}",taskId, StringUtils.toString(key), StringUtils.toString(value));
        }
    }

    @Override
    public void set(Long dbNum, byte[] key, byte[] value, long ms, String res) {
        if(compensatorUtils.isStringSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isStringSuccess(client.set(dbNum,key,value,ms))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[set - time]---->value{}",taskId, StringUtils.toString(key), StringUtils.toString(value));
        }
    }

    @Override
    public void append(Long dbNum, byte[] key, byte[] value, String res) {
        String stringKey=StringUtils.toString(key);
        if(appendMap.containsKey(stringKey)){
            appendMap.get(stringKey).getValue().append(StringUtils.toString(value));
        }else {
            //若不存在--->目标redis反查已有的数据存入内存
            appendMap.put(stringKey,StringCompensatorEntity.builder()
                    .key(key)
                    .dbNum(dbNum)
                    .value(new StringBuilder(client.get(dbNum,key)))
                    .ms(0L)
                    .stringKey(StringUtils.toString(key))
                    .build());
//            appendMap.put(stringKey,new StringBuilder(StringUtils.toString(value)));
        }

        if(compensatorUtils.isStringSuccess(res)){
            return;
        }

        int i=3;
        while (i-->0){
            if(compensatorUtils.isStringSuccess(client.set(dbNum,key,StringUtils.getBytes(appendMap.get(stringKey).getValue().toString())))){
                break;
            }
        }
        if(i<=0){
//            client.se
            log.warn("[{}]中key[{}]同步失败type[append]---->value{}",taskId, StringUtils.toString(key), StringUtils.toString(value));
        }
    }

    @Override
    public void lpush(Long dbNum, byte[] key, byte[][] value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.lpush(dbNum,key,value))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[lpush]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
        }
    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, byte[][] value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.lpush(dbNum,key,ms,value))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[lpush--time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
        }
    }

    @Override
    public void lpush(Long dbNum, byte[] key, List<byte[]> value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.lpush(dbNum,key,value))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[lpush - time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
        }
    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, List<byte[]> value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.lpush(dbNum,key,ms,value))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[lpush - time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
        }
    }

    @Override
    public void sadd(Long dbNum, byte[] key, byte[][] members, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.sadd(dbNum,key,members))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[sadd]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(members));
        }
    }

    @Override
    public void sadd(Long dbNum, byte[] key, long ms, byte[][] members, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.sadd(dbNum,key,ms,members))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[sadd--time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(members));
        }
    }

    @Override
    public void sadd(Long dbNum, byte[] key, Set<byte[]> members, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.sadd(dbNum,key,members))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[sadd ]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(members));
        }
    }

    @Override
    public void sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.sadd(dbNum,key,ms,members))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[sadd-time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(members));
        }
    }

    @Override
    public void zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.zadd(dbNum,key,value))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[zadd]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
        }
    }

    @Override
    public void zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.zadd(dbNum,key,value,ms))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[zadd-time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
        }
    }

    @Override
    public void hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, String res) {
        if(compensatorUtils.isStringSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isStringSuccess(client.hmset(dbNum,key,hash))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[hmset]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(hash));
        }
    }

    @Override
    public void hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms, String res) {
        if(compensatorUtils.isStringSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isStringSuccess(client.hmset(dbNum,key,hash,ms))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[hmset - time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(hash));
        }
    }

    @Override
    public void restore(Long dbNum, byte[] key, int ttl, byte[] serializedValue, String res) {
        if(compensatorUtils.isStringSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isStringSuccess(client.restore(dbNum,key,ttl,serializedValue))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[restore - restore]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(serializedValue));
        }
    }

    @Override
    public void restoreReplace(Long dbNum, byte[] key, int ttl, byte[] serializedValue, String res) {
        if(compensatorUtils.isStringSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isStringSuccess(client.restoreReplace(dbNum,key,ttl,serializedValue))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[restoreReplace - restore]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(serializedValue));
        }
    }

    @Override
    public void restoreReplace(Long dbNum, byte[] key, int ttl, byte[] serializedValue, boolean highVersion, String res) {
        if(compensatorUtils.isStringSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isStringSuccess(client.restoreReplace(dbNum,key,ttl,serializedValue,highVersion))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[restoreReplace - restore]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(serializedValue));
        }
    }

    @Override
    public void send(byte[] cmd, Object res, byte[]... args) {

    }

    @Override
    public void select(Integer dbNum) {

    }



}

