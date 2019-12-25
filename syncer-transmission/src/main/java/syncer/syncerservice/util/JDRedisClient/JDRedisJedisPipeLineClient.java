package syncer.syncerservice.util.JDRedisClient;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.SetParams;
import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.entity.EventEntity;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.util.jedis.JDJedis;
import syncer.syncerservice.util.jedis.ObjectUtils;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerservice.util.jedis.cmd.JedisProtocolCommand;
import syncer.syncerservice.util.jedis.pool.JDJedisPool;
import syncer.syncerservice.util.taskutil.TaskMsgStatusUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 单机redis pipeleine版本
 */
@Slf4j
public class JDRedisJedisPipeLineClient implements JDRedisClient {

    private String host;
    private Integer port;
    private JDJedisPool jedisPool;
    private JedisPoolConfig config;
    private Pipeline pipelined;
    private Integer currentDbNum=0;
    //批次数
    private Integer count = 1000;
    //上一次pipeline提交时间记录
    private Date date = new Date();
    private JDRedisJedisPipeLineClient.SyncTaskEntity syncTaskEntity = new JDRedisJedisPipeLineClient.SyncTaskEntity();
    //任务id
    private String taskId;
    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private Lock commitLock=new ReentrantLock();
    private AtomicInteger commandNums=new AtomicInteger();
    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }


    public JDRedisJedisPipeLineClient(String host, Integer port, String password, int count,String taskId) {

        this.host = host;
        this.port = port;
        this.taskId = taskId;
        if (count != 0) {
            this.count = count;
        }

        if (null == config) {
            config = new JedisPoolConfig();
            config.setMaxTotal(5);
            config.setMaxIdle(3);
            config.setMinIdle(2);
            //当池内没有返回对象时，最大等待时间
            config.setMaxWaitMillis(10000);
            config.setTimeBetweenEvictionRunsMillis(30000);
            config.setTestOnReturn(true);
            config.setBlockWhenExhausted(true);
            config.setTestOnBorrow(true);
        }
        int timeout = 50000;
        if (org.springframework.util.StringUtils.isEmpty(password)) {
            jedisPool = new JDJedisPool(this.config, this.host, this.port, timeout);
        } else{
            jedisPool = new JDJedisPool(this.config, this.host, this.port, timeout, password, 0, null);

        }

        JDJedis jdJedis = jedisPool.getResource();
        pipelined = jdJedis.pipelined();

        //定时回收线程
        threadPoolTaskExecutor.execute(new JDRedisJedisPipeLineClient.PipelineSubmitThread(taskId));


    }


    @Override
    public String get(final Long dbNum,byte[] key) {
        JDJedis jdJedis=null;
        String stringKey=StringUtils.toString(key);
        try {
            jdJedis = jedisPool.getResource();
            if(!jdJedis.getDbNum().equals(dbNum)){
                jdJedis.select(dbNum.intValue());
            }
            return jdJedis.get(stringKey);
        }catch (Exception e){
            log.warn("[{}]get key[{}]失败",taskId,stringKey);
        }finally {
            jdJedis.close();
        }
        return null;
    }

    @Override
    public String get(final Long dbNum,String key) {
        JDJedis jdJedis=null;
        try {
            if(!jdJedis.getDbNum().equals(dbNum)){
                jdJedis.select(dbNum.intValue());
            }
            jdJedis = jedisPool.getResource();
            return jdJedis.get(key);
        }catch (Exception e){
            log.warn("[{}]get key[{}]失败",taskId,key);
        }finally {
            jdJedis.close();
        }
        return null;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        selectDb(dbNum);
        pipelined.set(key, value);
        addCommandNum();

        return null;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        selectDb(dbNum);
        pipelined.set(key, value, SetParams.setParams().px(ms));
        addCommandNum();
        return null;
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {

        selectDb(dbNum);

        pipelined.append(key, value);
        addCommandNum();
        return null;
    }


    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        selectDb(dbNum);
        pipelined.lpush(key, value);
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        selectDb(dbNum);
        pipelined.lpush(key, value);
        pipelined.pexpire(key, ms);
        addCommandNum();
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        selectDb(dbNum);
        pipelined.lpush(key, ObjectUtils.listBytes(value));
        addCommandNum();
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        selectDb(dbNum);
        pipelined.lpush(key, ObjectUtils.listBytes(value));
        pipelined.pexpire(key, ms);
        addCommandNum();
        return null;
    }


    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {

        selectDb(dbNum);
        pipelined.sadd(key, members);
        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        selectDb(dbNum);

        pipelined.sadd(key, members);
        pipelined.pexpire(key, ms);
        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        selectDb(dbNum);
        pipelined.sadd(key, ObjectUtils.setBytes(members));
        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        selectDb(dbNum);
        pipelined.sadd(key, ObjectUtils.setBytes(members));
        pipelined.pexpire(key, ms);
        addCommandNum();
        return null;
    }


    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {

        selectDb(dbNum);
        pipelined.zadd(key, ObjectUtils.zsetBytes(value));
        addCommandNum();
        return null;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {

        selectDb(dbNum);
        pipelined.zadd(key, ObjectUtils.zsetBytes(value));
        pipelined.pexpire(key, ms);
        addCommandNum();
        return null;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        selectDb(dbNum);
        pipelined.hmset(key, hash);
        addCommandNum();
        return null;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        selectDb(dbNum);
        pipelined.hmset(key, hash);
        pipelined.pexpire(key, ms);
        addCommandNum();
        return null;
    }


    @Override
    public String restore(Long dbNum, byte[] key, int ttl, byte[] serializedValue) {
        selectDb(dbNum);
        pipelined.restore(key, ttl, serializedValue);
        addCommandNum();
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, int ttl, byte[] serializedValue) {
        selectDb(dbNum);
        pipelined.restoreReplace(key, ttl, serializedValue);
        addCommandNum();

        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, int ttl, byte[] serializedValue, boolean highVersion) {
        selectDb(dbNum);

        if (highVersion) {
            pipelined.restoreReplace(key, ttl, serializedValue);
        } else {
            pipelined.del(key);
            pipelined.restore(key, ttl, serializedValue);
        }
        addCommandNum();
        return null;
    }


    @Override
    public Object send(byte[] cmd, byte[]... args) {
        pipelined.sendCommand(JedisProtocolCommand.builder().raw(cmd).build(), args);
        addCommandNum();
        return null;
    }

    @Override
    public void select(Integer dbNum) {
        pipelined.select(dbNum);
        addCommandNum();
    }

    @Override
    public Long pexpire(Long dbNum,byte[] key, long ms) {
        selectDb(dbNum);
        pipelined.pexpire(key, ms);
        addCommandNum();
        return null;
    }

     void selectDb(Long dbNum){
        if(dbNum!=null&&!currentDbNum.equals(dbNum.intValue())){
            currentDbNum=dbNum.intValue();
            pipelined.select(dbNum.intValue());
        }
    }

      void addCommandNum() {
        commitLock.lock();
        try {
            int num=commandNums.incrementAndGet();
            if (num>= count) {
//                System.out.println("提交："+num);
                List<Object> resultList = pipelined.syncAndReturnAll();
                syncTaskEntity.clear();
                date = new Date();
                commandNums.set(0);
            }
        }finally {
            commitLock.unlock();
        }


//        syncTaskEntity.add();
//
//        if (syncTaskEntity.syncNums >= count) {
//            System.out.println("提交："+syncTaskEntity.syncNums);
//                  List<Object> resultList = pipelined.syncAndReturnAll();
//                  syncTaskEntity.clear();
//                  date = new Date();
//
//            //补偿机制
////            List<EventEntity>eventEntities=new ArrayList<>();
////            eventEntities.clear();
////            eventEntities.addAll(taskEntity.getKeys());
////            taskEntity.getKeys().clear();
////            threadPoolTaskExecutor.execute(new  PipelineCompensator(new ArrayList<>(resultList),eventEntities,suri,turi,pipelineLock.getTaskId()));
////            PipelineCompensator.singleCompensator(resultList,eventEntities,suri,turi,pipelineLock.getTaskId());
////            resultList.clear();
//
//        }


    }


     void submitCommandNum() {
         commitLock.lock();

         try {
             int num=commandNums.get();
             long time = System.currentTimeMillis() - date.getTime();
             if (num >= count && time > 5000) {
                 //pipelined.sync();


                 List<Object> resultList = pipelined.syncAndReturnAll();
                 resultList.clear();
                 syncTaskEntity.clear();
                 // log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());

                 date = new Date();
                 commandNums.set(0);



             } else if (num <= 0 && time > 4000) {
                 Response<String> r = pipelined.ping();
                 pipelined.sync();
                 syncTaskEntity.clear();
                 log.info("[{}]PING->{}",taskId, r.get());
                 date = new Date();
                 commandNums.set(0);


             }else if(num>0 && time > 3000){
//                 System.out.println("提交："+commandNums.get());
                 List<Object> resultList = pipelined.syncAndReturnAll();
                 syncTaskEntity.clear();
                 date = new Date();

                 commandNums.set(0);
             }
         }finally {
             commitLock.unlock();
         }



//            long time = System.currentTimeMillis() - date.getTime();
//            if (syncTaskEntity.syncNums >= count && time > 5000) {
//                //pipelined.sync();
//
//
//                    List<Object> resultList = pipelined.syncAndReturnAll();
//                    resultList.clear();
//                    syncTaskEntity.clear();
//                    // log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());
//
//                date = new Date();
//
//
//
//
//            } else if (syncTaskEntity.syncNums == 0 && time > 4000) {
//                Response<String> r = pipelined.ping();
//                pipelined.sync();
//                syncTaskEntity.clear();
//                log.info("[{}]PING->{}",taskId, r.get());
//                date = new Date();
//
//
//
//            }else if(syncTaskEntity.syncNums>0 && time > 3000){
//                System.out.println("提交："+syncTaskEntity.syncNums);
//                List<Object> resultList = pipelined.syncAndReturnAll();
//                syncTaskEntity.clear();
//                date = new Date();
//            }

    }


    /**
     * 超时自动提交线程
     */
    class PipelineSubmitThread implements Runnable {
        String taskId;
        private boolean status = true;
        private boolean startStatus = true;
        public PipelineSubmitThread(String taskId) {
            this.taskId = taskId;
        }

        @Override
        public void run() {
            while (true){
                submitCommandNum();
                if (TaskMsgStatusUtils.doThreadisCloseCheckTask(taskId)&&taskId!=null) {
                    Date time =new Date(date.getTime());
                    if (status) {
                        while (System.currentTimeMillis()-time.getTime()<1000*60*2){
                            submitCommandNum();
                        }

                        Thread.currentThread().interrupt();
                        status = false;
                        addCommandNum();
                        System.out.println("【" + taskId + "】 PipelinedSyncTask关闭...." + Thread.currentThread().getName());
                        pipelined.close();
                        break;
                    }
                }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {

                    }
            }
//            try {
//                if(startStatus){
//                    startStatus=false;
//                    Thread.sleep(3000);
//                }
//                while (true) {
//                    addCommandNum();
//                    if (TaskMsgStatusUtils.doThreadisCloseCheckTask(taskId)&&taskId!=null) {
//                        if (status) {
//                            Thread.currentThread().interrupt();
//                            status = false;
//                            addCommandNum();
//                            System.out.println("【" + taskId + "】 PipelinedSyncTask关闭...." + Thread.currentThread().getName());
//                            pipelined.close();
//                            break;
//                        }
//                    }
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//
//                    }
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }


    class SyncTaskEntity {

        private volatile int syncNums = 0;
        private Lock lock = new ReentrantLock();
        private boolean userStatus = true;
        private volatile List<EventEntity> keys = new ArrayList<>();

        public synchronized void addKey(EventEntity key) {
            lock.lock();
            try {
                keys.add(key);
            } finally {
                lock.unlock();
            }

        }

        public List<EventEntity> getKeys() {
            lock.lock();
            try {
                return keys;
            } finally {
                lock.unlock();
            }

        }

        public synchronized int getSyncNums() {
            return syncNums;
        }

        public boolean isUserStatus() {
            return userStatus;
        }

        public synchronized void inUserStatus() {
            this.userStatus = userStatus;
        }

        public synchronized void offUserStatus() {
            this.userStatus = userStatus;
        }

        public synchronized void add() {
            lock.lock();
            try {
                this.syncNums++;
            } finally {
                lock.unlock();
            }

        }


        public synchronized void add(int num) {
            lock.lock();
            try {
                this.syncNums += num;
            } finally {
                lock.unlock();
            }
        }

        public synchronized void clear() {
            lock.lock();
            try {
                this.syncNums = 0;
            } finally {
                lock.unlock();
            }

        }
    }

}


