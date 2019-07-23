package com.i1314i.syncerpluswebapp.controller;

import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerplusservice.entity.RedisPoolProps;
import com.i1314i.syncerplusservice.entity.dto.RedisJDClousterClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * Redis集群数据迁移同步
 */
@RestController
@RequestMapping(value = "/cluster")
@Validated
public class RedisClusterSyncController {
    @Autowired
    IRedisReplicatorService redisReplicatorService;
    @Autowired
    RedisPoolProps redisPoolProps;
//    @Autowired
//    private Environment env;
    /**
     * 开启redis数据同步
     * @param syncDataDto
     * 先加载前端传过来的配置文件，如果没有则加载application.yml或.properties中参数
     * springboot中配置文件加载顺序是 .properties->.yml
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/startSyncToJDCloud",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap StartSync(@RequestBody @Validated RedisJDClousterClusterDto syncDataDto) throws TaskMsgException {

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
        redisReplicatorService.syncToJDCloud(syncDataDto);
        return ResultMap.builder().code("200").msg("success");
    }





}
