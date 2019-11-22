package syncer.syncerplusservice.service;

import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import org.springframework.stereotype.Service;

@Service
public interface IRedisReplicatorService {
    /**
     * AOF备份
     * @param redisPath
     * @param aofPath
     */
//    void backupAof(String redisPath, String aofPath);

    /**
     * RDB备份
     * @param redisPath
     * @param path
     */
//    void backUPRdb(String redisPath, String path);

//    void sync(String sourceUri, String targetUri) throws TaskMsgException;
//    void sync(String sourceUri, String targetUri,String threadName) throws TaskMsgException;
//    void sync(RedisSyncDataDto syncDataDto) throws TaskMsgException;

//    void sync(RedisClusterDto clusterDto) throws TaskMsgException;
//    void batchedSync(RedisClusterDto clusterDto) throws TaskMsgException;
    void batchedSync(RedisClusterDto clusterDto,String taskId,boolean afresh) throws TaskMsgException;


    void filebatchedSync(RedisClusterDto clusterDto,String taskId) throws TaskMsgException;
//    void syncToJDCloud(RedisJDClousterClusterDto jdClousterClusterDto) throws TaskMsgException;
}
