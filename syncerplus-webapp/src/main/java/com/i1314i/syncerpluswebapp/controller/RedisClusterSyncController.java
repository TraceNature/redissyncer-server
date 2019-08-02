package com.i1314i.syncerpluswebapp.controller;

import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerplusservice.entity.RedisPoolProps;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisJDClousterClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import com.i1314i.syncerpluswebapp.util.DtoCheckUtils;
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
     * @param redisClusterDto
     * 先加载前端传过来的配置文件，如果没有则加载application.yml或.properties中参数
     * springboot中配置文件加载顺序是 .properties->.yml
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/startSync",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap StartSync(@RequestBody @Validated RedisClusterDto redisClusterDto) throws TaskMsgException {


        redisClusterDto= (RedisClusterDto) DtoCheckUtils.ckeckRedisClusterDto(redisClusterDto,redisPoolProps);


        redisReplicatorService.sync(redisClusterDto);


//        redisReplicatorService.syncToJDCloud(syncDataDto);
        return ResultMap.builder().code("200").msg("success");
    }







}
