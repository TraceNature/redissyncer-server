package com.i1314i.syncerplusservice.rdbtask.single.command;

import com.i1314i.syncerplusredis.event.Event;
import com.i1314i.syncerplusredis.rdb.datatype.ZSetEntry;
import com.i1314i.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import com.i1314i.syncerplusredis.rdb.iterable.datatype.*;
import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;

import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import com.i1314i.syncerplusservice.util.TaskMsgUtils;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.params.SetParams;

import java.util.*;


@Slf4j
public class SendRdbCommand implements Runnable {
private Long ms;
private RedisCommandTypeEnum typeEnum;
private Event event;
private JDJedis targetJedis;
private  String key;
private double redisVersion;
private String taskId;

    public SendRdbCommand(String taskId,Long ms, RedisCommandTypeEnum typeEnum, Event event, JDJedis targetJedis, String key, double redisVersion) {
        this.ms = ms;
        this.typeEnum = typeEnum;
        this.event = event;
        this.targetJedis = targetJedis;
        this.key = key;
        this.redisVersion = redisVersion;
        this.taskId=taskId;
    }

    @Override
    public void run(){
        Object r = null;
        StringBuilder info=new StringBuilder();
        int i = 3;
        try {
            while (i > 0) {

                if (ms == null||ms == 0L) {
                    if(typeEnum.equals(RedisCommandTypeEnum.STRING)){
                        BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                        if(valueString.getBatch()==0){
                            r = targetJedis.set(valueString.getKey(), valueString.getValue());
                        }else {
                            r = targetJedis.append(valueString.getKey(), valueString.getValue());
                        }
                    }else if(typeEnum.equals(RedisCommandTypeEnum.LIST)){
                        BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                        byte[][] array = listBytes(valueList.getValue());
                        r =targetJedis.lpush(valueList.getKey(), array);
                    }else if(typeEnum.equals(RedisCommandTypeEnum.SET)){
                        BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                        byte[][] array = setBytes( valueSet.getValue());
                        r =targetJedis.sadd(valueSet.getKey(), array);

                    }else if(typeEnum.equals(RedisCommandTypeEnum.ZSET)){
                        BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                        Map<byte[], Double> map = zsetBytes(valueZSet.getValue());

                        r =targetJedis.zadd(valueZSet.getKey(), map);

                    }else if(typeEnum.equals(RedisCommandTypeEnum.HASH)){

                        BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;

                        r =targetJedis.hmset(valueHash.getKey(), valueHash.getValue());

                    }else if(typeEnum.equals(RedisCommandTypeEnum.DUMP)){
                        DumpKeyValuePair valueDump= (DumpKeyValuePair) event;
                        if (redisVersion < 3.0) {
                            if (targetJedis.exists(valueDump.getKey())) {
                                if (targetJedis.del(valueDump.getKey()) >= 0) {
                                    r = targetJedis.restore(valueDump.getKey(), 0, valueDump.getValue());
                                }else {
                                    r = targetJedis.restore(valueDump.getKey(), 0, valueDump.getValue());
                                }
                            }else{
                                r = targetJedis.restore(valueDump.getKey(), 0, valueDump.getValue());
                            }
                        } else {
                            r = targetJedis.restoreReplace(valueDump.getKey(), 0, valueDump.getValue());
                        }
                    }

                } else {
                    if(typeEnum.equals(RedisCommandTypeEnum.STRING)){
                        BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                        if(valueString.getBatch()==0){
                            r = targetJedis.set(valueString.getKey(), valueString.getValue(), new SetParams().px(ms));
                        }else {
                            r = targetJedis.append(valueString.getKey(), valueString.getValue());
                        }
                    }else if(typeEnum.equals(RedisCommandTypeEnum.LIST)){
                        BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                        byte[][] array = listBytes(valueList.getValue());
                        r =targetJedis.lpush(valueList.getKey(), array);
                        targetJedis.pexpire(valueList.getKey(), ms);
                    }else if(typeEnum.equals(RedisCommandTypeEnum.SET)){
                        BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                        byte[][] array = setBytes( valueSet.getValue());
                        r =targetJedis.sadd(valueSet.getKey(), array);
                        targetJedis.pexpire(valueSet.getKey(), ms);
                    }else if(typeEnum.equals(RedisCommandTypeEnum.ZSET)){
                        BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                        Map<byte[], Double> map = zsetBytes(valueZSet.getValue());
                        r =targetJedis.zadd(valueZSet.getKey(), map);
                        targetJedis.pexpire(valueZSet.getKey(), ms);

                    }else if(typeEnum.equals(RedisCommandTypeEnum.HASH)){

                        BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;
                        r =targetJedis.hmset(valueHash.getKey(), valueHash.getValue());
                        targetJedis.pexpire(valueHash.getKey(), ms);
                    }else if(typeEnum.equals(RedisCommandTypeEnum.DUMP)){
                        DumpKeyValuePair valueDump= (DumpKeyValuePair) event;

//                        int ttl = (int) (ms / 1000);
                        if (redisVersion < 3.0) {

                            if (targetJedis.exists(valueDump.getKey())) {
                                if (targetJedis.del(valueDump.getKey()) >= 0) {
                                    r = targetJedis.restore(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                                }else {
                                    r = targetJedis.restore(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                                }
                            }else {
                                r = targetJedis.restore(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                            }
                        } else {
                            r = targetJedis.restoreReplace(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                        }
                    }



                }
                if(r instanceof String){
                    if (r.equals("OK")) {
                        i = -1;
                        info.append(key);
                        info.append("->");
                        info.append(r.toString());
                        log.info(info.toString());
                        break;
                    } else {
                        i--;
                    }
                }else if(r instanceof Integer){
                    if ((Integer)r>=0) {
                        i = -1;
                        info.append(key);
                        info.append("->");
                        info.append(r.toString());
                        log.info(info.toString());
                        break;
                    } else {
                        i--;
                    }
                }else if(r instanceof Long){
                    if ((Long)r>=0) {
                        i = -1;
                        info.append(key);
                        info.append(" ->");
                        info.append(r.toString());
                        log.info(info.toString());
                        break;
                    } else {
                        i--;
                    }
                }else {
                    if (r.equals("OK")) {
                        i = -1;
                        info.append(key);
                        info.append(" ->");
                        info.append(r.toString());
                        log.info(info.toString());
                        break;
                    } else {
                        i--;
                    }
                }


            }
        } catch (Exception epx) {

            if(epx.getMessage().toUpperCase().equals("ERR DUMP payload version or checksum are wrong".toUpperCase())){
                try {
                    log.warn("redis版本号错误，宕掉任务 "+epx.getMessage() + ": " + i + ":" + key );
                    Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
                } catch (TaskMsgException e) {
                    e.printStackTrace();
                }
            }

        } finally {

            if(targetJedis!=null){
//                targetJedisClientPool.returnBrokenResource(targetJedis);
                targetJedis.close();
            }
        }
    }

    public byte[][]  listBytes(List<byte[]> datas){
        byte[][] array = new byte[datas.size()][];
        datas.toArray(array);
        return array;
    }

    public byte[][]  setBytes(Set<byte[]> datas){
        byte[][] array = new byte[datas.size()][];
        datas.toArray(array);
        return array;
    }

    public Map<byte[], Double> zsetBytes(Set<ZSetEntry> datas){
        Map<byte[], Double> map = new HashMap<>();
        datas.forEach(zset -> {
            map.put(zset.getElement(), zset.getScore());
        });
        return map;
    }


    public void sendPipelineComand(){

    }
}
