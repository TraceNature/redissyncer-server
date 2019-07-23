package com.i1314i.syncerplusservice.util;

import com.i1314i.syncerpluscommon.util.common.TemplateUtils;
import com.i1314i.syncerplusservice.constant.RedisVersion;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.Impl.CommonPoolConnectionPoolImpl;
import com.i1314i.syncerplusservice.pool.Impl.ConnectionPoolImpl;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.service.exception.TaskRestoreException;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;
import com.moilioncircle.redis.replicator.Configuration;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.Replicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisDataException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static redis.clients.jedis.Protocol.Command.AUTH;

@Slf4j
public class RedisUrlUtils {
    public static boolean checkRedisUrl(String uri) throws URISyntaxException {
        URI uriplus = new URI(uri);
        if (uriplus.getScheme() != null && uriplus.getScheme().equalsIgnoreCase("redis")) {
            return true;
        }
        return false;
    }

    public static boolean getRedisClientConnectState(String url, String name) throws TaskMsgException {
        RedisURI turi = null;
        RedisClient target = null;
        try {
            turi = new RedisURI(url);

            target = new RedisClient(turi.getHost(), turi.getPort());
            Configuration tconfig = Configuration.valueOf(turi);
            //获取password
            if (tconfig.getAuthPassword() != null) {
                Object auth = target.send(AUTH, tconfig.getAuthPassword().getBytes());
            }
            try {
                target.send("GET".getBytes(), "TEST".getBytes());
                return true;
            } catch (Exception e) {
                return false;
            }

        } catch (URISyntaxException e) {
            throw new TaskMsgException(name + ":连接链接不正确");
        } catch (JedisDataException e) {
            throw new TaskMsgException(name + ":" + e.getMessage());
        } catch (TaskRestoreException e) {
            throw new TaskMsgException(name + ":error:" + e.getMessage());
        } finally {
            if (null != target) {
                target.close();
            }
        }

    }


    public static void main(String[] args) throws TaskMsgException, URISyntaxException, TaskRestoreException {
//        getRedisClientConnectState("redis://114.67.81.232:6340?authPassword=redistest010","redis");
        selectSyncerVersion("redis://114.67.81.232:6379?authPassword=redistest0102", "redis://127.0.0.1:6379?authPassword=redistest0102S");
    }


    public static RedisVersion selectSyncerVersion(String sourceUri, String targetUri) throws URISyntaxException {
        RedisURI sourceUriplus = new RedisURI(sourceUri);
        RedisURI targetUriplus = new RedisURI(targetUri);
        /**
         * 源目标
         */
        Jedis source = null;
        Jedis target = null;
        double sourceVersion = 0.0;
        double targetVersion = 0.0;
        try {
            source = new Jedis(sourceUriplus.getHost(), sourceUriplus.getPort());
            target = new Jedis(targetUriplus.getHost(), targetUriplus.getPort());
            Configuration sourceConfig = Configuration.valueOf(sourceUriplus);
            Configuration targetConfig = Configuration.valueOf(targetUriplus);

            //获取password
            if (sourceConfig.getAuthPassword() != null) {
                Object sourceAuth = source.auth(sourceConfig.getAuthPassword());
            }

            //获取password
            if (targetConfig.getAuthPassword() != null) {
                Object targetAuth = target.auth(targetConfig.getAuthPassword());
            }

            sourceVersion = TestJedisClient.getRedisVersion(source);
            targetVersion = TestJedisClient.getRedisVersion(target);
            source.close();
            target.close();
        } catch (Exception e) {

        } finally {
            if (source != null)
                source.close();
            if (target != null)
                target.close();
        }

        if (sourceVersion == targetVersion && targetVersion >= 3.0)
            return RedisVersion.SAME;
        else if (sourceVersion == targetVersion && targetVersion < 3.0) {
            return RedisVersion.LOWER;
        } else {
            return RedisVersion.OTHER;
        }
    }


    /**
     * 获取客户端
     *
     * @param syncDataDto
     * @param turi
     * @return
     */

    public static synchronized TestJedisClient getJedisClient(RedisSyncDataDto syncDataDto, RedisURI turi) {
        JedisPoolConfig sourceConfig = new JedisPoolConfig();
        Configuration sourceCon = Configuration.valueOf(turi);
        sourceConfig.setMaxTotal(syncDataDto.getMaxPoolSize());
        sourceConfig.setMaxIdle(syncDataDto.getMinPoolSize());
        sourceConfig.setMinIdle(syncDataDto.getMinPoolSize());
        //当池内没有返回对象时，最大等待时间
        sourceConfig.setMaxWaitMillis(syncDataDto.getMaxWaitTime());
        sourceConfig.setTimeBetweenEvictionRunsMillis(syncDataDto.getTimeBetweenEvictionRunsMillis());
        sourceConfig.setTestOnReturn(true);
        sourceConfig.setTestOnBorrow(true);
        return new TestJedisClient(turi.getHost(), turi.getPort(), sourceConfig, sourceCon.getAuthPassword(), 0);
    }


    /**
     * 线程检查
     *
     * @param r
     */
    public static synchronized void doCheckTask(Replicator r,Thread thread) {
        /**
         * 当aliveMap中不存在此线程时关闭
         */
        if (!TaskMonitorUtils.getAliveThreadHashMap().containsKey(thread.getName())) {
            try {
                System.out.println("线程正准备关闭...."+thread.getName());
                if (!Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }

                r.close();
                /**
                 * 清楚所有线程记录
                 */
                TaskMonitorUtils.removeAliveThread(thread.getName(), Thread.currentThread());
            } catch (IOException e) {
                TaskMonitorUtils.addDeadThread(thread.getName(), Thread.currentThread());
                log.info("数据同步关闭失败");
                e.printStackTrace();
            }
        }

    }


    public static synchronized void doCommandCheckTask(Replicator r) {
        /**
         * 当aliveMap中不存在此线程时关闭
         */
        if (!TaskMonitorUtils.getAliveThreadHashMap().containsKey(Thread.currentThread().getName())) {
            try {
                System.out.println("线程正准备关闭....");
                if (!Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }

                r.close();
                /**
                 * 清楚所有线程记录
                 */
                TaskMonitorUtils.removeAliveThread(Thread.currentThread().getName(), Thread.currentThread());
            } catch (IOException e) {
                TaskMonitorUtils.addDeadThread(Thread.currentThread().getName(), Thread.currentThread());
                log.info("数据同步关闭失败");
                e.printStackTrace();
            }
        }

    }


    public static synchronized boolean doThreadisCloseCheckTask() {
        /**
         * 当aliveMap中不存在此线程时关闭
         */
        if (!TaskMonitorUtils.getAliveThreadHashMap().containsKey(Thread.currentThread().getName()))
            return true;

        return false;
    }

    public void clearPool(ConnectionPool pools,TestJedisClient targetJedisClientPool,TestJedisClient sourceJedisClientPool){
       if(pools!=null){
           pools.close();
       }

       if(targetJedisClientPool!=null){
           targetJedisClientPool.closePool();
       }

        if(sourceJedisClientPool!=null){
            sourceJedisClientPool.closePool();
        }
    }

    public static synchronized boolean doThreadisCloseCheckTask(String name) {
        name=getOldName(name);
        /**
         * 当aliveMap中不存在此线程时关闭
         */
        if (!TaskMonitorUtils.getAliveThreadHashMap().containsKey(name))
            return true;

        return false;
    }


    public static synchronized ConnectionPool getConnectionPool() {
        ConnectionPool pools = null;

        if (StringUtils.isEmpty(TemplateUtils.getPropertiesdata("other.properties", "redispool.type")) || TemplateUtils.getPropertiesdata("other.properties", "redispool.type").trim().equals("commonpool")) {
            pools = new CommonPoolConnectionPoolImpl();
        } else if (TemplateUtils.getPropertiesdata("other.properties", "redispool.type").trim().equals("selefpool")) {
            pools = new ConnectionPoolImpl();
        }
        return pools;
    }


    public synchronized static String getNewName(String name){
        return name+"SyncerO";
    }

    public synchronized static String getOldName(String name){
        if(name.endsWith("SyncerO"))
          return name.substring(0,name.length()-7);
        return name;
    }

}
