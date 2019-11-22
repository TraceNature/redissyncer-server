package syncer.syncerplusservice.service;

import syncer.syncerplusredis.entity.dto.RedisAofSyncDataDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
public interface IRedisAofReplicatorService {


    void sync(RedisAofSyncDataDto syncDataDto) throws TaskMsgException, IOException, URISyntaxException;

}
