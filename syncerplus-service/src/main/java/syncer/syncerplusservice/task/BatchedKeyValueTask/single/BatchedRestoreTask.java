package syncer.syncerplusservice.task.BatchedKeyValueTask.single;

import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.*;
import syncer.syncerplusredis.constant.RedisCommandTypeEnum;
import syncer.syncerplusservice.util.Jedis.pool.JDJedisClientPool;

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
    private RedisCommandTypeEnum typeEnum;
    private JDJedisClientPool targetJedisClientPool;
    private double redisVersion;

    public BatchedRestoreTask(Event event, Long ms, String key, StringBuffer info, Jedis targetJedis, RedisCommandTypeEnum typeEnum, double redisVersion) {
        this.event = event;
        this.ms = ms;
        this.key = key;
        this.info = info;
        this.targetJedis = targetJedis;
        this.typeEnum = typeEnum;
        this.targetJedisClientPool = targetJedisClientPool;
        this.redisVersion = redisVersion;
    }

    public BatchedRestoreTask(Event event, Long ms, String key, StringBuffer info, Jedis targetJedis, RedisCommandTypeEnum typeEnum) {
        this.event = event;
        this.ms = ms;
        this.key = key;
        this.info = info;
        this.targetJedis = targetJedis;
        this.typeEnum = typeEnum;
        this.targetJedisClientPool = targetJedisClientPool;
        this.redisVersion = 0L;
    }

    @Override
    public Object call() throws Exception {

        Object r = null;
        int i = 3;
        try {
            while (i > 0) {

                if (ms == null || ms == 0L) {
                    if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                        BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                        if (valueString.getBatch() == 0) {
                            r = targetJedis.set(valueString.getKey(), valueString.getValue());
                        } else {
                            r = targetJedis.append(valueString.getKey(), valueString.getValue());
                        }
                    } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                        BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                        byte[][] array = listBytes(valueList.getValue());
                        r = targetJedis.lpush(valueList.getKey(), array);
                    } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {

                        BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                        byte[][] array = setBytes(valueSet.getValue());
                        r = targetJedis.sadd(valueSet.getKey(), array);

                    } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                        BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                        Map<byte[], Double> map = zsetBytes(valueZSet.getValue());

                        r = targetJedis.zadd(valueZSet.getKey(), map);

                    } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {
                        BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;
                        r = targetJedis.hmset(valueHash.getKey(), valueHash.getValue());
                    } else if (typeEnum.equals(RedisCommandTypeEnum.DUMP)) {
                        DumpKeyValuePair valueDump = (DumpKeyValuePair) event;
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
                    if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                        BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                        if (valueString.getBatch() == 0) {
                            r = targetJedis.set(valueString.getKey(), valueString.getValue(), new SetParams().px(ms));
                        } else {
                            r = targetJedis.append(valueString.getKey(), valueString.getValue());
                        }
                    } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                        BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                        byte[][] array = listBytes(valueList.getValue());
                        r = targetJedis.lpush(valueList.getKey(), array);
                        targetJedis.pexpire(valueList.getKey(), ms);
                    } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {
                        BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                        byte[][] array = setBytes(valueSet.getValue());
                        r = targetJedis.sadd(valueSet.getKey(), array);
                        targetJedis.pexpire(valueSet.getKey(), ms);
                    } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                        BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                        Map<byte[], Double> map = zsetBytes(valueZSet.getValue());
                        r = targetJedis.zadd(valueZSet.getKey(), map);
                        targetJedis.pexpire(valueZSet.getKey(), ms);

                    } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {
                        BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;
                        r = targetJedis.hmset(valueHash.getKey(), valueHash.getValue());
                        targetJedis.pexpire(valueHash.getKey(), ms);
                    } else if (typeEnum.equals(RedisCommandTypeEnum.DUMP)) {
                        DumpKeyValuePair valueDump = (DumpKeyValuePair) event;
                        int ttl = (int) (ms / 1000);

                        if (redisVersion < 3.0) {
                            if (targetJedis.exists(valueDump.getKey())) {
                                if (targetJedis.del(valueDump.getKey()) >= 0) {
                                    r = targetJedis.restore(valueDump.getKey(), ttl, valueDump.getValue());
                                }else {
                                    r = targetJedis.restore(valueDump.getKey(), ttl, valueDump.getValue());
                                }
                            }else {
                                r = targetJedis.restore(valueDump.getKey(), ttl, valueDump.getValue());
                            }
                        } else {
                            r = targetJedis.restoreReplace(valueDump.getKey(), ttl, valueDump.getValue());
                        }
                    }


                }
                if (r instanceof String) {
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

            if (targetJedis != null) {
                targetJedis.close();
            }
        }
        return r;
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
