package syncer.syncerservice.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerservice.pool.RedisClient;
import syncer.syncerservice.util.regex.RegexUtil;

import java.net.URISyntaxException;
import java.util.List;

import static syncer.syncerjedis.Protocol.Command.AUTH;
/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/2
 */
@Slf4j
public class KeyCountUtils {
    public synchronized static void  updateKeyCount(String taskId,RedisURI suri){
        RedisClient target = null;
        try {
            Configuration tconfig = Configuration.valueOf(suri);

            target = new RedisClient(suri.getHost(), suri.getPort());
            //获取password
            if (!StringUtils.isEmpty(tconfig.getAuthPassword())) {
                Object auth = target.send(AUTH, tconfig.getAuthPassword().getBytes());
            }

            //重试三次
            int i = 3;
            while (i > 0) {
                try {
                    String png = (String) target.send("PING".getBytes());

                    if ("PONG".equalsIgnoreCase(png)) {
                        break;
                    }
                    i--;
                } catch (Exception e) {
                    throw  e;
                }
            }

            String infoRes = (String) target.send("INFO".getBytes(),"Keyspace".getBytes());
            String rgex = "db(.*?):keys=(.*?),";
            List<List<String>>res= RegexUtil.getSubListUtil(infoRes,rgex,2);

            Long keyCount=res.stream().mapToLong(data->{
                return Long.parseLong(data.get(1));
            }).sum();


            TaskDataManagerUtils.get(taskId).getRdbKeyCount().set(keyCount);
            TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
//            taskMapper.updateRdbKeyCountById(taskId,keyCount);
            taskMapper.updateKeyCountById(taskId,keyCount,0L,0L);
        }catch (Exception e){
            e.printStackTrace();
            log.warn("任务[{}]获取全量key数量失败",taskId);
        }finally {
            if (null != target) {
                target.close();
            }
        }

    }

    public static void main(String[] args) throws URISyntaxException {
        //redis://45.40.203.109:20001?authPassword=redistest0102
        updateKeyCount("",new RedisURI("redis://45.40.203.109:20001"));
    }
}
