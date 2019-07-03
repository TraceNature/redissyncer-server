package com.i1314i.syncerplusservice.pool.Impl;

import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.moilioncircle.redis.replicator.Configuration;
import com.moilioncircle.redis.replicator.RedisURI;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static redis.clients.jedis.Protocol.Command.AUTH;

public class ConnectionPoolImpl implements ConnectionPool {
    //是否关闭
    AtomicBoolean isClosed = new AtomicBoolean(false);

    //队列实现连接 对象存储
    LinkedBlockingQueue<RedisClient> idle; //空闲队列

    LinkedBlockingQueue<RedisClient> busy; //繁忙队列

    //大小控制连接数量
    AtomicInteger activeSize = new AtomicInteger(0);

    //记录连接被创建的次数
    AtomicInteger createCounter = new AtomicInteger(0);

    @Getter@Setter
    RedisURI redisURI;//redis链接
    int maxActive;
    long maxWait;


    /**
     * 初始化连接池
     * @param maxActive
     * @param maxWait
     */
    public void init(int maxActive, long maxWait) {
        this.maxActive = maxActive;
        this.maxWait = maxWait;
        idle = new LinkedBlockingQueue<RedisClient>();
        busy = new LinkedBlockingQueue<RedisClient>();
    }



    /**
     * 初始化连接池
     * @param maxActive 最大连接数
     * @param maxWait  最大等待数
     * @param redisURI  redisUrl
     */
    @Override
    public void init(int maxActive, long maxWait, RedisURI redisURI) {
        this.maxActive = maxActive;
        this.maxWait = maxWait;
        idle = new LinkedBlockingQueue<RedisClient>();
        busy = new LinkedBlockingQueue<RedisClient>();
        this.redisURI=redisURI;
    }


    public RedisClient borrowResource() throws Exception {
        RedisClient redisClient = null;
        long now = System.currentTimeMillis();//获取连接的开始时间
        while(null == redisClient){
            //从空闲队列中获取一个
            redisClient = idle.poll();

            if(null != redisClient){
                //如果空闲队列里有连接,直接是被复用，再将此连接移动到busy （繁忙）队列中
                busy.offer(redisClient);
                System.out.println("从空闲队列里拿到连接");
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
                    if (tconfig.getAuthPassword() != null) {
                        Object auth = redisClient.send(AUTH, tconfig.getAuthPassword().getBytes());
                         System.out.println("AUTH:" + auth);
                    }

                    System.out.println("连接被创建的次数：" +createCounter.incrementAndGet());
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
                busy.offer(redisClient);
            }

        }
        return redisClient;
    }

    public void release(RedisClient jedis) throws Exception {
        if(null == jedis ) {
            System.out.println("释放 的jedis为空");
            return;
        }
        if(busy.remove(jedis)){
            idle.offer(jedis);
        }else{
            //如果释放不成功,则减去一个连接，在创建的时候可以自动补充
            activeSize.decrementAndGet();
            throw new Exception("释放jedis异常");
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


}
