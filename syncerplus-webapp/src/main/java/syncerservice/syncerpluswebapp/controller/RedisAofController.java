package syncerservice.syncerpluswebapp.controller;

import syncerservice.syncerplusredis.entity.dto.RedisAofSyncDataDto;

import syncerservice.syncerplusservice.service.IRedisAofReplicatorService;
import syncerservice.syncerplusredis.exception.TaskMsgException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping(value = "/sync")
@Validated
public class RedisAofController {
    @Autowired
    private IRedisAofReplicatorService redisAofReplicatorService;
    @RequestMapping(value = "/aof/sync")
    public String test(@RequestBody @Validated RedisAofSyncDataDto syncDataDto) throws URISyntaxException, IOException, TaskMsgException {
        redisAofReplicatorService.sync(syncDataDto);
        return "success";
    }
}
