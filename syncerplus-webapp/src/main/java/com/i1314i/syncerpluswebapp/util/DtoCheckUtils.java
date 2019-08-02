package com.i1314i.syncerpluswebapp.util;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerplusservice.entity.RedisPoolProps;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.entity.dto.common.SyncDataDto;

import java.util.*;

public class DtoCheckUtils {
    public synchronized static Object ckeckRedisClusterDto(SyncDataDto syncDataDto, RedisPoolProps redisPoolProps){
        if(syncDataDto instanceof RedisSyncDataDto){
            if(syncDataDto.getIdleTimeRunsMillis()==0){
                syncDataDto.setIdleTimeRunsMillis(redisPoolProps.getIdleTimeRunsMillis());
            }
            if(syncDataDto.getMaxWaitTime()==0){
                syncDataDto.setMaxWaitTime(redisPoolProps.getMaxWaitTime());
            }
            if(syncDataDto.getMaxPoolSize()==0){
                syncDataDto.setMaxPoolSize(redisPoolProps.getMaxPoolSize());
            }
            if(syncDataDto.getMinPoolSize()==0){
                syncDataDto.setMinPoolSize(redisPoolProps.getMinPoolSize());
            }

            syncDataDto.setTimeBetweenEvictionRunsMillis(redisPoolProps.getTimeBetweenEvictionRunsMillis());
        }

        if(syncDataDto instanceof RedisClusterDto){

            if(syncDataDto.getMaxWaitTime()==0){
                syncDataDto.setMaxWaitTime(redisPoolProps.getMaxWaitTime());
            }
            if(syncDataDto.getIdleTimeRunsMillis()==0){
                syncDataDto.setIdleTimeRunsMillis(redisPoolProps.getIdleTimeRunsMillis());
            }
            if(syncDataDto.getMaxPoolSize()==0){
                syncDataDto.setMaxPoolSize(redisPoolProps.getMaxPoolSize());
            }
            if(syncDataDto.getMinPoolSize()==0){
                syncDataDto.setMinPoolSize(redisPoolProps.getMinPoolSize());
            }

            if(syncDataDto.getDbNum()==null){
                syncDataDto.setDbNum(new HashMap<>());
            }
            updateUri((RedisClusterDto) syncDataDto);
            syncDataDto.setTimeBetweenEvictionRunsMillis(redisPoolProps.getTimeBetweenEvictionRunsMillis());
        }
        return syncDataDto;
    }



    public static void updateUri(RedisClusterDto redisClusterDto){
        redisClusterDto.setSourceUris(getUrlList(redisClusterDto.getSourceRedisAddress(),redisClusterDto.getSourcePassword()));
        redisClusterDto.setTargetUris(getUrlList(redisClusterDto.getTargetRedisAddress(),redisClusterDto.getTargetPassword()));
    }



    public synchronized static Set<String> getUrlList(String sourceUrls, String password) {
        Set<String> urlList = new HashSet<>();
        String[] sourceUrlsList = sourceUrls.split(";");
        //循环遍历所有的url
        for (String url : sourceUrlsList) {
            StringBuilder stringHead = new StringBuilder("redis://");
            //如果截取出空字符串直接跳过
            if (url != null && url.length() > 0) {
                stringHead.append(url);
                //判断密码是否为空如果为空直接跳过
                if (password != null && password.length() > 0) {
                    stringHead.append("?authPassword=");
                    stringHead.append(password);
                }
                urlList.add(stringHead.toString());

            }
        }
        return urlList;
    }


    public static void main(String[] args) {


        Map HH=new HashMap();
        HH.put(1,1);
        ResultMap map=ResultMap.builder().data(HH);

        RedisClusterDto dto=new RedisClusterDto("sourceAddress","targetAddress","sourcePassword",
                "targetPassword","test",100
                ,110,10000
                ,1000,100000,1,"off");
        dto.setDbNum(HH);
        System.out.println(JSON.toJSONString(dto));
    }
}
