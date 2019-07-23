package com.i1314i.syncerplusservice.service;

import com.i1314i.syncerplusservice.entity.dto.RedisAofSyncDataDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
public interface IRedisAofReplicatorService {


    void sync(RedisAofSyncDataDto syncDataDto) throws TaskMsgException, IOException, URISyntaxException;

}
