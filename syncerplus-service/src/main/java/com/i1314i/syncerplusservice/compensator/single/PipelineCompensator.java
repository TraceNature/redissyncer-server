package com.i1314i.syncerplusservice.compensator.single;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.entity.PipelineDataEntity;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PipelineCompensator {



    public static boolean singleCompensator(List<Object> statusList, List<PipelineDataEntity> keys, JDJedis source, JDJedis target, RedisCommandTypeEnum redisCommandTypeEnum) {
        //提交数据的总量
        int statusListLenght = statusList.size();
        Set<PipelineDataEntity> mistakeKeys = new HashSet<>();
        for (int i = 0; i < statusList.size(); i++) {
            Object status = statusList.get(i);

            if (status instanceof String) {
                if (!status.toString().equals("OK")) {
                    mistakeKeys.add(keys.get(i));

                }
            } else if (status instanceof Integer) {
                if (Integer.valueOf(status.toString()) < 0) {

                    System.out.println("---------------Integer");


                }
            }else if (status instanceof Long) {
                if (Long.valueOf(status.toString()) < 0) {

                    System.out.println("---------------Long");


                }
            }

            //判断是否超过50%
            if (mistakeKeys.size() > (statusListLenght / 2)) {
                //task panic
                System.out.println("------------------补偿启动");
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

        System.out.println(JSON.toJSONString(mistakeKeys));
        return true;
    }

    public static void main(String[] args) {
        List<Object> listResult =  Stream.of("OK", 1, "OK1","php","java").collect(Collectors.toList());

        List<PipelineDataEntity> listResultS =  Stream.of(new PipelineDataEntity("test".getBytes(),0,11),
                new PipelineDataEntity("test1".getBytes(),0,11),
                new PipelineDataEntity("test2".getBytes(),0,11),
                new PipelineDataEntity("test3".getBytes(),0,11),
                new PipelineDataEntity("test4".getBytes(),0,11)).collect(Collectors.toList());

//        singleCompensator(listResult,listResultS);
    }
}
