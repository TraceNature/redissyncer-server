package com.i1314i.syncerplusservice.service;

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

    void sync(String sourceUri, String targetUri);
    void sync(String sourceUri, String targetUri,String threadName);
}
