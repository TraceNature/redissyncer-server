package com.i1314i.syncerplusservice.pool.factory;

import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusservice.pool.RedisClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.springframework.util.StringUtils;

import static redis.clients.jedis.Protocol.Command.AUTH;

@Slf4j
public class CommonPoolConnectionFactory extends BasePoolableObjectFactory {
    @Getter
    @Setter
    RedisURI redisURI;//redis链接
    int minActive;
    int maxActive;
    long maxWait;
    long timeBetweenEvictionRunsMillis;
    long idleTimeRunsMillis;

    public CommonPoolConnectionFactory(int minActive, int maxActive, long maxWait, RedisURI redisURI, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis) {
        this.redisURI = redisURI;
        this.minActive = minActive;
        this.maxActive = maxActive;
        this.maxWait = maxWait;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.idleTimeRunsMillis = idleTimeRunsMillis;
    }

    /**
     * 创建一个新对象;当对象池中的对象个数不足时,将会使用此方法来"输出"一个新的"对象",并交付给对象池管理.
     * @return
     * @throws Exception
     */
    @Override
    public Object makeObject() throws Exception {
        RedisClient  redisClient = new RedisClient(redisURI.getHost(), redisURI.getPort());
        Configuration tconfig = Configuration.valueOf(redisURI);

        //获取password
        if (!StringUtils.isEmpty(tconfig.getAuthPassword())) {
            Object auth = redisClient.send(AUTH, tconfig.getAuthPassword().getBytes());
            log.info("AUTH:" + auth);
        }

        return redisClient;
    }

    /**
     *  销毁对象,如果对象池中检测到某个"对象"idle的时间超时,或者操作者向对象池"归还对象"时检测到"对象"已经无效,
     *  那么此时将会导致"对象销毁";"销毁对象"的操作设计相差甚远,但是必须明确:当调用此方法时,
     *  "对象"的生命周期必须结束.如果object是线程,那么此时线程必须退出;
     *  如果object是socket操作,那么此时socket必须关闭;如果object是文件流操作,
     *  那么此时"数据flush"且正常关闭.
     * @param obj
     * @throws Exception
     */
    public void destroyObject(Object obj) throws Exception  {
        if(obj instanceof RedisClient){
            ((RedisClient)obj).close();
        }
    }


    /**
     *  检测对象是否"有效";Pool中不能保存无效的"对象",因此"后台检测线程"会周期性的检测Pool中"对象"的有效性,
     *  如果对象无效则会导致此对象从Pool中移除,并destroy;
     *  此外在调用者从Pool获取一个"对象"时,也会检测"对象"的有效性,
     *  确保不能讲"无效"的对象输出给调用者;当调用者使用完毕将"对象归还"到Pool时,
     *  仍然会检测对象的有效性.所谓有效性,就是此"对象"的状态是否符合预期,
     *  是否可以对调用者直接使用;如果对象是Socket,
     *  那么它的有效性就是socket的通道是否畅通/阻塞是否超时等.
     * @param obj
     * @return
     */
    public boolean validateObject(Object obj) {
        if(obj instanceof RedisClient){
            RedisClient redisClient = ((RedisClient)obj);
            if(!redisClient.isConnected()){
                return false;
            }

            return true;
        }
        return false;
    }


}
