package syncer.syncerservice.filter.redis_start_check_strategy;
import org.springframework.util.StringUtils;
import syncer.syncerjedis.exceptions.JedisDataException;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.entity.*;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.exception.TaskRestoreException;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.pool.RedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static syncer.syncerjedis.Protocol.Command.AUTH;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/3
 */
public class RedisStartCheckRedisUrlStrategy implements IRedisStartCheckBaseStrategy {
    private IRedisStartCheckBaseStrategy next;
    private JDRedisClient client;
    private RedisStartCheckEntity eventEntity;
    private RedisPoolProps redisPoolProps;

    @Override
    public void run(JDRedisClient client, RedisStartCheckEntity eventEntity, RedisPoolProps redisPoolProps) throws Exception {

        //初始化数据
        RedisStartCheckTypeEnum type=eventEntity.getStartCheckType();

        RedisClusterDto clusterDto=eventEntity.getClusterDto();

        if(type.equals(RedisStartCheckTypeEnum.SINGLE_REDIS_TO_SINGLE_REDIS)
                ||type.equals(RedisStartCheckTypeEnum.SINGLE_REDIS_TO_CLUSTER)
                ||type.equals(RedisStartCheckTypeEnum.SINGLE_REDIS_TO_FILE)){

            Set<String> sourceRedisUris = clusterDto.getSourceUris();

            for (String sourceUri : sourceRedisUris) {
                checkRedisUrl(sourceUri, "sourceUri:" + sourceUri);
            }

        }else if(type.equals(RedisStartCheckTypeEnum.FILE_TO_CLUSTER)
                ||type.equals(RedisStartCheckTypeEnum.FILE_TO_SINGLE_REDIS)){
            clusterDto.setSourceUris(clusterDto.getFileUris());
        }


        if(!type.equals(RedisStartCheckTypeEnum.SINGLE_REDIS_TO_FILE)){
            Set<String> targetRedisUris = clusterDto.getTargetUris();
            for (String targetUri : targetRedisUris) {
                checkRedisUrl(targetUri, "targetUri : " + targetUri);
            }
        }


        /**
         * 存在dbMap时检查是否超出数据库大小
         */
//            try {
//                RedisUrlCheckUtils.doCheckDbNum(sourceRedisUris, clusterDto.getDbMapper(), KeyValueEnum.KEY);
//            } catch (URISyntaxException e) {
//                throw new TaskMsgException(e.getMessage());
//          }


        //下一节点
        toNext(client,eventEntity,redisPoolProps);


    }

    @Override
    public void toNext(JDRedisClient client, RedisStartCheckEntity eventEntity, RedisPoolProps redisPoolProps) throws Exception {
        if(null!=next) {
            next.run(client,eventEntity,redisPoolProps);
        }
    }

    @Override
    public void setNext(IRedisStartCheckBaseStrategy nextStrategy) {
        this.next=nextStrategy;
    }



    /**
     * 检查reids是否能够连接
     *
     * @param url
     * @param name
     * @throws TaskMsgException
     */
    public  void  checkRedisUrl(String url, String name) throws TaskMsgException {
        try {
            if (!checkRedisUrl(url)) {
                //TaskMsgException("scheme must be [redis].");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_URI_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_URI_ERROR));
            }
            if (!checkClientConnectState(url, name)) {
                // TaskMsgException(name + " :连接redis失败");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,TaskMsgConstant.TASK_MSG_TARGET_REDIS_CONNECT_ERROR));
            }
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }
    }



    public static boolean checkRedisUrl(String uri) throws URISyntaxException {
        URI uriplus = new URI(uri);
        if (uriplus.getScheme() != null && "redis".equalsIgnoreCase(uriplus.getScheme())) {
            return true;
        }
        return false;
    }


    /**
     * 检查Redis连接状态
     * @param url
     * @param clientName
     * @return
     * @throws TaskMsgException
     */

    public boolean checkClientConnectState(String url,String clientName)throws TaskMsgException{
        RedisURI turi = null;
        RedisClient target = null;
        try {
            turi = new RedisURI(url);
            target = new RedisClient(turi.getHost(), turi.getPort());
            Configuration tconfig = Configuration.valueOf(turi);
            //获取password
            if (!StringUtils.isEmpty(tconfig.getAuthPassword())) {
                System.out.println("CheckRedisConnectStatus is success");
                Object auth = target.send(AUTH, tconfig.getAuthPassword().getBytes());
            }

            //重试三次
            int i = 3;
            while (i > 0) {
                try {
                    String png = (String) target.send("PING".getBytes());
                    System.out.println(url.split("\\?")[0]+"------:"+png);
                    if ("PONG".equalsIgnoreCase(png)) {
                        System.out.println("CheckRedisConnectStatus is success");
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
        }catch (URISyntaxException e) {
            //TaskMsgException(name + ":连接链接不正确");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,clientName + ":连接链接不正确"));
        } catch (JedisDataException e) {
            //TaskMsgException(name + ":" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,clientName + ":" + e.getMessage()));
        } catch (TaskRestoreException e) {
            //TaskMsgException(name + ":error:" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,clientName + ":" + e.getMessage()));
        } catch (Exception e) {
            // TaskMsgException(name + ":error:" + e.getMessage());
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,clientName + ":" + e.getMessage()));
        } finally {
            if (null != target) {
                target.close();
            }
        }

    }


}
