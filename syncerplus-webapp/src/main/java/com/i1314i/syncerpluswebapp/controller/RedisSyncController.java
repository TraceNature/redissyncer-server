package com.i1314i.syncerpluswebapp.controller;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    @RequestMapping(value = "/startSync",method = {RequestMethod.GET,RequestMethod.POST})
    public String StartSync(@Validated RedisSyncDataDto syncDataDto) throws TaskMsgException {

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
        return JSON.toJSONString(syncDataDto);
    }

    /**
     * 根据任务名称关闭相关任务线程
     * @return
     */
    @RequestMapping(value = "/closeSync",method = {RequestMethod.GET,RequestMethod.POST})
    public String CloseSync( @NotBlank(message="任务名不能为空") String name){
        TaskMonitorUtils.removeAliveThread(name);
        return "success";
    }


    /**
     * alive线程列表
     * @return
     */
    @RequestMapping(value = "/listAlive",method = {RequestMethod.GET,RequestMethod.POST})
    public String ListAlive(){
        return JSON.toJSONString(TaskMonitorUtils.getAliveThreadHashMap());
    }

    /**
     * dead线程列表
     * @return
     */
    @RequestMapping(value = "/listDead",method = {RequestMethod.GET,RequestMethod.POST})
    public String ListDead(){
        return JSON.toJSONString(TaskMonitorUtils.getDeadThreadHashMap());
    }
}
