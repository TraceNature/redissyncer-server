package com.i1314i.syncerpluswebapp.controller;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    private Environment env;
    /**
     * 开启redis数据同步
     * @param syncDataDto
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/startSync",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap StartSync(@RequestBody @Validated RedisSyncDataDto syncDataDto) throws TaskMsgException {

        if(syncDataDto.getIdleTimeRunsMillis()==0){
            syncDataDto.setIdleTimeRunsMillis(Long.parseLong(env.getProperty("syncerplus.redispool.idleTimeRunsMillis")));
        }
        if(syncDataDto.getMaxWaitTime()==0){
            syncDataDto.setMaxWaitTime(Long.parseLong(env.getProperty("syncerplus.redispool.maxWaitTime")));
        }
        if(syncDataDto.getMaxPoolSize()==0){
            syncDataDto.setMaxPoolSize(Integer.parseInt(env.getProperty("syncerplus.redispool.maxPoolSize")));
        }
        if(syncDataDto.getMinPoolSize()==0){
            syncDataDto.setMinPoolSize(Integer.parseInt(env.getProperty("syncerplus.redispool.minPoolSize")));
        }
        syncDataDto.setTimeBetweenEvictionRunsMillis(Long.parseLong(env.getProperty("syncerplus.redispool.timeBetweenEvictionRunsMillis")));
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
