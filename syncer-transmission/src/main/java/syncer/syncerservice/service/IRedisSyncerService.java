package syncer.syncerservice.service;

import org.springframework.stereotype.Service;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.exception.TaskMsgException;

@Service
public interface IRedisSyncerService {
    void batchedSync(RedisClusterDto clusterDto, String taskId, boolean afresh) throws TaskMsgException;
    void filebatchedSync(RedisClusterDto clusterDto, String taskId) throws TaskMsgException;
}
