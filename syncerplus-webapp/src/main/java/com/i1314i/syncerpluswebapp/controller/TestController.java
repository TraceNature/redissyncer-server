package com.i1314i.syncerpluswebapp.controller;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class TestController {
    @Autowired
    IRedisReplicatorService redisReplicatorService;
    public static void main(String[] args) {

    }

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
        return JSON.toJSONString( TaskMonitorUtils.getDeadThreadHashMap());
    }
}
