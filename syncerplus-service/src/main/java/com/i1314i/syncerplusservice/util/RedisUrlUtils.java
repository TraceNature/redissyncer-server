package com.i1314i.syncerplusservice.util;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.util.common.TemplateUtils;
import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusredis.constant.KeyValueEnum;
import com.i1314i.syncerplusredis.constant.RedisVersion;
import com.i1314i.syncerplusredis.constant.TaskMsgConstant;
import com.i1314i.syncerplusredis.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusredis.entity.dto.RedisFileDataDto;
import com.i1314i.syncerplusredis.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.Impl.CommonPoolConnectionPoolImpl;
import com.i1314i.syncerplusservice.pool.Impl.ConnectionPoolImpl;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusredis.exception.TaskMsgException;
import com.i1314i.syncerplusredis.exception.TaskRestoreException;
import com.i1314i.syncerplusservice.util.Jedis.TestJedisClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import com.i1314i.syncerplusredis.util.code.CodeUtils;
import com.i1314i.syncerplusservice.util.Regex.RegexUtil;
import com.i1314i.syncerplusservice.util.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisDataException;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

import static redis.clients.jedis.Protocol.Command.AUTH;

@Slf4j
public class RedisUrlUtils {
    static Map<Double,Integer>rdbVersion=null;

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
            if (!StringUtils.isEmpty(tconfig.getAuthPassword())) {

                Object auth = target.send(AUTH, tconfig.getAuthPassword().getBytes());
            }
            int i = 3;
            while (i > 0) {
                try {
                    String png = (String) target.send("PING".getBytes());
                    System.out.println("------:"+png);
                    if (png.equals("PONG")) {
                        return true;
                    }
                    i--;
                } catch (Exception e) {

                    return false;
                }

            }


//            throw new TaskMsgException("无法连接该reids");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,TaskMsgConstant.TASK_MSG_TARGET_REDIS_CONNECT_ERROR));

        } catch (URISyntaxException e) {
//            throw new TaskMsgException(name + ":连接链接不正确");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,name + ":连接链接不正确"));
        } catch (JedisDataException e) {
//            throw new TaskMsgException(name + ":" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,name + ":" + e.getMessage()));
        } catch (TaskRestoreException e) {
//            throw new TaskMsgException(name + ":error:" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,name + ":" + e.getMessage()));
        } catch (Exception e) {
//            throw new TaskMsgException(name + ":error:" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,name + ":" + e.getMessage()));

        } finally {
            if (null != target) {
                target.close();
            }
        }

    }


    /**
     * 获取redis版本号
     * info信息中若无版本号信息则返回0L
     * @param targetUri
     * @return
     * @throws URISyntaxException
     */
    public static double selectSyncerVersion( String targetUri) throws URISyntaxException {

        RedisURI targetUriplus = new RedisURI(targetUri);
        /**
         * 源目标
         */
        Jedis target = null;
        double targetVersion = 0.0;
        try {
            target = new Jedis(targetUriplus.getHost(), targetUriplus.getPort());
            Configuration targetConfig = Configuration.valueOf(targetUriplus);

            //获取password
            if (!StringUtils.isEmpty(targetConfig.getAuthPassword())) {
                Object targetAuth = target.auth(targetConfig.getAuthPassword());
            }
            String info=target.info();

            targetVersion = TestJedisClient.getRedisVersion(info);

        } catch (Exception e) {

        } finally {
            if (target != null)
                target.close();
        }
        return targetVersion;
    }


    /**
     * 获取target和source之间的版本对应关系
     * @param sourceUri
     * @param targetUri
     * @return
     * @throws URISyntaxException
     */
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
            if (!StringUtils.isEmpty(sourceConfig.getAuthPassword())) {
                Object sourceAuth = source.auth(sourceConfig.getAuthPassword());
            }

            //获取password
            if (!StringUtils.isEmpty(targetConfig.getAuthPassword())) {
                Object targetAuth = target.auth(targetConfig.getAuthPassword());
            }

            String sourceInfo=source.info();
            sourceVersion = TestJedisClient.getRedisVersion(sourceInfo);
            String targetInfo=target.info();
            targetVersion = TestJedisClient.getRedisVersion(targetInfo);

        } catch (Exception e) {

        } finally {
            if (source != null)
                source.close();
            if (target != null)
                target.close();
        }
        System.out.println(sourceVersion+": "+targetVersion);
        if (sourceVersion == targetVersion && targetVersion >= 3.0)
            return RedisVersion.SAME;
        else if (sourceVersion == targetVersion && targetVersion < 3.0) {
            return RedisVersion.LOWER;
        } else {
            return RedisVersion.OTHER;
        }
    }


    /**
     * 从配置文件中获取rdb版本号
     * @param redisVersion
     * @return
     */
    public static synchronized  Integer getRdbVersion(Double redisVersion){
        Object lock=new Object();
        //单例模式
        if(rdbVersion==null){
            //加锁维持多任务的线程安全
            synchronized (lock){
                if(rdbVersion==null){
                    rdbVersion = (Map)JSON.parse(FileUtils.getText(TemplateUtils.class.getClassLoader().getResourceAsStream(
                            "rdbsetting.json")));
                }
            }

        }


        Object rdb=rdbVersion.get(redisVersion);

        if(rdb!=null){
            return  Integer.valueOf((String) rdb);
        }
        if(rdb instanceof  Integer){
            return (Integer) rdb;
        }else {
            return 0;
        }


    }



    /**
     * 获取redis DB库的数目
     * @param sourceUriList
     * @param dbMap
     * @throws URISyntaxException
     * @throws TaskMsgException
     */
    public static void doCheckDbNum(Set<String> sourceUriList, Map<Integer,Integer> dbMap, KeyValueEnum type) throws URISyntaxException,TaskMsgException {
        if(dbMap==null||dbMap.size()==0){
            return;
        }
        int[]maxInt=getMapMaxInteger(dbMap);
        for (String sourceUri:sourceUriList) {
            RedisURI sourceUriplus = new RedisURI(sourceUri);
            Jedis source = null;
            try {
                source = new Jedis(sourceUriplus.getHost(), sourceUriplus.getPort());
                Configuration sourceConfig = Configuration.valueOf(sourceUriplus);
                //获取password
                if (!StringUtils.isEmpty(sourceConfig.getAuthPassword())) {
                    Object sourceAuth = source.auth(sourceConfig.getAuthPassword());
                }

                List<String>databases=source.configGet("databases");
                int dbNum=256;
                if(null!=databases&&databases.size()==2){
                    dbNum= Integer.parseInt(databases.get(1));
                }

               if (type.equals(KeyValueEnum.KEY)){
                   if(maxInt[0]>dbNum){
//                       throw new TaskMsgException("dbMaping中库号超出Redis库的最大大小 :["+maxInt[0]+"] : "+sourceUri);

                       throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_DB_ERROR_CODE,"dbMaping中库号超出Redis库的最大大小 :["+maxInt[0]+"] : "+sourceUri));
                   }
               }

               if(type.equals(KeyValueEnum.VALUE)){
                   if(maxInt[1]>dbNum){
                       throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_DB_ERROR_CODE,"dbMaping中库号超出Redis库的最大大小 :["+maxInt[1]+"] : "+sourceUri));

//                       throw new TaskMsgException("dbMaping中库号超出Redis库的最大大小 :["+maxInt[1]+"] : "+sourceUri);
                   }
               }


            } catch (Exception e) {
                throw new TaskMsgException("sourceUri is error :"+e.getMessage());
            } finally {
                if (source != null)
                    source.close();
            }
        }
    }

    /**
     * 初始化
     */


    /**
     * 获取Map<Integer,Integer> key-value的最大值
     * @param dbMap
     * @return
     */
    public static synchronized   int[] getMapMaxInteger(Map<Integer, Integer> dbMap){
        int[]nums=new int[2];


//        List<Integer>list= dbMap.entrySet().stream().map(e ->e.getKey()).collect(Collectors.toList());
        nums[0] = dbMap.entrySet().stream().map(e ->e.getKey())
                .max(Comparator.comparing(i -> i))
                .get();
        nums[1] = dbMap.entrySet().stream().map(e ->e.getValue())
                .max(Comparator.comparing(i -> i))
                .get();

        return nums;
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
     *获取JDJEDISCLIENT 链接
     * @param syncDataDto
     * @param turi
     * @return
     */
    public static synchronized JDJedisClientPool getJDJedisClient(RedisSyncDataDto syncDataDto, RedisURI turi) {
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
        return new JDJedisClientPool(turi.getHost(), turi.getPort(), sourceConfig, sourceCon.getAuthPassword(), 0);
    }



    public static synchronized JDJedisClientPool getJDJedisClients(RedisSyncDataDto syncDataDto, RedisURI turi) {

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
        return new JDJedisClientPool(turi.getHost(), turi.getPort(), sourceConfig, sourceCon.getAuthPassword(), 0);
    }



    public void clearPool(ConnectionPool pools, TestJedisClient targetJedisClientPool, TestJedisClient sourceJedisClientPool) {
        if (pools != null) {
            pools.close();
        }

        if (targetJedisClientPool != null) {
            targetJedisClientPool.closePool();
        }

        if (sourceJedisClientPool != null) {
            sourceJedisClientPool.closePool();
        }
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

    public static synchronized SyncJedisClusterClient getConnectionClusterPool(RedisClusterDto syncDataDto) throws ParseException {
//        JedisClusterClient pools = new JedisClusterClient(syncDataDto.getTargetRedisAddress(),syncDataDto.getTargetPassword(),
//                syncDataDto.getMaxPoolSize(),
//               3000,
//                syncDataDto.getTimeBetweenEvictionRunsMillis(),15000);

        SyncJedisClusterClient pools=new SyncJedisClusterClient( syncDataDto.getTargetRedisAddress(),syncDataDto.getTargetPassword(),syncDataDto.getMaxPoolSize(),syncDataDto.getTargetUris().size(),syncDataDto.getMaxWaitTime(),10000);

        return pools;
    }





    public synchronized static String getNewName(String name) {
        return name + "SyncerO";
    }

    public synchronized static String getOldName(String name) {
        if (name.endsWith("SyncerO"))
            return name.substring(0, name.length() - 7);
        return name;
    }


//    public static synchronized Integer getRdbVersion(double redisVersion){
//        if(rdbVersion==null){
//            rdbVersion= (Map<Double, Integer>) JSON.parse(FileUtils.getText(TemplateUtils.class.getClassLoader().getResourceAsStream(
//                    "rdbsetting.json")));
//        }
//
//
//        System.out.println(redisVersion);
//        System.out.println("===: "+rdbVersion.get(redisVersion));
////        Integer  rdbVer=rdbVersion.get(redisVersion);
////        if(rdbVer==null)
////            return 0;
//
//        return 0;
//    }




    /**
     * 检查reids是否能够连接
     *
     * @param url
     * @param name
     * @throws TaskMsgException
     */
     public static synchronized void  checkRedisUrl(String url, String name) throws TaskMsgException {

        try {
            if (!RedisUrlUtils.checkRedisUrl(url)) {

                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_URI_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_URI_ERROR));

//                throw new TaskMsgException("scheme must be [redis].");
            }
            if (!RedisUrlUtils.getRedisClientConnectState(url, name)) {
//                throw new TaskMsgException(name + " :连接redis失败");

                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,TaskMsgConstant.TASK_MSG_TARGET_REDIS_CONNECT_ERROR));

            }
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }
    }


    /**
     * 获取buffer值
     */

    public static String[] selectSyncerBuffer( String targetUri,String type) throws URISyntaxException {

        RedisURI targetUriplus = new RedisURI(targetUri);
        /**
         * 源目标
         */
        Jedis target = null;
        String[] version = new String[2];
        version[0]="0";

        try {
            target = new Jedis(targetUriplus.getHost(), targetUriplus.getPort());
            Configuration targetConfig = Configuration.valueOf(targetUriplus);

            //获取password
            if (!StringUtils.isEmpty(targetConfig.getAuthPassword())) {
                Object targetAuth = target.auth(targetConfig.getAuthPassword());
            }
            String info=target.info();

            version = getRedisBuffer(info,type);

        } catch (Exception e) {

        } finally {
            if (target != null)
                target.close();
        }
        return version;
    }


    private static String[] getRedisBuffer(String info,String type) {
        String[] version = new String[2];
        version[0]="0";
        try {
            String firstRgex = "repl_backlog_first_byte_offset:(.*?)\r\n";
            String endRrgex = "master_repl_offset:(.*?)\r\n";
            String replid="master_replid:(.*?)\r\n";
            String runIdRrgex = "run_id:(.*?)\r\n";
            if(type.trim().equals("endbuf")){
                if(RegexUtil.getSubUtilSimple(info, endRrgex).length()>0){
                    version[0] = RegexUtil.getSubUtilSimple(info, endRrgex);
                }
            }else {
                if(RegexUtil.getSubUtilSimple(info, firstRgex).length()>0){
                    version[0] = RegexUtil.getSubUtilSimple(info, firstRgex);
                }
            }

            String runid1=RegexUtil.getSubUtilSimple(info, replid);
            System.out.println("runid1:"+runid1);
            if(!StringUtils.isEmpty(runid1.trim())){
                version[1]=runid1;
            }else {
                version[1]=RegexUtil.getSubUtilSimple(info, runIdRrgex);
            }

        } catch (Exception e) {

        } finally {
//            if(jedisClient!=null){
//                jedisClient.close();
//            }

        }
        return version;

    }



    public static void main(String[] args) throws URISyntaxException, TaskMsgException {
//        Map<Integer,Integer>map=new HashMap<>();
//        map.put(1,11);
//        map.put(2,22);
//        map.put(3,33);
//        doCheckDbNum(Stream.of("redis://114.67.100.239:6379?authPassword=redistest0102").collect(Collectors.toList()),map,KeyValueEnum.VALUE);
//        System.out.println(JSON.toJSONString(getMapMaxInteger(map)));

        System.out.println(JSON.toJSONString(selectSyncerBuffer("redis://114.67.100.238:6379?authPassword=redistest0102","endbuf")));
    }






}
