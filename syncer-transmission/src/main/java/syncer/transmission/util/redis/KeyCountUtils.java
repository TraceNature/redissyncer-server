package syncer.transmission.util.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.common.util.RegexUtil;
import syncer.jedis.Jedis;
import syncer.replica.entity.Configuration;
import syncer.replica.entity.RedisURI;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.net.URISyntaxException;
import java.util.List;

import static syncer.replica.constant.CMD.PONG;

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
            Configuration tconfig = Configuration.valueOf(suri);

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

            SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getRdbKeyCount().set(keyCount);

            SqlOPUtils.updateKeyCountById(taskId,keyCount,0L,0L);
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
        updateKeyCount("",new RedisURI("redis://114.67.100.239:6379?authPassword=redistest0102"));
    }
}