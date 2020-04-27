package syncer.syncerservice.util.JDRedisClient;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import syncer.syncerjedis.*;
import syncer.syncerjedis.params.SetParams;
import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.PipeLineCompensatorEnum;
import syncer.syncerplusredis.entity.EventEntity;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.constant.CmdEnum;
import syncer.syncerservice.po.KVPersistenceDataEntity;
import syncer.syncerservice.po.StringCompensatorEntity;
import syncer.syncerservice.util.CompensatorUtils;
import syncer.syncerservice.util.EliminationAlgorithm.lru.LruCache;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.jedis.ObjectUtils;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerservice.util.jedis.cmd.JedisProtocolCommand;
import syncer.syncerservice.util.taskutil.TaskMsgStatusUtils;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;


/**
 * 单机redis pipeleine版本
 */
@Slf4j
public class JDRedisJedisPipeLineClient implements JDRedisClient {

    private String host;
    private Integer port;
    private JedisPool jedisPool;
    private JedisPoolConfig config;
    private Pipeline pipelined;
    private Integer currentDbNum=0;
    //批次数
    private Integer count = 1000;
    //上一次pipeline提交时间记录
    private Date date = new Date();
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

    //补偿存储
    private KVPersistenceDataEntity kvPersistence=new KVPersistenceDataEntity();
    private CompensatorUtils compensatorUtils=new CompensatorUtils();
    //内存非幂等命令转幂等命令
    private Map<String,Integer>incrMap= new LruCache<>(1000);
    private Map<String, StringCompensatorEntity>appendMap=new LruCache<>(1000);
    private Map<String,Float>incrDoubleMap= new LruCache<>(1000);


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
            jedisPool = new JedisPool(this.config, this.host, this.port, timeout);
        } else{
            jedisPool = new JedisPool(this.config, this.host, this.port, timeout, password, 0, null);

        }

        Jedis jdJedis = jedisPool.getResource();
        pipelined = jdJedis.pipelined();

        //定时回收线程
        threadPoolTaskExecutor.execute(new JDRedisJedisPipeLineClient.PipelineSubmitThread(taskId));


    }


    @Override
    public String get(final Long dbNum,byte[] key) {
        Jedis jdJedis=null;
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
        Jedis jdJedis=null;
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
        kvPersistence.addKey(EventEntity
                .builder()
                .key(key)
                .value(value)
                .stringKey(Strings.byteToString(key))
                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SET)
                .dbNum(dbNum)
                .build());
        pipelined.set(key, value);
        addCommandNum();

        return null;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        selectDb(dbNum);

        pipelined.set(key, value, SetParams.setParams().px(ms));
        kvPersistence.addKey(EventEntity
                .builder()
                .key(key)
                .value(value)
                .stringKey(Strings.byteToString(key))
                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SET_WITH_TIME)
                .dbNum(dbNum)
                .ms(ms)
                .build());
        addCommandNum();
        return null;
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {

        selectDb(dbNum);

        commitLock.lock();
        try{

        }finally {
            commitLock.unlock();
        }
        pipelined.append(key, value);
        EventEntity entity=EventEntity
                .builder()
                .key(key)
                .value(value)
                .stringKey(Strings.byteToString(key))
                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.APPEND)
                .dbNum(dbNum)
                .build();
        kvPersistence.addKey(entity);
        compensatorMap(entity);
        addCommandNum();
        return null;
    }


    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        selectDb(dbNum);
        commitLock.lock();

        try{
            pipelined.lpush(key, value);

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH)
                    .dbNum(dbNum)
                    .build());
        }finally {
            commitLock.unlock();
        }

        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.lpush(key, value);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH_WITH_TIME)
                    .dbNum(dbNum)
                    .ms(ms)
                    .build());

            pexpire(dbNum,key,ms);
        }finally {
            commitLock.unlock();
        }


        addCommandNum();
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        selectDb(dbNum);
        commitLock.lock();

        try {
            pipelined.lpush(key, ObjectUtils.listBytes(value));

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .lpush_value(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH_LIST)
                    .dbNum(dbNum)
                    .build());
        }finally {
            commitLock.unlock();
        }



        addCommandNum();
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        selectDb(dbNum);

        commitLock.lock();
        try{
            pipelined.lpush(key, ObjectUtils.listBytes(value));
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .lpush_value(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH_LIST)
                    .dbNum(dbNum)
                    .ms(ms)
                    .build());

            pexpire(dbNum,key,ms);
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        selectDb(dbNum);
        commitLock.lock();

        try{
            pipelined.rpush(key, value);

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH)
                    .dbNum(dbNum)
                    .build());
        }finally {
            commitLock.unlock();
        }

        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.rpush(key, value);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH_WITH_TIME)
                    .dbNum(dbNum)
                    .ms(ms)
                    .build());

            pexpire(dbNum,key,ms);
        }finally {
            commitLock.unlock();
        }


        addCommandNum();
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, List<byte[]> value) {
        selectDb(dbNum);
        commitLock.lock();

        try {
            pipelined.rpush(key, ObjectUtils.listBytes(value));

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .lpush_value(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH_LIST)
                    .dbNum(dbNum)
                    .build());
        }finally {
            commitLock.unlock();
        }



        addCommandNum();
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        selectDb(dbNum);

        commitLock.lock();
        try{
            pipelined.rpush(key, ObjectUtils.listBytes(value));
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .lpush_value(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH_LIST)
                    .dbNum(dbNum)
                    .ms(ms)
                    .build());

            pexpire(dbNum,key,ms);
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }


    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {

        selectDb(dbNum);

        commitLock.lock();
        try{
            pipelined.sadd(key, members);

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(members)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SADD)
                    .dbNum(dbNum)
                    .build());
        }finally {
            commitLock.unlock();
        }


        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        selectDb(dbNum);

        commitLock.lock();
        try{
            pipelined.sadd(key, members);

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(members)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SADD_WITH_TIME)
                    .dbNum(dbNum)
                    .ms(ms)
                    .build());
            pexpire(dbNum,key,ms);
        }finally {
            commitLock.unlock();
        }


        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        selectDb(dbNum);

        commitLock.lock();
        try {
            pipelined.sadd(key, ObjectUtils.setBytes(members));

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .members(members)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SADD_SET)
                    .dbNum(dbNum)
                    .build());
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        selectDb(dbNum);

        commitLock.lock();
        try{
            pipelined.sadd(key, ObjectUtils.setBytes(members));
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .members(members)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SADD_WITH_TIME_SET)
                    .dbNum(dbNum)
                    .ms(ms)
                    .build());
            pexpire(dbNum,key,ms);
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }


    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {

        selectDb(dbNum);

        commitLock.lock();
        try{
            pipelined.zadd(key, ObjectUtils.zsetBytes(value));

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .zaddValue(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.ZADD)
                    .dbNum(dbNum)
                    .build());
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {

        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.zadd(key, ObjectUtils.zsetBytes(value));

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .zaddValue(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.ZADD)
                    .dbNum(dbNum)
                    .ms(ms)
                    .build());
            pexpire(dbNum,key,ms);
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        selectDb(dbNum);

        commitLock.lock();
        try {
            pipelined.hmset(key, hash);

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .hash_value(hash)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.HMSET)
                    .dbNum(dbNum)
                    .build());
        }finally {
            commitLock.unlock();
        }


        addCommandNum();
        return null;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        selectDb(dbNum);

        commitLock.lock();
        try{
            pipelined.hmset(key, hash);

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .hash_value(hash)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.HMSET)
                    .dbNum(dbNum)
                    .ms(ms)
                    .build());
            pexpire(dbNum,key,ms);
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }


    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        selectDb(dbNum);

        commitLock.lock();
        try {
            pipelined.restore(key, ttl, serializedValue);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .value(serializedValue)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RESTORE)
                    .dbNum(dbNum)
                    .ms(ttl)
                    .build());
        }finally {
            commitLock.unlock();
        }


        addCommandNum();
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        selectDb(dbNum);

        commitLock.lock();
        try {
            pipelined.restoreReplace(key, ttl, serializedValue);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .value(serializedValue)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RESTORREPLCE)
                    .dbNum(dbNum)
                    .ms(ttl)
                    .build());
        }finally {
            commitLock.unlock();
        }


        addCommandNum();

        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        selectDb(dbNum);

        commitLock.lock();

        try{
            if (highVersion) {
                pipelined.restoreReplace(key, ttl, serializedValue);
                kvPersistence.addKey(EventEntity
                        .builder()
                        .key(key)
                        .value(serializedValue)
                        .stringKey(Strings.byteToString(key))
                        .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RESTORREPLCE)
                        .dbNum(dbNum)
                        .ms(ttl)
                        .highVersion(highVersion)
                        .build());
            } else {
                pipelined.del(key);
                pipelined.restore(key, ttl, serializedValue);
                kvPersistence.addKey(EventEntity
                        .builder()
                        .key(key)
                        .stringKey(Strings.byteToString(key))
                        .pipeLineCompensatorEnum(PipeLineCompensatorEnum.DEL)
                        .dbNum(dbNum)
                        .ms(ttl)
                        .highVersion(highVersion)
                        .build());

                kvPersistence.addKey(EventEntity
                        .builder()
                        .key(key)
                        .value(serializedValue)
                        .stringKey(Strings.byteToString(key))
                        .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RESTORREPLCE)
                        .dbNum(dbNum)
                        .ms(ttl)
                        .highVersion(highVersion)
                        .build());

            }
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }


    @Override
    public Object send(byte[] cmd, byte[]... args) {


        commitLock.lock();

        try {
            pipelined.sendCommand(JedisProtocolCommand.builder().raw(cmd).build(), args);



            if(args==null||args.length==0){
                kvPersistence.addKey(EventEntity
                        .builder()
                        .cmd(cmd)
                        .valueList(args)
                        .pipeLineCompensatorEnum(PipeLineCompensatorEnum.COMMAND)
                        .build());
            }else {

                //判断幂等非幂等命令
                if(compensatorUtils.isIdempotentCommand(cmd)){
                    EventEntity entity=EventEntity
                            .builder()
                            .key(args[0])
                            .cmd(cmd)
                            .valueList(args)
                            .stringKey(Strings.byteToString(args[0]))
                            .pipeLineCompensatorEnum(compensatorUtils.getIdempotentCommand(cmd))
                            .build();
                    kvPersistence.addKey(entity);
                    compensatorMap(entity);

                }else {
                    kvPersistence.addKey(EventEntity
                            .builder()
                            .key(args[0])
                            .cmd(cmd)
                            .valueList(args)
                            .stringKey(Strings.byteToString(args[0]))
                            .pipeLineCompensatorEnum(PipeLineCompensatorEnum.COMMAND)
                            .build());



                }

            }

        }finally {
            commitLock.unlock();
        }


        addCommandNum();
        return null;
    }




    @Override
    public void select(Integer dbNum) {
        commitLock.lock();
        try {
            pipelined.select(dbNum);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .dbNum(Long.valueOf(dbNum))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SELECT)
                    .build());
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
    }

    @Override
    public Long pexpire(Long dbNum,byte[] key, long ms) {

        selectDb(dbNum);
        commitLock.lock();
        try{
            pipelined.pexpire(key, ms);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .dbNum(Long.valueOf(dbNum))
                    .ms(ms)
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.PEXPIRE)
                    .build());
        }finally {
            commitLock.unlock();
        }

        addCommandNum();
        return null;
    }

    void selectDb(Long dbNum){

        commitLock.lock();

        try{
            if(dbNum!=null&&!currentDbNum.equals(dbNum.intValue())){
                currentDbNum=dbNum.intValue();
                pipelined.select(dbNum.intValue());
                kvPersistence.addKey(EventEntity
                        .builder()
                        .dbNum(Long.valueOf(dbNum))
                        .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SELECT)
                        .build());
                addCommandNum();

            }
        }finally {
            commitLock.unlock();
        }

    }

      void addCommandNum() {
        commitLock.lock();
        try {
            int num=commandNums.incrementAndGet();
            if (num>= count) {
//                System.out.println("提交："+num);
                List<Object> resultList = pipelined.syncAndReturnAll();
//                System.out.println(resultList.size()+":内存： "+kvPersistence.size());

//                Stream.iterate(0, i -> i + 1).limit(resultList.size()).forEach(index -> {
//                    Object data = resultList.get(index);
//                    if(!compensatorUtils.isObjectSuccess(data)){
//
//                        compensator(kvPersistence.getKey(index));
//
//                    }
//                });

                resultList.clear();
                kvPersistence.clear();
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
//        }


    }


    void compensatorMap(EventEntity eventEntity){
        Jedis client=null;

        try {

          if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.COMMAND)){
                return;
                //非幂等性命令
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.APPEND)){
              if(appendMap.containsKey(eventEntity.getStringKey())){
                  appendMap.get(eventEntity.getStringKey()).getValue().append(Strings.byteToString(eventEntity.getValue()));
              }else {
                  client=jedisPool.getResource();

                 String oldValue= client.get(eventEntity.getStringKey());
                 StringBuilder stringBuilder=new StringBuilder();
                 if(org.springframework.util.StringUtils.isEmpty(oldValue)){
                     stringBuilder.append(Strings.byteToString(eventEntity.getValue()));
                 }else {
                     stringBuilder.append(oldValue);
                     stringBuilder.append(Strings.byteToString(eventEntity.getValue()));
                 }

                  appendMap.put(eventEntity.getStringKey(),StringCompensatorEntity
                          .builder()
                          .stringKey(eventEntity.getStringKey())
                  .value(stringBuilder)
                  .key(eventEntity.getKey())
                  .dbNum(eventEntity.getDbNum())
                  .build());

              }

            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCR)){




              if(incrMap.containsKey(eventEntity.getStringKey())){
                incrMap.put(eventEntity.getStringKey(),incrMap.get(eventEntity.getStringKey())+1);
              }else{
                  client=jedisPool.getResource();
                  String oldValue= client.get(eventEntity.getStringKey());
                  Integer newValue=0;
                  if(org.springframework.util.StringUtils.isEmpty(oldValue)){
                      newValue++;
                  }else {
                      newValue= Integer.valueOf(oldValue+1);
                  }
                  incrMap.put(eventEntity.getStringKey(),newValue);
              }




            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBY)){

              if(incrMap.containsKey(eventEntity.getStringKey())){
                  incrMap.put(eventEntity.getStringKey(), incrMap.get(eventEntity.getStringKey())+ Integer.valueOf(Strings.byteToString(eventEntity.getValueList()[1])));
              }else{
                  client=jedisPool.getResource();
                  String oldValue= client.get(eventEntity.getStringKey());
                  Integer newValue=0;
                  if(org.springframework.util.StringUtils.isEmpty(oldValue)){
                      newValue++;
                  }else {
                      newValue= Integer.valueOf(oldValue+ Strings.byteToString(eventEntity.getValueList()[1]));
                  }
                  incrMap.put(eventEntity.getStringKey(),newValue);
              }


            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBYFLOAT)){

              if(incrDoubleMap.containsKey(eventEntity.getStringKey())){
                  incrDoubleMap.put(eventEntity.getStringKey(),incrDoubleMap.get(eventEntity.getStringKey())+Float.valueOf(Strings.byteToString(eventEntity.getValueList()[1])));
              }else{
                  client=jedisPool.getResource();
                  String oldValue= client.get(eventEntity.getStringKey());
                  float newValue=0;
                  if(org.springframework.util.StringUtils.isEmpty(oldValue)){
                      newValue-=newValue+Float.valueOf(Strings.byteToString(eventEntity.getValueList()[1]));
                  }else {
                      newValue= Float.valueOf(oldValue)+ Float.valueOf(Strings.byteToString(eventEntity.getValueList()[1]));
                  }
                  incrDoubleMap.put(eventEntity.getStringKey(),newValue);
              }

            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECR)){

              if(incrMap.containsKey(eventEntity.getStringKey())){
                  incrMap.put(eventEntity.getStringKey(),incrMap.get(eventEntity.getStringKey())-1);
              }else{
                  client=jedisPool.getResource();
                  String oldValue= client.get(eventEntity.getStringKey());
                  Integer newValue=0;
                  if(org.springframework.util.StringUtils.isEmpty(oldValue)){
                      newValue--;
                  }else {
                      newValue= Integer.valueOf(oldValue)-1;
                  }
                  incrMap.put(eventEntity.getStringKey(),newValue);
              }

            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECRBY)){

              if(incrMap.containsKey(eventEntity.getStringKey())){
                  incrMap.put(eventEntity.getStringKey(),incrMap.get(eventEntity.getStringKey())-Integer.valueOf(Strings.byteToString(eventEntity.getValueList()[1])));
              }else{
                  client=jedisPool.getResource();
                  String oldValue= client.get(eventEntity.getStringKey());
                  Integer newValue=0;
                  if(org.springframework.util.StringUtils.isEmpty(oldValue)){
                      newValue-=newValue-Integer.valueOf(Strings.byteToString(eventEntity.getValueList()[1]));
                  }else {
                      newValue= Integer.valueOf(oldValue)- Integer.valueOf(Strings.byteToString(eventEntity.getValueList()[1]));
                  }
                  incrMap.put(eventEntity.getStringKey(),newValue);
              }
            }



        }catch (Exception e){
            log.warn("key[{}]幂等命令计算,原因：[{}]",eventEntity.getStringKey(),e.getMessage());
//            log.warn("key[{}]同步失败被抛弃",eventEntity.getStringKey());

        }finally {
            if(null!=client){
                client.close();
            }
        }

    }

    void compensator(EventEntity eventEntity){
        Jedis client=null;

        try {
            client=jedisPool.getResource();
            if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SET)){
                client.set(eventEntity.getKey(),eventEntity.getValue());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SET_WITH_TIME)){
                client.set(eventEntity.getKey(),eventEntity.getValue(),SetParams.setParams().px(eventEntity.getMs()));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH)){
                client.lpush(eventEntity.getKey(),eventEntity.getValue());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_LIST)){
                client.lpush(eventEntity.getKey(),ObjectUtils.listBytes(eventEntity.getLpush_value()));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_WITH_TIME)){
                client.lpush(eventEntity.getKey(),eventEntity.getValue());
                client.pexpire(eventEntity.getKey(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_WITH_TIME_LIST)){
                client.lpush(eventEntity.getKey(),ObjectUtils.listBytes(eventEntity.getLpush_value()));
                client.pexpire(eventEntity.getKey(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.HMSET)){
                client.hmset(eventEntity.getKey(),eventEntity.getHash_value());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.HMSET_WITH_TIME)){
                client.hmset(eventEntity.getKey(),eventEntity.getHash_value());
                client.pexpire(eventEntity.getKey(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD)){
                client.sadd(eventEntity.getKey(),eventEntity.getValue());

            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_SET)){
                client.sadd(eventEntity.getKey(),ObjectUtils.setBytes(eventEntity.getMembers()));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_WITH_TIME)){
                client.sadd(eventEntity.getKey(),eventEntity.getValue());
                client.pexpire(eventEntity.getKey(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_WITH_TIME_SET)){
                client.sadd(eventEntity.getKey(),ObjectUtils.setBytes(eventEntity.getMembers()));
                client.pexpire(eventEntity.getKey(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.ZADD)){
                client.zadd(eventEntity.getKey(),ObjectUtils.zsetBytes(eventEntity.getZaddValue()));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.ZADD_WITH_TIME)){
                client.zadd(eventEntity.getKey(),ObjectUtils.zsetBytes(eventEntity.getZaddValue()));
                client.pexpire(eventEntity.getKey(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.PEXPIRE)){
                client.pexpire(eventEntity.getKey(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RESTORE)){
                client.restore(eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValue());

            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RESTORREPLCE)){
                if(eventEntity.isHighVersion()){
                    client.restoreReplace(eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValue());
                }else {
                    client.del(eventEntity.getKey());
                    client.restore(eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValue());
                }

            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.COMMAND)){
                client.sendCommand(JedisProtocolCommand.builder().raw(eventEntity.getCmd()).build(), eventEntity.getValueList());

                //非幂等性命令
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.APPEND)){

                client.set(eventEntity.getStringKey(),appendMap.get(eventEntity.getStringKey()).getValue().toString());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCR)){
                client.set(eventEntity.getKey(),ObjectUtils.toBytes(incrMap.get(eventEntity.getStringKey())));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBY)){
                client.set(eventEntity.getKey(),ObjectUtils.toBytes(incrMap.get(eventEntity.getStringKey())));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBYFLOAT)){
                client.set(eventEntity.getKey(),ObjectUtils.toBytes(incrDoubleMap.get(eventEntity.getStringKey())));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECR)){
                client.set(eventEntity.getKey(),ObjectUtils.toBytes(incrMap.get(eventEntity.getStringKey())));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECRBY)){
                client.set(eventEntity.getKey(),ObjectUtils.toBytes(incrMap.get(eventEntity.getStringKey())));

            }



        }catch (Exception e){
            log.warn("key[{}]同步失败被抛弃,原因：[{}]",eventEntity.getStringKey(),e.getMessage());

        }finally {
            if(null!=client){
                client.close();
            }

        }

    }


     void submitCommandNum() {
         commitLock.lock();

         try {
             int num=commandNums.get();
             long time = System.currentTimeMillis() - date.getTime();
             if (num >= count && time > 5000) {
                 //pipelined.sync();


                 List<Object> resultList = pipelined.syncAndReturnAll();


//                 System.out.println(resultList.size()+":内存： "+kvPersistence.size());


//                 Stream.iterate(0, i -> i + 1).limit(resultList.size()).forEach(index -> {
//                     Object data = resultList.get(index);
//                     if(!compensatorUtils.isObjectSuccess(data)){
//
//                         compensator(kvPersistence.getKey(index));
//                         System.out.println("补偿机制："+ JSON.toJSONString(data));
//
//
//
//                     }
//                 });


                 resultList.clear();
                 kvPersistence.clear();
                 // log.info("将管道中超过 {} 个值提交",taskEntity.getSyncNums());

                 date = new Date();
                 commandNums.set(0);

                 time = System.currentTimeMillis() - date.getTime();

             } else if (num <= 0 && time >4000) {
                 Response<String> r = pipelined.ping();
                 kvPersistence.addKey(EventEntity.builder().cmd("PING".getBytes()).pipeLineCompensatorEnum(PipeLineCompensatorEnum.COMMAND).build());
//                 pipelined.
                 List<Object> resultList = pipelined.syncAndReturnAll();

//                 Stream.iterate(0, i -> i + 1).limit(resultList.size()).forEach(index -> {
//                     Object data = resultList.get(index);
//                     if(!compensatorUtils.isObjectSuccess(data)){
//
//                         System.out.println("补偿机制："+ JSON.toJSONString(data));
//                         compensator(kvPersistence.getKey(index));
//                         System.out.println(compensatorUtils.getRes(data));
//
//
//                     }
//                 });

                 kvPersistence.clear();
//                 log.info("[{}]PING->{}",taskId, r.get());
                 date = new Date();
                 commandNums.set(0);
                 time = System.currentTimeMillis() - date.getTime();

             }else if(num>=0 && time > 3000){

                 List<Object> resultList = pipelined.syncAndReturnAll();

//
//                 Stream.iterate(0, i -> i + 1).limit(resultList.size()).forEach(index -> {
//                     Object data = resultList.get(index);
//                     if(!compensatorUtils.isObjectSuccess(data)){
//                         System.out.println("补偿机制："+data);
//                         kvPersistence.getKey(index);
//                         if(kvPersistence.getKey(index))
//                         compensator(kvPersistence.getKey(index));
//                         System.out.println(compensatorUtils.getRes(data));
//
//
//                     }
//                 });

                 kvPersistence.clear();
                 date = new Date();
                 time = System.currentTimeMillis() - date.getTime();
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


}


