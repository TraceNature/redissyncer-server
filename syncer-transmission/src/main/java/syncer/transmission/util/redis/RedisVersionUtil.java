// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.util.redis;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.common.exception.TaskMsgException;
import syncer.common.util.TemplateUtils;
import syncer.common.util.file.FileUtils;
import syncer.jedis.Jedis;
import syncer.jedis.exceptions.JedisDataException;
import syncer.replica.config.RedisURI;
import syncer.replica.config.ReplicConfig;
import syncer.transmission.constants.TaskMsgConstant;
import syncer.transmission.util.code.CodeUtils;
import syncer.transmission.util.regex.RegexUtils;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/14
 */
@Slf4j
public class RedisVersionUtil {

    /**
     * jimdb版本头
     */
    private final static String JIMDB_VERSION_HEADER="jimdb";

    /**
     * jimdb版本头
     */
    private final static String JIMDB_VERSION_TWO_HEADER="jimdb_";

    /**
     *
     */
    private final static String DEFAULT_NO_VERSION="0.0";

    /**
     * jimdb版本头
     */
    private final static int JIMDB_VERSION_LEN=3;

    //rdb版本和redis版本映射关系
    static Map<String,Integer> rdbVersion=null;

    static final Object lock=new Object();
    /**
     * 获取redis版本号
     * info信息中若无版本号信息则返回0L
     *
     * @param targetUri
     * @return
     * @throws URISyntaxException
     */
    public String selectSyncerVersion(String targetUri) throws URISyntaxException, TaskMsgException {
        RedisURI targetUriplus = new RedisURI(targetUri);
        /**
         * 源目标
         */
        Jedis target = null;
        String targetVersion = null;
        try {
            target = new Jedis(targetUriplus.getHost(), targetUriplus.getPort());
            ReplicConfig targetConfig = ReplicConfig.valueOf(targetUriplus);

            //获取password
            if(!StringUtils.isEmpty(targetConfig.getAuthUser())){
                Object targetAuth = target.auth(targetConfig.getAuthUser(),targetConfig.getAuthPassword());
            }else if (!StringUtils.isEmpty(targetConfig.getAuthPassword())) {
                Object targetAuth = target.auth(targetConfig.getAuthPassword());
            }
            String info = target.info();

            targetVersion = getRedisVersionString(info);

        } catch (Exception e) {
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE, e.getMessage()));

        } finally {
            if (target != null) {
                target.close();
            }
        }
        return targetVersion;
    }

    public String selectSyncerVersion(String host,Integer port,String password) throws URISyntaxException, TaskMsgException {
        /**
         * 源目标
         */
        Jedis target = null;
        String targetVersion = null;
        try {
            target = new Jedis(host, port);

            //获取password
            if (!StringUtils.isEmpty(password)) {
                Object targetAuth = target.auth(password);
            }
            String info = target.info();

            targetVersion = getRedisVersionString(info);

        } catch (Exception e) {
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE, e.getMessage()));

        } finally {
            if (target != null) {
                target.close();
            }
        }
        return targetVersion;
    }

    public String selectSyncerVersionAboutSentinel(String targetUri,String masterName) throws URISyntaxException, TaskMsgException {
        RedisURI targetUriplus = new RedisURI(targetUri);
        /**
         * 源目标
         */
        Jedis target = null;
        Jedis client=null;
        String targetVersion = null;
        try {
            target = new Jedis(targetUriplus.getHost(), targetUriplus.getPort());
            ReplicConfig targetConfig = ReplicConfig.valueOf(targetUriplus);

            //获取password
            if (!StringUtils.isEmpty(targetConfig.getAuthPassword())) {
                try {
                    Object targetAuth = target.auth(targetConfig.getAuthPassword());
                }catch (JedisDataException e){
                    target = new Jedis(targetUriplus.getHost(), targetUriplus.getPort());
                    if(!e.getMessage().contains("ERR AUTH <password> called without any password configured for the default user. Are you sure your configuration is correct?")){
                        throw e;
                    }
                }

            }

            List<String> result=target.sentinelGetMasterAddrByName(masterName);
            client=new Jedis(result.get(0), Integer.parseInt(result.get(1)));
            if (!StringUtils.isEmpty(targetConfig.getAuthPassword())) {
                Object targetAuth = client.auth(targetConfig.getAuthPassword());
            }

            String info = client.info();
            targetVersion = getRedisVersionString(info);

        } catch (Exception e) {
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE, e.getMessage()));

        } finally {
            if (target != null) {
                target.close();
            }
            if (client != null) {
                client.close();
            }
        }
        return targetVersion;
    }

    public String getRedisVersionString(String info) {
        String version = null;
        try {
            String rgex = "redis_version:(.*?)\r\n";
            String version_res = RegexUtils.getSubUtilSimple(info, rgex);
            if (version_res.length() >= JIMDB_VERSION_LEN) {
                if (version_res.trim().toLowerCase().contains(JIMDB_VERSION_HEADER)) {
                    version =JIMDB_VERSION_TWO_HEADER + RegexUtils.getSubUtilSimple(info, rgex).substring(6, 9);
                } else {
                    version = RegexUtils.getSubUtilSimple(info, rgex).substring(0, 3);
                }
            } else {
                version = DEFAULT_NO_VERSION;
            }

        } catch (Exception e) {
            log.warn("getRedisVersion.", e);
        }
        return version;
    }


    /**
     * 根据uri获取RDB版本
     * @param uri
     * @param userRedisVersion
     * @return
     */
    public  Integer getRdbVersionByRedisVersion(String uri,String userRedisVersion){
        String redisVersion = null;
        try {
            redisVersion = selectSyncerVersion(uri);
        } catch (Exception e) {
            redisVersion="0";
        }
        Integer userRdbVersion = getRdbVersion(userRedisVersion);
        Integer rdbVersion = getRdbVersion(redisVersion);
        log.warn("版本号获取：{}",uri.split("\\?")[0]);
        log.warn("自动获取redis版本号：{} ,对应rdb版本号：{},手动输入版本号：{}，对应rdb版本号：{}",redisVersion,rdbVersion,userRdbVersion,rdbVersion);
        if (rdbVersion == 0) {
            if (userRdbVersion == 0) {
                return  -1;
            } else {
                return  userRdbVersion;
            }
        } else {
            return rdbVersion;
        }
    }


    /**
     * 从配置文件中获取rdb版本号
     * @param redisVersion
     * @return
     */
    public static synchronized  Integer getRdbVersion(String redisVersion){

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
        if(Objects.nonNull(rdb)){

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
}
