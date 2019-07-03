package com.i1314i.syncerpluswebapp.controller;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 开启redis数据同步
     * @param syncDataDto
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/startSync",method = {RequestMethod.GET,RequestMethod.POST})
    public String StartSync(@Validated RedisSyncDataDto syncDataDto) throws TaskMsgException {
        redisReplicatorService.sync(syncDataDto);
        return "success";
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
    @RequestMapping(value = "/listAlive",method = {RequestMethod.GET})
    public String ListAlive(){
        return JSON.toJSONString(TaskMonitorUtils.getAliveThreadHashMap());
    }

    /**
     * dead线程列表
     * @return
     */
    @RequestMapping(value = "/listDead",method = {RequestMethod.GET})
    public String ListDead(){
        return JSON.toJSONString(TaskMonitorUtils.getDeadThreadHashMap());
    }
}
