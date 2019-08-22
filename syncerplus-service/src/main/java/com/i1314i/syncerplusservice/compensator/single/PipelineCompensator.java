package com.i1314i.syncerplusservice.compensator.single;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.entity.PipelineDataEntity;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import com.i1314i.syncerplusservice.util.Jedis.ObjectUtils;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import redis.clients.jedis.params.SetParams;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * pipeline补偿机制
 */
public class PipelineCompensator {

    public static boolean singleCompensator(List<Object> statusList, List<PipelineDataEntity> keys, JDJedis source, JDJedis target,String thredName) {
        //提交数据的总量
        int statusListLenght = statusList.size();
        Set<PipelineDataEntity> mistakeKeys = new HashSet<>();
        for (int i = 0; i < statusList.size(); i++) {
            Object status = statusList.get(i);
            PipelineDataEntity dataEntity = keys.get(i);
            if (status instanceof String) {
                if (!status.toString().equals("OK")) {
                    mistakeKeys.add(keys.get(i));


                }
            } else if (status instanceof Integer) {
                if (Integer.valueOf(status.toString()) < 0) {

                    mistakeKeys.add(keys.get(i));
                }
            } else if (status instanceof Long) {
                if (Long.valueOf(status.toString()) < 0) {
                    mistakeKeys.add(keys.get(i));

                }
            }

            //判断是否超过50%
            if (mistakeKeys.size() <= (statusListLenght / 2)) {
                //task panic
                StringConpensator(dataEntity.getRedisCommandTypeEnum(), target, source, dataEntity);
                SetConpensator(dataEntity.getRedisCommandTypeEnum(), target, source, dataEntity);
                ZSetConpensator(dataEntity.getRedisCommandTypeEnum(), target, source, dataEntity);
                StringConpensator(dataEntity.getRedisCommandTypeEnum(), target, source, dataEntity);
                ListConpensator(dataEntity.getRedisCommandTypeEnum(), target, source, dataEntity);
                HashConpensator(dataEntity.getRedisCommandTypeEnum(), target, source, dataEntity);
            }else{
                //结束当前线程停止同步
                TaskMonitorUtils.removeAliveThread(thredName);

                System.out.println("结束");
            }
        }
        // 逐条提交 有错误 task panic
//        for (PipelineDataEntity pipelineData :
//                mistakeKeys) {
//
//            //提交返回结果
//            //如有错误直接停止
//            if (true) {
//                //task panic
//                return false;
//            }
//        }


        return true;
    }


    public static String StringConpensator(RedisCommandTypeEnum redisCommandTypeEnum, JDJedis target, JDJedis source, PipelineDataEntity dataEntity) {
        String msg=null;
        if (redisCommandTypeEnum.equals(RedisCommandTypeEnum.STRING)) {
            if (dataEntity.getTtl() != 0) {
                msg=target.set(dataEntity.getKey(), source.get(dataEntity.getKey()), new SetParams().ex(dataEntity.getTtl()));
            } else {
                msg=target.set(dataEntity.getKey(), source.get(dataEntity.getKey()));
            }
        }else
            return null;

        return msg;
    }

    public static Long SetConpensator(RedisCommandTypeEnum redisCommandTypeEnum, JDJedis target, JDJedis source, PipelineDataEntity dataEntity) {
        long msg = -1;
        if (redisCommandTypeEnum.equals(RedisCommandTypeEnum.SET)) {
            if (dataEntity.getTtl() != 0) {
                msg=target.sadd(dataEntity.getKey(), ObjectUtils.setBytes(source.smembers(dataEntity.getKey())));
                target.expire(dataEntity.getKey(), dataEntity.getTtl());
            } else {
                msg=target.sadd(dataEntity.getKey(), ObjectUtils.setBytes(source.smembers(dataEntity.getKey())));
            }
        }else
            return null;

        return msg;
    }


    public static Long ZSetConpensator(RedisCommandTypeEnum redisCommandTypeEnum, JDJedis target, JDJedis source, PipelineDataEntity dataEntity) {
        long msg = -1;
        if (redisCommandTypeEnum.equals(RedisCommandTypeEnum.ZSET)) {
            if (dataEntity.getTtl() != 0) {
                    msg=target.zadd(dataEntity.getKey(), ObjectUtils.zsetByteP(source.zrange(dataEntity.getKey(), 0, source.zcard(dataEntity.getKey())),source,dataEntity.getKey()));

                    target.expire(dataEntity.getKey(), dataEntity.getTtl());
                } else {

                    msg=target.zadd(dataEntity.getKey(), ObjectUtils.zsetByteP(source.zrange(dataEntity.getKey(), 0, source.zcard(dataEntity.getKey())),source,dataEntity.getKey()));
                }
        }else
            return null;

        return msg;
    }

    public static String HashConpensator(RedisCommandTypeEnum redisCommandTypeEnum, JDJedis target, JDJedis source, PipelineDataEntity dataEntity) {
        String msg=null;
        if (redisCommandTypeEnum.equals(RedisCommandTypeEnum.HASH)) {
            if (dataEntity.getTtl() != 0) {
                msg=target.hmset(dataEntity.getKey(), source.hgetAll(dataEntity.getKey()));
                target.expire(dataEntity.getKey(), dataEntity.getTtl());
            } else {
                msg=target.hmset(dataEntity.getKey(), source.hgetAll(dataEntity.getKey()));
            }
        }else
            return null;
        System.out.println(msg +": "+new String(dataEntity.getKey())+"hash ");
        return msg;
    }

    public static Long ListConpensator(RedisCommandTypeEnum redisCommandTypeEnum, JDJedis target, JDJedis source, PipelineDataEntity dataEntity) {
        long msg = -1;
        if (redisCommandTypeEnum.equals(RedisCommandTypeEnum.LIST)) {
            if (dataEntity.getTtl() != 0) {
                msg=target.lpush(dataEntity.getKey(), ObjectUtils.listBytes(source.lrange(dataEntity.getKey(), 0, source.llen(dataEntity.getKey()))));
                target.expire(dataEntity.getKey(), dataEntity.getTtl());
            } else {
                msg=target.lpush(dataEntity.getKey(), ObjectUtils.listBytes(source.lrange(dataEntity.getKey(), 0, source.llen(dataEntity.getKey()))));
            }
        }else
            return null;

        return msg;

    }


    public static void main(String[] args) {
        List<Object> listResult = Stream.of("OK", 1, -1, "php", "java").collect(Collectors.toList());
        JDJedis source=new JDJedis("127.0.0.1",6379);
        System.out.println(source.ping());
        JDJedis target=new JDJedis("114.67.100.239",6379);
        target.auth("redistest0102");
        List<PipelineDataEntity> listResultS = Stream.of(new PipelineDataEntity("test".getBytes(), 0, 0,RedisCommandTypeEnum.STRING),
                new PipelineDataEntity("test1".getBytes(), 0, 0,RedisCommandTypeEnum.STRING),
                new PipelineDataEntity("runoobkey".getBytes(), 0, 0,RedisCommandTypeEnum.LIST),
                new PipelineDataEntity("hash".getBytes(), 0, 0,RedisCommandTypeEnum.HASH),
                new PipelineDataEntity("hash".getBytes(), 0, 0,RedisCommandTypeEnum.HASH)).collect(Collectors.toList());
        singleCompensator(listResult,listResultS,source,target,"test");
    }
}
