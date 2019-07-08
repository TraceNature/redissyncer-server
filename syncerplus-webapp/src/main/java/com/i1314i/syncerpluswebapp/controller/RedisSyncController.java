package com.i1314i.syncerpluswebapp.controller;

import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerplusservice.entity.RedisPoolProps;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * Redis数据迁移同步
 */
@RestController
@RequestMapping(value = "/sync")
@Validated
public class RedisSyncController {
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
    @RequestMapping(value = "/startSync",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap StartSync(@RequestBody @Validated RedisSyncDataDto syncDataDto) throws TaskMsgException {

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
        redisReplicatorService.sync(syncDataDto);
        return ResultMap.builder().code("200").msg("success");
    }

    /**
     * 根据任务名称关闭相关任务线程
     * @return
     */
    @RequestMapping(value = "/closeSync/{name}",method = {RequestMethod.DELETE})
    public ResultMap CloseSync( @PathVariable("name") @NotBlank(message="任务名不能为空") String name){
        if(!TaskMonitorUtils.containsKeyAliveMap(name)){
            return ResultMap.builder().code("101").msg("任务不存在");
        }
        TaskMonitorUtils.removeAliveThread(name);
        return ResultMap.builder().code("200").msg("success");
    }


    /**
     * alive线程列表
     * @return
     */
    @RequestMapping(value = "/listAlive",method = {RequestMethod.GET})
    public ResultMap ListAlive(){
        return ResultMap.builder().code("200").msg("success").data(TaskMonitorUtils.getAliveThreadHashMap().keySet());
    }

    /**
     * dead线程列表
     * @return
     */
    @RequestMapping(value = "/listDead",method = {RequestMethod.GET})
    public ResultMap ListDead(){
        return ResultMap.builder().code("200").msg("success").data(TaskMonitorUtils.getDeadThreadHashMap().keySet());
    }

}
