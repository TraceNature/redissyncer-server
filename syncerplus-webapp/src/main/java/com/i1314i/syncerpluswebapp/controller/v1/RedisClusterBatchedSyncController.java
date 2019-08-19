package com.i1314i.syncerpluswebapp.controller.v1;

import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerplusservice.entity.RedisPoolProps;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerpluswebapp.util.DtoCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redis集群数据迁移同步
 */
@RestController
@RequestMapping(value = "/v1")
@Validated
public class RedisClusterBatchedSyncController {
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


        redisReplicatorService.batchedSync(redisClusterDto);


//        redisReplicatorService.syncToJDCloud(syncDataDto);
        return ResultMap.builder().code("200").msg("success");
    }






}
