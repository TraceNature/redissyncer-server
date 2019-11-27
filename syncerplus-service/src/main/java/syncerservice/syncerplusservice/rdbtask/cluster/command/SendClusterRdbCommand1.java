package syncerservice.syncerplusservice.rdbtask.cluster.command;

import syncerservice.syncerplusredis.event.Event;
import syncerservice.syncerplusredis.rdb.datatype.ZSetEntry;
import syncerservice.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncerservice.syncerplusredis.rdb.iterable.datatype.*;
import syncerservice.syncerplusredis.constant.RedisCommandTypeEnum;
import syncerservice.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.params.SetParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
public class SendClusterRdbCommand1 {



    public synchronized void sendCommand(Long ms, RedisCommandTypeEnum typeEnum, Event event, JedisClusterPlus redisClient, String key) {
        Object r = null;
        StringBuilder info = new StringBuilder();
        int i = 3;
        try {
            while (i > 0) {

                if (ms == null || ms == 0L) {
                    if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                        BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                        if (valueString.getBatch() == 0) {
                            r = redisClient.set(valueString.getKey(), valueString.getValue());
                        } else {
                            r = redisClient.append(valueString.getKey(), valueString.getValue());
                        }
                    } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                        BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                        byte[][] array = listBytes(valueList.getValue());
                        r = redisClient.lpush(valueList.getKey(), array);
                    } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {

                        BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                        byte[][] array = setBytes(valueSet.getValue());
                        r = redisClient.sadd(valueSet.getKey(), array);

                    } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                        BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                        Map<byte[], Double> map = zsetBytes(valueZSet.getValue());

                        r = redisClient.zadd(valueZSet.getKey(), map);

                    } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {

                        BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;

                        r = redisClient.hmset(valueHash.getKey(), valueHash.getValue());
                    } else if (typeEnum.equals(RedisCommandTypeEnum.DUMP)) {
//                        System.out.println("----------------");println


                        DumpKeyValuePair valueDump = (DumpKeyValuePair) event;


                        if(redisClient.del(valueDump.getKey())>=0){
                            r = redisClient.restore(valueDump.getKey(), 0, valueDump.getValue());
                        }else {
                        }

//                        if (targetJedis.exists(valueDump.getKey())) {
//                            if (targetJedis.del(valueDump.getKey()) >= 0) {
//                                r = targetJedis.restore(valueDump.getKey(), 0, valueDump.getValue());
//                            } else {
//                                r = targetJedis.restore(valueDump.getKey(), 0, valueDump.getValue());
//                            }
//                        } else {
//                            r = targetJedis.restore(valueDump.getKey(), 0, valueDump.getValue());
//                        }

                    }

                } else {
                    if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                        BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                        if (valueString.getBatch() == 0) {
                            r = redisClient.set(valueString.getKey(), valueString.getValue(), new SetParams().px(ms));
                        } else {
                            r = redisClient.append(valueString.getKey(), valueString.getValue());
                        }
                    } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                        BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                        byte[][] array = listBytes(valueList.getValue());
                        r = redisClient.lpush(valueList.getKey(), array);
                        redisClient.pexpire(valueList.getKey(), ms);
                    } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {
                        BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                        byte[][] array = setBytes(valueSet.getValue());
                        r = redisClient.sadd(valueSet.getKey(), array);
                        redisClient.pexpire(valueSet.getKey(), ms);
                    } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                        BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                        Map<byte[], Double> map = zsetBytes(valueZSet.getValue());
                        r = redisClient.zadd(valueZSet.getKey(), map);
                        redisClient.pexpire(valueZSet.getKey(), ms);

                    } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {

                        BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;
                        r = redisClient.hmset(valueHash.getKey(), valueHash.getValue());
                        redisClient.pexpire(valueHash.getKey(), ms);
                    } else if (typeEnum.equals(RedisCommandTypeEnum.DUMP)) {
                        System.out.println("-------"+"tset");
                        DumpKeyValuePair valueDump = (DumpKeyValuePair) event;

//                        int ttl = (int) (ms / 1000);
                        if (redisClient.exists(valueDump.getKey())) {
                            if (redisClient.del(valueDump.getKey()) >= 0) {
                                r = redisClient.restore(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                            } else {
                                r = redisClient.restore(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                            }
                        } else {
                            r = redisClient.restore(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                        }
                    }


                }
                if (r instanceof String) {
                    if (r.equals("OK")) {
                        i = -1;
                        info.append(key);
                        info.append("-> ");
                        info.append(r.toString());
                        log.info(info.toString());
                        break;
                    } else {
                        i--;
                    }
                } else if (r instanceof Integer) {
                    if ((Integer) r >= 0) {
                        i = -1;
                        info.append(key);
                        info.append("->");
                        info.append(r.toString());
                        log.info(info.toString());
                        break;
                    } else {
                        i--;
                    }
                } else if (r instanceof Long) {
                    if ((Long) r >= 0) {
                        i = -1;
                        info.append(key);
                        info.append(" ->");
                        info.append(r.toString());
                        log.info(info.toString());
                        break;
                    } else {
                        i--;
                    }
                } else {
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
            log.info(epx.getMessage() + ": " + i + ":" + key);
        } finally {

//            if (redisClient != null) {
////                targetJedisClientPool.returnBrokenResource(targetJedis);
//                redisClient.close();
//            }
        }
    }

    public byte[][] listBytes(List<byte[]> datas) {
        byte[][] array = new byte[datas.size()][];
        datas.toArray(array);
        return array;
    }

    public byte[][] setBytes(Set<byte[]> datas) {
        byte[][] array = new byte[datas.size()][];
        datas.toArray(array);
        return array;
    }

    public Map<byte[], Double> zsetBytes(Set<ZSetEntry> datas) {
        Map<byte[], Double> map = new HashMap<>();
        datas.forEach(zset -> {
            map.put(zset.getElement(), zset.getScore());
        });
        return map;
    }

}
