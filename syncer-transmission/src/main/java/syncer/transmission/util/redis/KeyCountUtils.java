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
import syncer.common.util.RegexUtil;
import syncer.jedis.Jedis;
import syncer.replica.config.RedisURI;
import syncer.replica.config.ReplicConfig;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.List;
import java.util.Objects;

import static syncer.replica.cmd.CMD.PONG;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
@Slf4j
public class KeyCountUtils {
    public synchronized static void  updateKeyCount(String taskId, RedisURI suri){
        Jedis target = null;
        try {
            ReplicConfig tconfig = ReplicConfig.valueOf(suri);

            target = new Jedis(suri.getHost(), suri.getPort());
            //获取password
            if (!StringUtils.isEmpty(tconfig.getAuthPassword())) {
                Object auth = target.auth( tconfig.getAuthPassword());
            }

            //重试三次
            int i = 3;
            while (i > 0) {
                try {
                    String png =target.ping();

                    if (PONG.equalsIgnoreCase(png)) {
                        break;
                    }
                    i--;
                } catch (Exception e) {
                    throw  e;
                }
            }

            String infoRes =target.info("Keyspace");
            String rgex = "db(.*?):keys=(.*?),";
            List<List<String>>res= RegexUtil.getSubListUtil(infoRes,rgex,2);

            Long keyCount=res.stream().mapToLong(data->{
                return Long.parseLong(data.get(1));
            }).sum();

            if(Objects.nonNull(keyCount)){
                SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getRdbKeyCount().set(keyCount);

                SqlOPUtils.updateKeyCountById(taskId,keyCount,0L,0L);
            }

        }catch (Exception e){
            e.printStackTrace();
            log.warn("任务[{}]获取全量key数量失败",taskId);
        }finally {
            if (null != target) {
                target.close();
            }
        }

    }

}