package com.i1314i.syncerplusservice.task.BatchedKeyValueTask.single;

import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.rdb.datatype.*;
import com.moilioncircle.redis.replicator.rdb.iterable.datatype.*;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;


/**
 * 不相同版本之间的数据迁移
 */
@Slf4j
public class BatchedRestoreTask implements Callable<Object> {
    private Event event;
    private Long ms;
    private String key;
    private StringBuffer info;
    private Jedis targetJedis;
    private RedisCommandTypeEnum  typeEnum;
    private JDJedisClientPool targetJedisClientPool;

    public BatchedRestoreTask(Event event, Long ms, String key, StringBuffer info, Jedis targetJedis, RedisCommandTypeEnum  typeEnum) {
        this.event = event;
        this.ms = ms;
        this.key = key;
        this.info = info;
        this.targetJedis = targetJedis;
        this.typeEnum=typeEnum;
        this.targetJedisClientPool=targetJedisClientPool;
    }


    @Override
    public Object call() throws Exception {

        Object r = null;
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
            log.info(epx.getMessage() + ": " + i + ":" + key );
        } finally {

            if(targetJedis!=null){
//                targetJedisClientPool.returnBrokenResource(targetJedis);
                targetJedis.close();
            }
        }
        return r;
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

    public Map<byte[], Double>  zsetBytes(Set<ZSetEntry> datas){
        Map<byte[], Double> map = new HashMap<>();
        datas.forEach(zset -> {
            map.put(zset.getElement(), zset.getScore());
        });
        return map;
    }

}
