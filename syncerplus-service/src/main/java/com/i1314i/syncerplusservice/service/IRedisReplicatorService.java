package com.i1314i.syncerplusservice.service;

import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisJDClousterClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import org.springframework.stereotype.Service;

@Service
public interface IRedisReplicatorService {
    /**
     * AOF备份
     * @param redisPath
     * @param aofPath
     */
    void backupAof(String redisPath, String aofPath);

    /**
     * RDB备份
     * @param redisPath
     * @param path
     */
    void backUPRdb(String redisPath, String path);

    void sync(String sourceUri, String targetUri) throws TaskMsgException;
    void sync(String sourceUri, String targetUri,String threadName) throws TaskMsgException;
    void sync(RedisSyncDataDto syncDataDto) throws TaskMsgException;

    void sync(RedisClusterDto clusterDto) throws TaskMsgException;

    void syncToJDCloud(RedisJDClousterClusterDto jdClousterClusterDto) throws TaskMsgException;
}
