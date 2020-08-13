package syncer.syncerservice.filter.redis_start_check_strategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.syncerjedis.exceptions.JedisDataException;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.constant.TaskMsgConstant;
import syncer.syncerplusredis.entity.*;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.exception.TaskRestoreException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.pool.RedisClient;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static syncer.syncerjedis.Protocol.Command.AUTH;

/**
 * @author zhanenqiang
 * @Description url心跳检查策略
 * @Date 2020/3/3
 */
@AllArgsConstructor
@Builder
@Slf4j
public class RedisStartCheckRedisUrlStrategy implements IRedisStartCheckBaseStrategy {
    private IRedisStartCheckBaseStrategy next;
    private JDRedisClient client;
    private TaskModel taskModel;
    private RedisPoolProps redisPoolProps;


    @Override
    public void run(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) throws Exception {

        //两边都需要进行检测
        if(taskModel.getSyncType().equals(SyncType.SYNC.getCode())){
            checkRedisUrl(taskModel.getSourceUri(), "sourceUri:" + taskModel.getSourceHost());
            for (String targetUri : taskModel.getTargetUri()) {
                checkRedisUrl(targetUri, "targetUri:" + taskModel.getTargetHost());
            }
            //只校验目标
        }else if(taskModel.getSyncType().equals(SyncType.RDB.getCode())
                ||taskModel.getSyncType().equals(SyncType.AOF.getCode())
                ||taskModel.getSyncType().equals(SyncType.MIXED.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINERDB.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINEAOF.getCode())
                ||taskModel.getSyncType().equals(SyncType.ONLINEMIXED.getCode())){
            for (String targetUri : taskModel.getTargetUri()) {
                checkRedisUrl(targetUri, "sourcehost"+taskModel.getSourceHost()+"targetUri:" + taskModel.getTargetHost());
            }
        }else if(taskModel.getSyncType().equals(SyncType.COMMANDDUMPUP.getCode())){
            //只校验源
            checkRedisUrl(taskModel.getSourceUri(), "sourceUri:" + taskModel.getSourceHost());
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
        toNext(client,taskModel,redisPoolProps);


    }

    @Override
    public void toNext(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) throws Exception {
        if(null!=next) {
            next.run(client,taskModel,redisPoolProps);
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
                log.info("CheckRedisConnectStatus is success");
                Object auth = target.send(AUTH, tconfig.getAuthPassword().getBytes());
            }

            //重试三次
            int i = 3;
            while (i > 0) {
                try {
                    String png = (String) target.send("PING".getBytes());
                    if ("PONG".equalsIgnoreCase(png)) {
                        log.info("CheckRedisConnectStatus is success");
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
            StringBuilder stringBuilder=new StringBuilder("[").append(clientName).append("]").append("节点连接失败 原因: [").append(e.getMessage()).append(" ]");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,stringBuilder.toString()));
        } catch (TaskRestoreException e) {
            //TaskMsgException(name + ":error:" + e.getMessage());
            StringBuilder stringBuilder=new StringBuilder("[").append(clientName).append("]").append("节点连接失败 原因: [").append(e.getMessage()).append(" ]");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,stringBuilder.toString()));
        } catch (Exception e) {
            // TaskMsgException(name + ":error:" + e.getMessage());
            StringBuilder stringBuilder=new StringBuilder("[").append(clientName).append("]").append("节点连接失败 原因: [").append(e.getMessage()).append(" ]");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,stringBuilder.toString()));
        } finally {
            if (null != target) {
                target.close();
            }
        }

    }


}
