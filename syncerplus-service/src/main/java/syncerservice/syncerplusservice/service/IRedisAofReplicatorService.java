package syncerservice.syncerplusservice.service;

import syncerservice.syncerplusredis.entity.dto.RedisAofSyncDataDto;
import syncerservice.syncerplusredis.exception.TaskMsgException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
public interface IRedisAofReplicatorService {


    void sync(RedisAofSyncDataDto syncDataDto) throws TaskMsgException, IOException, URISyntaxException;

}
