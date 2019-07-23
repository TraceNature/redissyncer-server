package com.i1314i.syncerplusservice.task.singleTask.defaultVersion;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.service.exception.TaskRestoreException;
import com.i1314i.syncerplusservice.util.Jedis.IJedisClient;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.SetParams;

import java.util.concurrent.Callable;


/***
 * 当service层设置的版本条件都不符合时执行本同步线程
 */
@Slf4j
public class RdbRestoreTask implements Callable<Object> {
    private DumpKeyValuePair mkv;
    long ms;
    private RedisClient redisClient;
    private ConnectionPool pool;
    private boolean status;
    private StringBuffer info;
    private Jedis targetJedis;
    private Jedis sourceJedis;
//    public RdbRestoreTask(DumpKeyValuePair mkv, long ms, RedisClient redisClient, ConnectionPool pool, boolean status) {
//        this.mkv = mkv;
//        this.ms = ms;
//        this.redisClient = redisClient;
//        this.pool = pool;
//        this.status = status;
//        this.info=new StringBuffer();
//    }

//    public RdbRestoreTask(DumpKeyValuePair mkv, long ms, RedisClient redisClient, ConnectionPool pool, boolean status, StringBuffer info) {
//        this.mkv = mkv;
//        this.ms = ms;
//        this.redisClient = redisClient;
//        this.pool = pool;
//        this.status = status;
//        this.info = info;
//    }

    public RdbRestoreTask(DumpKeyValuePair mkv, long ms, RedisClient redisClient, ConnectionPool pool, boolean status, StringBuffer info,Jedis targetJedis,Jedis sourceJedis) {
        this.mkv = mkv;
        this.ms = ms;
        this.redisClient = redisClient;
        this.pool = pool;
        this.status = status;
        this.info = info;
        this.targetJedis=targetJedis;
        this.sourceJedis=sourceJedis;
    }


    @Override
    public Object call() throws Exception {

        Object r = null;
        try {
            r = redisClient.restore(mkv.getKey(), ms, mkv.getValue(), status);
        } catch (TaskRestoreException e) {

            if(e.getMessage().trim().indexOf("ERR wrong number of arguments for 'restore' command")>=0){
                try {
                    r=TestJedisClient.restorebyteObject(mkv.getKey(),mkv.getValue(),mkv.getExpiredSeconds(),targetJedis,status);
                    info.append(mkv.getKey());
                    info.append("->");
                    info.append(r.toString());
                    log.info(info.toString());
                    return r;
                }catch (JedisDataException ex){
                    if(ex.getMessage().trim().indexOf("ERR DUMP payload version or checksum are wrong")>=0){
                        int i=3;
                        while (i>0){
                            try {
                                byte[]data=sourceJedis.get(mkv.getKey());
                                if(mkv.getExpiredMs()==null){
                                    r=targetJedis.set(mkv.getKey(),data);
                                }else {
                                    r=targetJedis.set(mkv.getKey(),data,new SetParams().px(mkv.getExpiredMs()));
                                }



                                info.append(mkv.getKey());
                                info.append("->");
                                info.append(r.toString());
                                log.info(info.toString());
                                break;
                            }catch (Exception epx){
                                i--;
                                System.out.println(epx.getMessage()+": "+i+":" +mkv.getKey()+": "+mkv.getExpiredMs());
                            }
                        }

                        return r;


                    }
                }

            }

        }finally {

            if (redisClient!=null){
                pool.release(redisClient);
            }
            if(targetJedis!=null){
                TestJedisClient.returnStaticBrokenResource(targetJedis);
            }


            if(sourceJedis!=null){
                TestJedisClient.returnStaticBrokenResource(sourceJedis);
            }
        }
        pool.release(redisClient);
        info.append(mkv.getKey());
        info.append("->");
        info.append(r.toString());
        log.info(info.toString());
        return r;
    }
}
