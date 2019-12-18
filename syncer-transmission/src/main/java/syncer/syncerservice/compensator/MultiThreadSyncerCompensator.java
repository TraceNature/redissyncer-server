package syncer.syncerservice.compensator;

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
        if(i==0){
            log.warn("[{}]中key[{}]同步失败---->value{}",taskId, StringUtils.toString(key), StringUtils.toString(value));
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
        if(i==0){
            log.warn("[{}]中key[{}]同步失败---->value{}",taskId, StringUtils.toString(key), StringUtils.toString(value));
        }
    }

    @Override
    public void append(Long dbNum, byte[] key, byte[] value, String res) {
        String stringKey=StringUtils.toString(key);
        if(appendMap.containsKey(stringKey)){
//            appendMap.put(stringKey,appendMap.get(key).append(StringUtils.toString(value)));
        }else {

//            appendMap.put(stringKey,new StringBuilder(StringUtils.toString(value)));
        }

        if(compensatorUtils.isStringSuccess(res)){
            return;
        }

        int i=3;
        while (i-->0){
            if(compensatorUtils.isLongSuccess(client.append(dbNum,key,value))){
                break;
            }
        }
        if(i<=0){
//            client.se
            log.warn("[{}]中key[{}]同步失败---->value{}",taskId, StringUtils.toString(key), StringUtils.toString(value));
        }
    }

    @Override
    public void lpush(Long dbNum, byte[] key, byte[][] value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, byte[][] value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, List<byte[]> value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, List<byte[]> value, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, byte[][] members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, long ms, byte[][] members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, Set<byte[]> members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members, Long res) {

    }

    @Override
    public void zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, Long res) {

    }

    @Override
    public void zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms, Long res) {

    }

    @Override
    public void hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, String res) {

    }

    @Override
    public void hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms, String res) {

    }

    @Override
    public void restore(Long dbNum, byte[] key, int ttl, byte[] serializedValue, String res) {

    }

    @Override
    public void restoreReplace(Long dbNum, byte[] key, int ttl, byte[] serializedValue, String res) {

    }

    @Override
    public void restoreReplace(Long dbNum, byte[] key, int ttl, byte[] serializedValue, boolean highVersion, String res) {

    }

    @Override
    public void send(byte[] cmd, Object res, byte[]... args) {

    }

    @Override
    public void select(Integer dbNum) {

    }



}

