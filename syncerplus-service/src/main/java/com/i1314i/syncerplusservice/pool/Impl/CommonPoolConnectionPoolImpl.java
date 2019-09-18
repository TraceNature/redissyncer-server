package com.i1314i.syncerplusservice.pool.Impl;

import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.pool.factory.CommonPoolConnectionFactory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

@Slf4j
public class CommonPoolConnectionPoolImpl implements ConnectionPool {
    private CommonPoolConnectionFactory poolFactory = null;
    private GenericObjectPool pool;
    @Override
    public void init(int minActive, int maxActive, long maxWait, RedisURI redisURI, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis) {
        Config config = new Config();
        config.minIdle=minActive;
        config.maxActive=maxActive;
        config.maxWait=maxWait;
        config.timeBetweenEvictionRunsMillis=timeBetweenEvictionRunsMillis;

        /**
         * 指定池中对象被消耗完以后的行为，有下面这些选择：
         *
         * WHEN_EXHAUSTED_FAIL                  0
         *
         * WHEN_EXHAUSTED_GROW             2
         *
         * WHEN_EXHAUSTED_BLOCK             1
         * 如果是WHEN_EXHAUSTED_FAIL，当池中对象达到上限以后，继续borrowObject会抛出NoSuchElementException异常。
         *
         * 如果是WHEN_EXHAUSTED_GROW，当池中对象达到上限以后，会创建一个新对象，并返回它。
         *
         * 如果是WHEN_EXHAUSTED_BLOCK，当池中对象达到上限以后，会一直等待，直到有一个对象可用。
         * 这个行为还与maxWait有关，如果maxWait是正数，那么会等待maxWait的毫秒的时间，
         * 超时会抛出NoSuchElementException异常；如果maxWait为负值，会永久等待。
         */
        config.whenExhaustedAction= 1;
        config.minEvictableIdleTimeMillis=idleTimeRunsMillis;
        if(poolFactory==null){
           poolFactory=new CommonPoolConnectionFactory(minActive, maxActive, maxWait,redisURI,timeBetweenEvictionRunsMillis,idleTimeRunsMillis);
       }

        pool = new GenericObjectPool(poolFactory, config);
    }

    @Override
    public RedisClient borrowResource() throws Exception {
        return (RedisClient) pool.borrowObject();
    }

    @Override
    public void release(RedisClient redisClient) throws Exception {
        try{
            pool.returnObject(redisClient);
        }catch(Exception e){
            if(redisClient != null){
                try{
                    redisClient.close();
                }catch(Exception ex){
                    //
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            pool.close();
        } catch (Exception e) {
            pool.clear();
            log.info("redis pool 关闭失败");
        }
    }
}
