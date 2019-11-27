package syncerservice.syncerplusservice.task.BatchedKeyValueTask.cluster;

import syncerservice.syncerplusredis.event.Event;
import syncerservice.syncerplusredis.rdb.datatype.ZSetEntry;
import syncerservice.syncerplusredis.rdb.iterable.datatype.*;
import syncerservice.syncerplusredis.constant.RedisCommandTypeEnum;
import syncerservice.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import syncerservice.syncerplusservice.util.Jedis.pool.JDJedisClientPool;

import lombok.extern.slf4j.Slf4j;
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
public class BatchedClusterRestoreTask implements Callable<Object> {
    private Event event;
    private Long ms;
    private String key;
    private StringBuffer info;
    private JedisClusterPlus targetJedis;
    private RedisCommandTypeEnum  typeEnum;
    private JDJedisClientPool targetJedisClientPool;

    public BatchedClusterRestoreTask(Event event, Long ms, String key, StringBuffer info, JedisClusterPlus redisClient, RedisCommandTypeEnum  typeEnum) {
        this.event = event;
        this.ms = ms;
        this.key = key;
        this.info = info;
        this.targetJedis = redisClient;
        this.typeEnum=typeEnum;
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
        } finally {

            if(targetJedis!=null){
//                targetJedisClientPool.returnBrokenResource(targetJedis);
//                targetJedis.close();
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
