package com.i1314i.syncerplusservice.pool;

import com.moilioncircle.redis.replicator.RedisURI;


/**
 * RedisClient连接池
 */
public interface ConnectionPool {

    /**
     * 初始化线程池
     * max 最大连接数
     * maxWait 最大等待时间
     * */
    void init(int maxActive, long maxWait);

    /**
     * 初始化线程池
     * @param maxActive 最大连接数
     * @param maxWait  最大等待数
     * @param redisURI  redisUrl
     */
    void init(int maxActive, long maxWait, RedisURI redisURI);


    /**
     * 获取连接
     * @return
     * @throws Exception
     */
    RedisClient borrowResource()throws Exception;

    /**
     * 释放连接
     * @param redisClient
     * @throws Exception
     */
    void release(RedisClient redisClient)throws Exception;


    /**
     * 清空所有连接 关闭
     */
    void close();

}
