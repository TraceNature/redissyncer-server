package syncer.transmission.compensator.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.rdb.datatype.ZSetEntry;
import syncer.replica.util.objectutil.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.constants.CmdEnum;
import syncer.transmission.entity.StringCompensatorEntity;
import syncer.transmission.util.CompensatorUtils;
import syncer.transmission.util.cache.LruCache;
import syncer.transmission.util.strings.StringUtils;


import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 多线程补偿
 */

@Slf4j
public class MultiThreadSyncerCompensator implements ISyncerCompensator {

   private  String taskId;
   private Map<String,Double>incrMap=new LruCache<>(1000);
   private Map<String, StringCompensatorEntity>appendMap=new LruCache<>(1000);
   private CompensatorUtils compensatorUtils=new CompensatorUtils();
   private RedisClient client;
   private Long dbNum=0L;
    public MultiThreadSyncerCompensator(String taskId, RedisClient client) {
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
    public void append(Long dbNum, byte[] key, byte[] value, Long res) {
        String stringKey=StringUtils.toString(key);
        if(appendMap.containsKey(stringKey)){
            appendMap.get(stringKey).getValue().append(StringUtils.toString(value));
        }else {
            //若不存在--->目标redis反查已有的数据存入内存
            appendMap.put(stringKey, StringCompensatorEntity.builder()
                    .key(key)
                    .dbNum(dbNum)
                    .value(new StringBuilder(client.get(dbNum,key)))
                    .ms(0L)
                    .stringKey(StringUtils.toString(key))
                    .build());
//            appendMap.put(stringKey,new StringBuilder(StringUtils.toString(value)));
        }

        if(compensatorUtils.isLongSuccess(res)){
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
//        int i=3;
//        while (i-->0){
//            if(compensatorUtils.isLongSuccess(client.lpush(dbNum,key,value))){
//                break;
//            }
//        }
//        if(i<=0){
//            log.warn("[{}]中key[{}]同步失败type[lpush]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
//        }
    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, byte[][] value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
//        int i=3;
//        while (i-->0){
//            if(compensatorUtils.isLongSuccess(client.lpush(dbNum,key,ms,value))){
//                break;
//            }
//        }
//        if(i<=0){
//            log.warn("[{}]中key[{}]同步失败type[lpush--time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
//        }
    }

    @Override
    public void lpush(Long dbNum, byte[] key, List<byte[]> value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
//        int i=3;
//        while (i-->0){
//            if(compensatorUtils.isLongSuccess(client.lpush(dbNum,key,value))){
//                break;
//            }
//        }
//        if(i<=0){
//            log.warn("[{}]中key[{}]同步失败type[lpush - time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
//        }
    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, List<byte[]> value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
//        int i=3;
//        while (i-->0){
//            if(compensatorUtils.isLongSuccess(client.lpush(dbNum,key,ms,value))){
//                break;
//            }
//        }
//        if(i<=0){
//            log.warn("[{}]中key[{}]同步失败type[lpush - time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
//        }
    }

    @Override
    public void rpush(Long dbNum, byte[] key, byte[][] value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
//        int i=3;
//        while (i-->0){
//            if(compensatorUtils.isLongSuccess(client.rpush(dbNum,key,value))){
//                break;
//            }
//        }
//        if(i<=0){
//            log.warn("[{}]中key[{}]同步失败type[rpush]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
//        }
    }

    @Override
    public void rpush(Long dbNum, byte[] key, long ms, byte[][] value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
//        int i=3;
//        while (i-->0){
//            if(compensatorUtils.isLongSuccess(client.rpush(dbNum,key,ms,value))){
//                break;
//            }
//        }
//        if(i<=0){
//            log.warn("[{}]中key[{}]同步失败type[rpush--time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
//        }
    }

    @Override
    public void rpush(Long dbNum, byte[] key, List<byte[]> value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
//        int i=3;
//        while (i-->0){
//            if(compensatorUtils.isLongSuccess(client.rpush(dbNum,key,value))){
//                break;
//            }
//        }
//        if(i<=0){
//            log.warn("[{}]中key[{}]同步失败type[rpush - time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
//        }
    }

    @Override
    public void rpush(Long dbNum, byte[] key, long ms, List<byte[]> value, Long res) {
        if(compensatorUtils.isLongSuccess(res)){
            return;
        }
//        int i=3;
//        while (i-->0){
//            if(compensatorUtils.isLongSuccess(client.rpush(dbNum,key,ms,value))){
//                break;
//            }
//        }
//        if(i<=0){
//            log.warn("[{}]中key[{}]同步失败type[rpush - time]---->value{}",taskId, StringUtils.toString(key), JSON.toJSONString(value));
//        }
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
    public void restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue, String res) {
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
    public void restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, String res) {
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
    public void restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion, String res) {
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
        if(isIdempotentCommand(cmd)){

            retryIdempotentCommand(cmd,res,args);
            return;
        }
        if(compensatorUtils.isObjectSuccess(res)){
            return;
        }
        int i=3;
        while (i-->0){
            if(compensatorUtils.isObjectSuccess(client.send(cmd,args))){
                break;
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[restoreReplace - restore]---->value{}",taskId, StringUtils.toString(cmd), JSON.toJSONString(args));
        }

    }

    @Override
    public void select(Integer dbNum) {
        this.dbNum= Long.valueOf(dbNum);
    }


    boolean isIdempotentCommand(byte[]cmd){
        String stringCmd= Strings.byteToString(cmd);
        CmdEnum cmdEnum=null;
        try{
            cmdEnum= CmdEnum.valueOf(stringCmd.toUpperCase());
            if(null==cmd){
                cmdEnum=CmdEnum.SEND;
            }
        }catch (Exception e){
            cmdEnum=CmdEnum.SEND;
        }
        if(cmdEnum.equals(CmdEnum.INCR)){
            return true;
        }else if(cmdEnum.equals(CmdEnum.INCRBY)){
            return true;
        }else if (cmdEnum.equals(CmdEnum.INCRBYFLOAT)){
            return true;
        }else if(cmdEnum.equals(CmdEnum.DECR)){
            return true;
        }else if(cmdEnum.equals(CmdEnum.DECRBY)){
            return true;
        }else if(cmdEnum.equals(CmdEnum.APPEND)){
            return true;
        }
        return false;
    }


    void retryIdempotentCommand(byte[] cmd, Object res, byte[]... args){
        String stringCmd= Strings.byteToString(cmd);
        CmdEnum cmdEnum=null;
        try{
            cmdEnum= CmdEnum.valueOf(stringCmd.toUpperCase());
            if(null==cmd){
                cmdEnum=CmdEnum.SEND;
            }
        }catch (Exception e){
            cmdEnum=CmdEnum.SEND;
        }

        String key= Strings.byteToString(args[0]);
        if(cmdEnum.equals(CmdEnum.APPEND)){
            if(!appendMap.containsKey(key)){
                appendMap.put(key,StringCompensatorEntity.builder().key(cmd).stringKey(key).value(new StringBuilder()).build());
            }
        }else {
            if(!incrMap.containsKey(key)){
                incrMap.put(key, Double.valueOf(client.get(dbNum,cmd)));
            }
        }
        if(cmdEnum.equals(CmdEnum.INCR)){
            incrMap.put(key,incrMap.get(key)+1);
        }else if(cmdEnum.equals(CmdEnum.INCRBY)){
            incrMap.put(key,incrMap.get(key)+Integer.valueOf(String.valueOf(args[1])));
        }else if(cmdEnum.equals(CmdEnum.INCRBYFLOAT)){
            incrMap.put(key,incrMap.get(key)+Integer.valueOf(String.valueOf(args[1])));
        }else if(cmdEnum.equals(CmdEnum.DECR)){
            incrMap.put(key,incrMap.get(key)-1);
        }else if(cmdEnum.equals(CmdEnum.DECRBY)){
            incrMap.put(key,incrMap.get(key)+Integer.valueOf(String.valueOf(args[1])));
        }else if(cmdEnum.equals(CmdEnum.APPEND)){
            appendMap.get(key).getValue().append(Strings.byteToString(args[1]));
        }
        if(compensatorUtils.isObjectSuccess(res)){
            return;
        }

        int i=3;
        while (i-->0){
            if(cmdEnum.equals(CmdEnum.INCRBYFLOAT)){
                String data= String.valueOf(incrMap.get(key));
                if(compensatorUtils.isStringSuccess(client.set(dbNum,cmd, data.getBytes()))){
                    break;
                }
            }else if(cmdEnum.equals(CmdEnum.APPEND)){
                String data= appendMap.get(key).getValue().toString();
                if(compensatorUtils.isStringSuccess(client.set(dbNum,cmd, data.getBytes()))){
                    break;
                }
            }else {
                String data= String.valueOf(incrMap.get(key).intValue());
                if(compensatorUtils.isStringSuccess(client.set(dbNum,cmd, data.getBytes()))){
                    break;
                }
            }
        }
        if(i<=0){
            log.warn("[{}]中key[{}]同步失败type[restoreReplace - restore]---->value{}",taskId, StringUtils.toString(cmd), JSON.toJSONString(args));
        }
    }
}

