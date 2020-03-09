package syncer.syncerservice.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.syncerjedis.Jedis;
import syncer.syncerjedis.exceptions.JedisDataException;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.constant.KeyValueEnum;
import syncer.syncerplusredis.constant.RedisVersion;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.exception.TaskRestoreException;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.pool.RedisClient;
import syncer.syncerservice.util.file.FileUtils;
import syncer.syncerservice.util.jedis.TestJedisClient;
import syncer.syncerservice.util.regex.RegexUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static syncer.syncerjedis.Protocol.Command.AUTH;

@Slf4j
public class RedisUrlCheckUtils {
    //rdb版本和redis版本映射关系
    static Map<String,Integer> rdbVersion=null;


    /**
     * check redis的uri是否符合规范
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    public static boolean checkRedisUrl(String uri) throws URISyntaxException {
        URI uriplus = new URI(uri);
        if (uriplus.getScheme() != null && "redis".equalsIgnoreCase(uriplus.getScheme())) {
            return true;
        }
        return false;
    }


    /**
     * 检查reids是否能够连接
     *
     * @param url
     * @param name
     * @throws TaskMsgException
     */
    public static synchronized void  checkRedisUrl(String url, String name) throws TaskMsgException {
        try {
            if (!checkRedisUrl(url)) {
                //TaskMsgException("scheme must be [redis].");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_URI_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_URI_ERROR));
            }
            if (!getRedisClientConnectState(url, name)) {
                // TaskMsgException(name + " :连接redis失败");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,TaskMsgConstant.TASK_MSG_TARGET_REDIS_CONNECT_ERROR));
            }
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }
    }


    /**
     * 检查Redis连接状态
     * @param url
     * @param name
     * @return
     * @throws TaskMsgException
     */
    public synchronized static boolean getRedisClientConnectState(String url, String name) throws TaskMsgException {
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

            //重试三次
            int i = 3;
            while (i > 0) {
                try {
                    String png = (String) target.send("PING".getBytes());
                    System.out.println(url.split("\\?")[0]+"------:"+png);
                    if ("PONG".equalsIgnoreCase(png)) {
                        log.info("心跳检测成功");
                        return true;
                    }
                    i--;
                } catch (Exception e) {
                    return false;
                }

            }

            if(i > 0){
                return true;
            }
            //TaskMsgException("无法连接该reids");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,TaskMsgConstant.TASK_MSG_TARGET_REDIS_CONNECT_ERROR));
        } catch (URISyntaxException e) {
            //TaskMsgException(name + ":连接链接不正确");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,name + ":连接链接不正确"));
        } catch (JedisDataException e) {
            //TaskMsgException(name + ":" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,name + ":" + e.getMessage()));
        } catch (TaskRestoreException e) {
            //TaskMsgException(name + ":error:" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,name + ":" + e.getMessage()));
        } catch (Exception e) {
            // TaskMsgException(name + ":error:" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,name + ":" + e.getMessage()));
        } finally {
            if (null != target) {
                target.close();
            }
        }

    }


    /**
     * 查询redisKey数量
     * @param host
     * @param port
     * @param password
     * @return
     * @throws TaskMsgException
     */
    public static List<List<String>> getRedisClientKeyNum(String host,Integer port, String password) throws TaskMsgException {
        RedisClient target = null;
        try {
            target = new RedisClient(host, port);
            //获取password
            if (!StringUtils.isEmpty(password)) {
                Object auth = target.send(AUTH, password.getBytes());
            }

            //重试三次
            int i = 3;
            while (i > 0) {
                try {
                    String png = (String) target.send("PING".getBytes());

                    if ("PONG".equals(png)) {

                    }
                    i--;
                } catch (Exception e) {
                    throw  e;
                }
            }
            String infoRes = (String) target.send("INFO".getBytes(),"Keyspace".getBytes());
            String rgex = "db(.*?):keys=(.*?),";
            List<List<String>>res=RegexUtil.getSubListUtil(infoRes,rgex,2);
            return res;

        } catch (JedisDataException e) {
            //TaskMsgException(name + ":" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,host+":"+port +  ":" + e.getMessage()));
        } catch (TaskRestoreException e) {
            //TaskMsgException(name + ":error:" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,host+":"+port + ":" + e.getMessage()));
        } catch (Exception e) {
            // TaskMsgException(name + ":error:" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,host+":"+port + ":" + e.getMessage()));
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
    public static String selectSyncerVersion( String targetUri) throws URISyntaxException, TaskMsgException {

        RedisURI targetUriplus = new RedisURI(targetUri);
        /**
         * 源目标
         */
        Jedis target = null;
        String targetVersion = null;
        try {
            target = new Jedis(targetUriplus.getHost(), targetUriplus.getPort());
            Configuration targetConfig = Configuration.valueOf(targetUriplus);

            //获取password
            if (!StringUtils.isEmpty(targetConfig.getAuthPassword())) {
                Object targetAuth = target.auth(targetConfig.getAuthPassword());
            }
            String info=target.info();

            targetVersion = TestJedisClient.getRedisVersionString(info);

        } catch (Exception e) {
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,e.getMessage()));

        } finally {
            if (target != null) {
                target.close();
            }
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
            if (source != null) {
                source.close();
            }
            if (target != null) {
                target.close();
            }
        }
        System.out.println(sourceVersion+": "+targetVersion);
        if (sourceVersion == targetVersion && targetVersion >= 3.0) {
            return RedisVersion.SAME;
        } else if (sourceVersion == targetVersion && targetVersion < 3.0) {
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
    public static synchronized  Integer getRdbVersion(String redisVersion){
        Object lock=new Object();
        //单例模式
        if(rdbVersion==null){
            //加锁维持多任务的线程安全
            synchronized (lock){
                if(rdbVersion==null){
                    rdbVersion = (Map) JSON.parse(FileUtils.getText(TemplateUtils.class.getClassLoader().getResourceAsStream(
                            "rdbsetting.json")));
                }
            }

        }

        if(null==redisVersion){
            return 0;
        }

        Object rdb=0;
        if(rdbVersion.containsKey(redisVersion)){
            rdb=rdbVersion.get(redisVersion);

             return  Integer.valueOf((String) rdb);
        }else {

            if(redisVersion.indexOf("jimdb")>=0){
                rdb=6;
                return  Integer.valueOf((String) rdb);
            }else {
                if(redisVersion!=null&&redisVersion.length()>1){
                    String newVersion=redisVersion.substring(0, 1);
                    if(rdbVersion.containsKey(newVersion)){
                        rdb=rdbVersion.get(redisVersion);
                        return  Integer.valueOf((String) rdb);
                    }
                }

            }
        }



        if(rdb!=null){

            if(rdb instanceof  String){
                return  Integer.valueOf((String) rdb);
            }else if(rdb instanceof  Integer){
                return (Integer) rdb;
            }
            return  Integer.valueOf((String) rdb);
        }else {
            return 0;
        }




    }



    /**
     * 检查redis DB库的数目
     * @param sourceUriList
     * @param dbMap  无参数时默认为256
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
                List<String> databases=source.configGet("databases");
                int dbNum=256;
                if(null!=databases&&databases.size()==2){
                    dbNum= Integer.parseInt(databases.get(1));
                }

                if (type.equals(KeyValueEnum.KEY)){
                    if(maxInt[0]>dbNum){
                        //TaskMsgException("dbMaping中库号超出Redis库的最大大小 :["+maxInt[0]+"] : "+sourceUri);
                        throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_DB_ERROR_CODE,"dbMaping中库号超出Redis库的最大大小 :["+maxInt[0]+"] : "+sourceUri));
                    }
                }

                if(type.equals(KeyValueEnum.VALUE)){
                    if(maxInt[1]>dbNum){
                        throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_DB_ERROR_CODE,"dbMaping中库号超出Redis库的最大大小 :["+maxInt[1]+"] : "+sourceUri));
                        //TaskMsgException("dbMaping中库号超出Redis库的最大大小 :["+maxInt[1]+"] : "+sourceUri);
                    }
                }

            } catch (Exception e) {
                throw new TaskMsgException("sourceUri is error :"+e.getMessage());
            } finally {
                if (source != null) {
                    source.close();
                }
            }
        }
    }


    /**
     * 获取Map<Integer,Integer> key-value的最大值
     * @param dbMap
     * @return
     */
    public static synchronized  int[] getMapMaxInteger(Map<Integer, Integer> dbMap){
        int[]nums=new int[2];
        nums[0] = dbMap.entrySet().stream().map(e ->e.getKey())
                .max(Comparator.comparing(i -> i))
                .get();
        nums[1] = dbMap.entrySet().stream().map(e ->e.getValue())
                .max(Comparator.comparing(i -> i))
                .get();
        return nums;
    }

    /**
     * 获取buffer值
     */

    public static synchronized String[] selectSyncerBuffer( String targetUri,String type) throws URISyntaxException {

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
            if (target != null) {
                target.close();
            }
        }
        return version;
    }

    private static synchronized String[] getRedisBuffer(String info,String type) {
        String[] version = new String[2];
        version[0]="0";
        try {
            String firstRgex = "repl_backlog_first_byte_offset:(.*?)\r\n";
            String endRrgex = "master_repl_offset:(.*?)\r\n";

            String replid="master_replid:(.*?)\r\n";

            String jimDb_shard_id="shard_id:(.*?)\r\n";

            String runIdRrgex = "run_id:(.*?)\r\n";

            if("endbuf".equalsIgnoreCase(type.trim())){
                if(RegexUtil.getSubUtilSimple(info, endRrgex).length()>0){
                    version[0] = RegexUtil.getSubUtilSimple(info, endRrgex);
                }


            }else {

                if(RegexUtil.getSubUtilSimple(info, endRrgex).length()>0){
                    version[0] = RegexUtil.getSubUtilSimple(info, endRrgex);
                }

                //从头部开始
//                if(RegexUtil.getSubUtilSimple(info, firstRgex).length()>0){
//                    version[0] = RegexUtil.getSubUtilSimple(info, firstRgex);
//                }
            }
            String runid1=RegexUtil.getSubUtilSimple(info, replid);

            if(!StringUtils.isEmpty(runid1.trim())){
                version[1]=runid1;
            }else {
                if(RegexUtil.getSubUtilSimple(info, jimDb_shard_id).length()>0){
                    version[1]=RegexUtil.getSubUtilSimple(info, jimDb_shard_id);
                }else {
                    version[1]=RegexUtil.getSubUtilSimple(info, runIdRrgex);
                }

            }

        } catch (Exception e) {

        } finally {
//            if(jedisClient!=null){
//                jedisClient.close();
//            }

        }
        return version;

    }
}
