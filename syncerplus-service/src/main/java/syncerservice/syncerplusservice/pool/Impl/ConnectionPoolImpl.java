package syncerservice.syncerplusservice.pool.Impl;

import syncerservice.syncerplusredis.entity.Configuration;
import syncerservice.syncerplusredis.entity.RedisURI;
import syncerservice.syncerplusservice.pool.ConnectionPool;
import syncerservice.syncerplusservice.pool.RedisClient;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static redis.clients.jedis.Protocol.Command.AUTH;


/**
 * Redis连接池
 */
@Slf4j
public class ConnectionPoolImpl implements ConnectionPool {
    //是否关闭
    AtomicBoolean isClosed = new AtomicBoolean(false);

    //队列实现连接 对象存储
    volatile LinkedBlockingQueue<RedisClient> idle; //空闲队列

    volatile LinkedBlockingQueue<RedisClient> busy; //繁忙队列

    //大小控制连接数量
    AtomicInteger activeSize = new AtomicInteger(0);

    //记录连接被创建的次数
    AtomicInteger createCounter = new AtomicInteger(0);

    @Getter@Setter
    RedisURI redisURI;//redis链接
    int minActive;
    int maxActive;
    long maxWait;
    long timeBetweenEvictionRunsMillis;
    long idleTimeRunsMillis;
    boolean status=true;


    /**
     * 初始化线程池
     * @param minActive 最小连接数
     * @param maxActive 最大连接数
     * @param maxWait   超时时间
     * @param redisURI  uri
     * @param timeBetweenEvictionRunsMillis
     * @param idleTimeRunsMillis
     */
    @Override
    public void init(int minActive, int maxActive, long maxWait, RedisURI redisURI, long timeBetweenEvictionRunsMillis,long idleTimeRunsMillis) {
        this.minActive = minActive;
        this.maxActive = maxActive;
        this.maxWait = maxWait;
        this.idleTimeRunsMillis=idleTimeRunsMillis;
        idle = new LinkedBlockingQueue<RedisClient>();
        busy = new LinkedBlockingQueue<RedisClient>();
        this.redisURI=redisURI;
        this.timeBetweenEvictionRunsMillis=timeBetweenEvictionRunsMillis;
        freeResourceMonitor();
    }

    public RedisClient borrowResource() throws Exception {
        RedisClient redisClient = null;
        long now = System.currentTimeMillis();//获取连接的开始时间
        while(null == redisClient){
            //从空闲队列中获取一个
            redisClient = idle.poll();
            if(null != redisClient){
                redisClient.setLastTime(new Date());
                //如果空闲队列里有连接,直接是被复用，再将此连接移动到busy （繁忙）队列中
                busy.offer(redisClient);
//                System.out.println("从空闲队列里拿到连接");
                return redisClient;
            }


            //如果空闲队列里没有连接,就判断是否超出连接池大小，不超出就创建一个
            if(activeSize.get() < maxActive){//多线程并发
                //先加再判断
                if(activeSize.incrementAndGet() <= maxActive){
                    //创建jedis连接
                    redisClient = new RedisClient(redisURI.getHost(), redisURI.getPort());
                    Configuration tconfig = Configuration.valueOf(redisURI);

                    //获取password
                    if (!StringUtils.isEmpty(tconfig.getAuthPassword())) {
                        Object auth = redisClient.send(AUTH, tconfig.getAuthPassword().getBytes());
                        log.info("AUTH:" + auth);
                    }

                    createCounter.incrementAndGet();

//                    System.out.println("连接被创建的次数：" +createCounter.incrementAndGet());
                    //存入busy队列
                    busy.offer(redisClient);
                    return redisClient;
                }else{
                    //加完后超出大小再减回来
                    activeSize.decrementAndGet();
                }

            }

            //如果前面2个都不能拿到连接，那就在我们设置的最大等待超时时间内，等待别人释放连接
            try {
                //等待别人释放得到连接，同时也有最长的等待时间限制
                redisClient = idle.poll(maxWait - (System.currentTimeMillis() - now), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new Exception("等待异常 ... ");
            }

            if(null == redisClient ) {
                //判断是否超时
                if( (System.currentTimeMillis() - now) >= maxWait ){
                    throw new Exception("timeout ... ");
                }else{
                    continue;
                }
            }else{
                //存入busy队列
                redisClient.setLastTime(new Date());
                busy.offer(redisClient);
            }

        }

        while (!redisClient.isConnected()){
            log.info("获取redisClient不可用重新获取");
            closeRedisClient(redisClient);
            redisClient=borrowResource();
        }
        return redisClient;
    }

    public void release(RedisClient client) throws Exception {
        if(null == client ) {
            log.info("释放的redisClient为空");
            return;
        }
        if(busy.remove(client)){
            idle.offer(client);
        }else{
            //如果释放不成功,则减去一个连接，在创建的时候可以自动补充
            activeSize.decrementAndGet();
            throw new Exception("释放redisClient异常");
        }
    }

    public void close() {

        if(isClosed.compareAndSet(false, true)){
            LinkedBlockingQueue<RedisClient> pool = idle;
            while(pool.isEmpty()){
                RedisClient jedis = pool.poll();
                jedis.close();
                if(pool == idle && pool.isEmpty() ){
                    pool = busy;
                }
            }
        }
    }

    void closeRedisClient(RedisClient redisClient){
        busy.remove(redisClient);
        redisClient.close();
        activeSize.decrementAndGet();

    }


    /**
     * 空闲资源释放监控
     */
    void freeResourceMonitor(){
        if(status){
            new Thread(new Runnable() {
                @Override
                public void run() {

                    if(isClosed.get()){
                        Thread.currentThread().isInterrupted();
                    }
                    /**
                     * 判断线程池是否关闭
                     */
                    while (!isClosed.get()){

                        int count=0;
                        /**
                         * 每次遍历空闲列表
                         */

                        if(activeSize.get()>minActive+1){
                            while (count<idle.size()){
                                count++;
                                //从空闲队列中获取一个
                                RedisClient redisClient= idle.poll();
                                if(null != redisClient){
                                    Date nowTime=new Date();

                                    //超时
                                    if((nowTime.getTime()-redisClient.getLastTime().getTime())>timeBetweenEvictionRunsMillis){
                                        redisClient.close();
                                        activeSize.decrementAndGet();
                                    }else if(!redisClient.isConnected()){
                                        //连接已断开
                                        redisClient.close();
                                        activeSize.decrementAndGet();
                                    } else {
                                        redisClient.ping();
                                        idle.offer(redisClient);
                                    }
                                }
                            }
                        }
                        try {
                            Thread.sleep(timeBetweenEvictionRunsMillis);
                        } catch (InterruptedException e) {
                            log.info("----------InterruptedException ");
                        }
                    }
                }
            }).start();
        }else {
            this.status=false;
        }

    }


}
