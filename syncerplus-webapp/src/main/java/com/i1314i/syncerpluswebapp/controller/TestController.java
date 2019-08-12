package com.i1314i.syncerpluswebapp.controller;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.entity.PoolConfig;
import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerpluscommon.util.common.TemplateUtils;
import com.i1314i.syncerplusservice.entity.RedisPoolProps;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.entity.dto.common.SyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import com.i1314i.syncerpluswebapp.util.DtoCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController

public class TestController {

    @Autowired
    RedisPoolProps redisPoolProps;
    @Autowired
    private PoolConfig poolConfig;

    @Autowired
    private Environment env;
    @Autowired
    IRedisReplicatorService redisReplicatorService;


    @RequestMapping(value = "/test")
    public String success(@Validated RedisSyncDataDto syncDataDto) throws TaskMsgException {
        redisReplicatorService.sync(syncDataDto);
        return "success";
    }



    @RequestMapping(value = "/test1")
    public String success1(String name){
        TaskMonitorUtils.removeAliveThread(name);
        return "success";
    }

    @RequestMapping(value = "/test2")
    public String success2(){
        return JSON.toJSONString(TaskMonitorUtils.getAliveThreadHashMap());
    }


    @RequestMapping(value = "/test3")
    public String success3(){
        return JSON.toJSONString(TaskMonitorUtils.getThreadStateHashMap());
    }

    @RequestMapping(value = "/test4")
    public String success4(){
        return JSON.toJSONString(poolConfig);
    }


    @RequestMapping(value = "/s")
    public   RedisClusterDto  s(@RequestBody RedisClusterDto dto){
        dto= (RedisClusterDto) DtoCheckUtils.ckeckRedisClusterDto(dto,redisPoolProps);
        if(dto.getTargetUris().size()==1){
            //单机 间或者往京东云集群迁移

        }else {
            //cluster

        }

        return dto;
    }

    public static void main(String[] args) {
        System.out.println(JSON.toJSONString(ResultMap.builder().code("200").msg("msg").data("wadwa")));
    }
}
