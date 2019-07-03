package com.i1314i.syncerpluswebapp.controller;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {
    @Autowired
    IRedisReplicatorService redisReplicatorService;
    public static void main(String[] args) {

    }

    @RequestMapping(value = "/test")
    public String success(String name){
//       TemplateUtils.writeData("other.properties","sss","123");
//        System.out.println(TemplateUtils.getPropertiesdata("other.properties","sss"));


        redisReplicatorService.sync("redis://114.67.81.232:6379?authPassword=redistest0102", "redis://114.67.81.232:6340?authPassword=redistest0102",name);
        return "success";
    }



    @RequestMapping(value = "/test1")
    public String success1(String name){
//       TemplateUtils.writeData("other.properties","sss","123");
//        System.out.println(TemplateUtils.getPropertiesdata("other.properties","sss"));

        TaskMonitorUtils.removeAliveThread(name);
        return "success";
    }

    @RequestMapping(value = "/test2")
    public String success2(){
//       TemplateUtils.writeData("other.properties","sss","123");
//        System.out.println(TemplateUtils.getPropertiesdata("other.properties","sss"));

        return JSON.toJSONString(TaskMonitorUtils.getAliveThreadHashMap());
    }


    @RequestMapping(value = "/test3")
    public String success3(){
//       TemplateUtils.writeData("other.properties","sss","123");
//        System.out.println(TemplateUtils.getPropertiesdata("other.properties","sss"));

        return JSON.toJSONString(TaskMonitorUtils.getThreadStateHashMap());
    }

    @RequestMapping(value = "/test4")
    public String success4(){
//       TemplateUtils.writeData("other.properties","sss","123");
//        System.out.println(TemplateUtils.getPropertiesdata("other.properties","sss"));

        return JSON.toJSONString( TaskMonitorUtils.getDeadThreadHashMap());
    }
}
