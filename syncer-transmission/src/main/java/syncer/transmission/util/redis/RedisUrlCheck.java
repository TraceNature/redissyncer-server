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

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.common.exception.TaskMsgException;
import syncer.common.exception.TaskRestoreException;
import syncer.jedis.Jedis;
import syncer.jedis.exceptions.JedisDataException;
import syncer.replica.cmd.CMD;
import syncer.replica.config.RedisURI;
import syncer.replica.config.ReplicConfig;
import syncer.transmission.constants.TaskMsgConstant;
import syncer.transmission.util.code.CodeUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author zhanenqiang
 * @Description redis
 * @Date 2020/12/14
 */
@Slf4j
public class RedisUrlCheck {
    public  void  checkRedisUrl(String url,String clientName)throws TaskMsgException {
        try {
            if (!checkRedisUrl(url)) {
                //TaskMsgException("scheme must be [redis].");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_URI_ERROR_CODE,TaskMsgConstant.TASK_MSG_REDIS_URI_ERROR));
            }
            if (!checkClientConnectState(url, clientName)) {
                // TaskMsgException(name + " :连接redis失败");
                throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,TaskMsgConstant.TASK_MSG_TARGET_REDIS_CONNECT_ERROR));
            }
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }
    }

    public  boolean checkRedisUrl(String uri) throws URISyntaxException {
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
        Jedis target = null;
        try {
            turi = new RedisURI(url);
            target = new Jedis(turi.getHost(), turi.getPort());
            ReplicConfig tconfig = ReplicConfig.valueOf(turi);
            //获取password
            if (!StringUtils.isEmpty(tconfig.getAuthPassword())) {
                log.info("CheckRedisConnectStatus is success");
                String auth = target.auth(tconfig.getAuthPassword());
            }

            //重试三次
            int i = 3;
            while (i > 0) {
                try {
                    String png = target.ping();
                    if (CMD.PONG.equalsIgnoreCase(png)) {
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
            StringBuilder stringBuilder=new StringBuilder("[").append(clientName).append("]").append("节点连接失败 原因: [").append(e.getMessage()).append(" ]");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,stringBuilder.toString()));
        } catch (Exception e) {
            StringBuilder stringBuilder=new StringBuilder("[").append(clientName).append("]").append("节点连接失败 原因: [").append(e.getMessage()).append(" ]");
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_REDIS_ERROR_CODE,stringBuilder.toString()));
        } finally {
            if (null != target) {
                target.close();
            }
        }

    }

}
