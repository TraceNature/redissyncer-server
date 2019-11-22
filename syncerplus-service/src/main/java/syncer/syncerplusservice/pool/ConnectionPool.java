package syncer.syncerplusservice.pool;


import syncer.syncerplusredis.entity.RedisURI;

/**
 * RedisClient连接池
 */
public interface ConnectionPool {


    /**
     * 初始化线程池
     * @param minActive
     * @param maxActive
     * @param maxWait
     * @param redisURI
     * @param timeBetweenEvictionRunsMillis
     * @param idleTimeRunsMillis
     */
    void init(int minActive, int maxActive, long maxWait, RedisURI redisURI, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis);




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
