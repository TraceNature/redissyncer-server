package syncer.transmission.client.cluster;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.jedis.Jedis;
import syncer.jedis.JedisPool;
import syncer.jedis.Pipeline;
import syncer.jedis.Response;
import syncer.jedis.exceptions.JedisConnectionException;
import syncer.jedis.params.SetParams;
import syncer.jedis.util.JedisClusterCRC16;
import syncer.replica.cmd.CMD;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.impl.ConnectErrorRetry;
import syncer.transmission.client.impl.JedisPipelineSubmitMultiCommandRetryRunner;
import syncer.transmission.cmd.JedisProtocolCommand;
import syncer.transmission.compensator.PipeLineCompensatorEnum;
import syncer.transmission.entity.*;
import syncer.transmission.model.DataCompensationModel;
import syncer.transmission.queue.DbDataCommitQueue;
import syncer.transmission.util.CommandCompensatorUtils;
import syncer.transmission.util.CompensatorUtils;
import syncer.transmission.util.TaskGetUtils;
import syncer.transmission.util.cache.LruCache;
import syncer.transmission.util.object.ObjectUtils;
import syncer.transmission.util.strings.StringUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 单节点客户端
 * 原理与syncer/transmission/client/impl/JedisMultiExecPipeLineClient.java相同
 *
 *
 * {ip}:{port}-runid
 * {ip}:{port}-offset
 * pointcheckVersion
 *
 */

@Slf4j
public class SingleMultiExecRedisClient implements RedisClient {
    protected String targetHost;
    protected Integer targetPort;
    protected String targetPassword;
    protected String sourceHost;
    protected Integer sourcePort;
    protected String sourcePassword;
    protected Jedis targetClient;
    protected Pipeline pipelined;
    /**
     * 当前连接db
     */
    private Integer currentDbNum = 0;

    //任务id
    private String taskId;

    //批次数
    protected Integer count = 1000;
    /**
     * 上一次pipeline提交时间记录
     */
    protected Date date = new Date();

    /**
     * 提交锁
     */
    private Lock commitLock = new ReentrantLock();
    /**
     * 数据补偿锁
     */
    private Lock compensatorLock = new ReentrantLock();
    protected AtomicInteger commandNums = new AtomicInteger();
    //错误次数
    private long errorCount = 1;

    private boolean connectError = false;
    //补偿存储
    protected KVPersistenceDataEntity kvPersistence = new KVPersistenceDataEntity();

    private CompensatorUtils compensatorUtils = new CompensatorUtils();
    //内存非幂等命令转幂等命令
    private Map<String, Integer> incrMap = new LruCache<>(1000);
    private Map<String, StringCompensatorEntity> appendMap = new LruCache<>(1000);
    private Map<String, Float> incrDoubleMap = new LruCache<>(1000);
    private CommandCompensatorUtils commandCompensatorUtils = new CommandCompensatorUtils();
    //断线重试机制
    private ConnectErrorRetry retry=new ConnectErrorRetry(taskId);


    /**
     * 连接集合
     */
    private final Map<Integer, SyncerClusterClient> slots = new HashMap<Integer, SyncerClusterClient>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private final Lock readLock = rwl.readLock();

    private final Lock writeLock = rwl.writeLock();


    /**
     * offset 检查点
     */
    private final static String REDIS_SYNCER_CHECKPOINT="redis-syncer-checkpoint";
    /**
     * checkpoint 版本号
     */
    private final static String CHECKPOINT_VERSION="1";
    /**
     * 提交的最后一个replid
     */
    private String lastReplid;
    /**
     * 提交的最后一个offset
     */
    private long lastOffset;

    /**
     * 前面是否已经执行过muliti
     */
    private AtomicBoolean multiLock=new AtomicBoolean(true);

    private volatile boolean commitMultiLockStatus=true;

    /**
     * 前面有multi
     */
    private AtomicBoolean hasMulti=new AtomicBoolean(false);


    @Override
    public String get(Long dbNum, byte[] key) {

        JedisClusterCRC16.getSlot(key);
        return null;
    }

    @Override
    public String get(Long dbNum, String key) {
        return null;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        return null;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        return null;
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, List<byte[]> value) {
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        return null;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        return null;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        return null;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        return null;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        return null;
    }

    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        return null;
    }

    @Override
    public Object send(byte[] cmd, byte[]... args) {
        return null;
    }

    @Override
    public void select(Integer dbNum) {

    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        return null;
    }

    @Override
    public void updateLastReplidAndOffset(String replid, long offset) {

    }

    @Override
    public void commitCheckPoint() {

    }

    @Override
    public void close() {

    }



    /**
     * 超时自动提交线程
     */
    //死锁
    class PipelineSubmitTask implements Runnable {
        String taskId;
        private boolean status = true;
        private boolean startStatus = true;
        public PipelineSubmitTask(String taskId) {
            this.taskId = taskId;
        }

        @Override
        public void run() {
            Thread.currentThread().setName(taskId + ": " + Thread.currentThread().getName());
            while (true) {
                if(!commitMultiLockStatus){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                compensatorLock.lock();
                try {
                    submitCommandByNum();
                    if (SingleTaskDataManagerUtils.isTaskClose(taskId) && taskId != null) {
                        log.warn("task[{}]数据传输模块进入关闭保护状态,不再接收新数据", taskId);
                        Date time = new Date(date.getTime());
                        if (status) {
                            while (System.currentTimeMillis() - time.getTime() < 1000 * 10) {
                                submitCommandByNum();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Thread.currentThread().interrupt();
                            status = false;
                            submitCommandByNum();
                            log.warn("task[{}]数据传输模保护状态退出,任务停止", taskId);
                            try {
                                targetClient.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }

                }finally {
                    compensatorLock.unlock();
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {

                }
            }

        }
    }


    /**
     * 添加检查点Point
     */
    void addCheckPoint(){
        //checkpoint
        Map<String,String>chekpoint=new HashMap<>();
        String hostName=sourceHost+":"+sourcePort;
        chekpoint.put(hostName+"-offset", String.valueOf(lastOffset));
        chekpoint.put(hostName+"-runid", lastReplid);
        chekpoint.put(hostName+"-version", CHECKPOINT_VERSION);
        pipelined.hset(REDIS_SYNCER_CHECKPOINT,chekpoint);
        kvPersistence.addKey(EventEntity
                .builder()
                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.HSET)
                .cmd("HSET".getBytes())
                .build());
    }

    /**
     * 提交事务
     */
    public void exec(){
        //checkpoint
        addCheckPoint();
        pipelined.exec();
        hasMulti.set(false);
        kvPersistence.addKey(EventEntity
                .builder()
                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.EXEC)
                .cmd("EXEC".getBytes())
                .build());
    }


    /**
     *
     */
    private void submitCommandByNum() {
        if (Objects.nonNull(taskId)&&SingleTaskDataManagerUtils.isTaskClose(taskId)) {
            return;
        }
        commitLock.lock();
        try {
            int num = commandNums.get();
            long time = System.currentTimeMillis() - date.getTime();
            if (num >= count && time > 5000) {
                if(hasMulti.get()) {
                    exec();
                }
                //pipelined.sync();
                List<Object> resultList = pipelined.syncAndReturnAll();
                //补偿入口
                commitCompensator(resultList);
            } else if (num <= 0 && time > 4000) {
                Response<String> r = pipelined.ping();
                kvPersistence.addKey(EventEntity.builder().cmd("PING".getBytes()).pipeLineCompensatorEnum(PipeLineCompensatorEnum.COMMAND).build());
                //pipelined.

                List<Object> resultList = pipelined.syncAndReturnAll();
                //补偿入口
                commitCompensator(resultList);
            } else if (num >= 0 && time > 1000) {
                if(hasMulti.get()) {
                    exec();
                }
                List<Object> resultList = pipelined.syncAndReturnAll();
                //补偿入口
                commitCompensator(resultList);

            }
        } catch (JedisConnectionException e) {
            try {
                retry.retry(new ClusterPipelineSubmitMultiCommandRetryRunner(this));
            }catch (JedisConnectionException ex){
                log.error("[TASKDI {}] pipelined retry fail",taskId);
                brokenTaskByConnectError(ex);
            }

        } finally {
            commitLock.unlock();
        }
    }


    /**
     * key补偿机制入口
     *
     * @param resultList
     */
    void commitCompensator(List<Object> resultList) {
        //更新时间
        updateTaskLastCommitTime(taskId, resultList);
        try {
            if (resultList.size() != kvPersistence.size()) {

                log.error("pipeline返回size[{}]:内存[{}] ", resultList.size(), kvPersistence.size());
                kvPersistence.clear();
                resultList.clear();
                date = new Date();
                commandNums.set(0);
                return;
            }
            KVPersistenceDataEntity newKvPersistence = new KVPersistenceDataEntity();
            for (int i = 0; i < resultList.size(); i++) {
                Object data = resultList.get(i);
                EventEntity eventEntity= kvPersistence.getKey(i);
                byte[] cmd = eventEntity.getCmd();
                String key = eventEntity.getStringKey();

                if (!commandCompensatorUtils.isCommandSuccess(data, cmd, taskId, key)) {
                    log.error("Command[{}],KEY[{}]进入补偿机制：[{}] : RESPONSE[{}]->String[{}]", Strings.byteToString(cmd), eventEntity.getStringKey(), JSON.toJSONString(data), data, compensatorUtils.getRes(data));
                    newKvPersistence.addKey(eventEntity);
                    insertCompensationCommand(eventEntity);
                }
            }

            kvPersistence.clear();
            resultList.clear();
            date = new Date();
            commandNums.set(0);
            newKvPersistence.getKeys().stream().forEach(data -> {
                compensator(data);
            });


            newKvPersistence.clear();
        } finally {

        }
    }


    /**
     * 更新最后pipeline提交时间
     * @param taskIds
     * @param resultList
     */
    void updateTaskLastCommitTime(String taskIds, List<Object> resultList) {
        //记录任务最后一次update时间
        try {
            if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskIds) && getCommandNums(resultList) > 0) {
                TaskDataEntity dataEntity = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskIds);
                dataEntity.getTaskModel().setLastKeyCommitTime(System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("[{}] update last commit time error", taskIds);
        }
    }

    /**
     * 获取非ping-pong命令的数量
     * @param resultList
     * @return
     */
    int getCommandNums(List<Object> resultList) {
        int num = 0;
        for (Object result : resultList) {
            if (null != result && !"PONG".equalsIgnoreCase(compensatorUtils.getRes(result))) {
                num++;
            }
        }
        return num;
    }



    /**
     * multi相关的数据补偿
     * 未做完
     *
     * TODO
     *
     * @param eventEntityList
     */
    void compensator(List<Object> resultList,List<EventEntity> eventEntityList){
        if (Objects.nonNull(taskId)&&SingleTaskDataManagerUtils.isTaskClose(taskId)) {
            return;
        }
        commitLock.lock();
        try {
            Jedis client = null;
            try {
                int currentNum=0;
                int endNum=0;
                boolean status=true;
                for (int i=0;i<resultList.size();i++){
                    EventEntity eventEntity=eventEntityList.get(i);
                    if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.MULTI)){
                        currentNum=i;
                        for (int boyI=currentNum;boyI<resultList.size();boyI++){
                            if(eventEntityList.get(boyI).getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.EXEC)){
                                endNum=boyI;
                                List<Object>resList= (List<Object>) resultList.get(boyI);
                                //
                                for (int cp=currentNum+1,vk=0;cp<boyI&&vk<resList.size();cp++,vk++){
                                    KVPersistenceDataEntity newKvPersistence = new KVPersistenceDataEntity();
                                    for (int is = 0; i < resultList.size(); is++) {
                                        Object data = resultList.get(is);
                                        EventEntity eventEntitys= kvPersistence.getKey(is);
                                        byte[] cmd = eventEntitys.getCmd();
                                        String key = eventEntitys.getStringKey();
                                        if (!commandCompensatorUtils.isCommandSuccess(data, cmd, taskId, key)) {
                                            log.error("Command[{}],KEY[{}]进入补偿机制：[{}] : RESPONSE[{}]->String[{}]", Strings.byteToString(cmd), eventEntitys.getStringKey(), JSON.toJSONString(data), data, compensatorUtils.getRes(data));
                                            newKvPersistence.addKey(eventEntitys);
                                            insertCompensationCommand(eventEntitys);
                                        }
                                    }
                                }
                                status=false;
                                break;
                            }
                        }
                        if(!status){
                            status=true;
                            i=endNum;
                            continue;
                        }
                    }
                }
            }catch (Exception e){

            }
        }finally {
            commitLock.unlock();
        }
    }


    void compensator(EventEntity eventEntity) {
        if (Objects.nonNull(taskId)&&SingleTaskDataManagerUtils.isTaskClose(taskId)) {
            return;
        }
        commitLock.lock();
        try {
            Jedis client = null;
            try {
                client = createJedis(this.targetHost,this.targetPort,targetPassword);
                Object result = null;
                String command = null;
                String key = null;
                if (eventEntity != null && eventEntity.getDbNum() != null) {
                    result = client.select(Math.toIntExact(eventEntity.getDbNum()));
                    command = "SELECT";
                }
                if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SET)) {
                    result = client.set(eventEntity.getKey(), eventEntity.getValue());
                    command = "SET";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SET_WITH_TIME)) {
                    result = client.set(eventEntity.getKey(), eventEntity.getValue(), SetParams.setParams().px(eventEntity.getMs()));
                    command = "SET";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH)) {
                    result = client.lpush(eventEntity.getKey(), eventEntity.getValue());
                    command = "lpush";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_LIST)) {
                    result = client.lpush(eventEntity.getKey(), ObjectUtils.listBytes(eventEntity.getLpush_value()));
                    command = "lpush";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_WITH_TIME)) {
                    result = client.lpush(eventEntity.getKey(), eventEntity.getValue());
                    command = "lpush";
                    client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_WITH_TIME_LIST)) {
                    result = client.lpush(eventEntity.getKey(), ObjectUtils.listBytes(eventEntity.getLpush_value()));
                    command = "lpush";
                    client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RPUSH)) {
                    result = client.rpush(eventEntity.getKey(), eventEntity.getValue());
                    command = "rpush";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RPUSH_LIST)) {
                    result = client.rpush(eventEntity.getKey(), ObjectUtils.listBytes(eventEntity.getLpush_value()));
                    command = "rpush";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RPUSH_WITH_TIME)) {
                    result = client.rpush(eventEntity.getKey(), eventEntity.getValue());
                    command = "rpush";
                    client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RPUSH_WITH_TIME_LIST)) {
                    result = client.rpush(eventEntity.getKey(), ObjectUtils.listBytes(eventEntity.getLpush_value()));
                    command = "rpush";
                    client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.HMSET)) {
                    result = client.hmset(eventEntity.getKey(), eventEntity.getHash_value());
                    command = "hmset";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.HMSET_WITH_TIME)) {
                    result = client.hmset(eventEntity.getKey(), eventEntity.getHash_value());
                    command = "hmset";
                    client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD)) {
                    result = client.sadd(eventEntity.getKey(), eventEntity.getValue());
                    command = "sadd";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_SET)) {
                    result = client.sadd(eventEntity.getKey(), ObjectUtils.setBytes(eventEntity.getMembers()));
                    command = "sadd";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_WITH_TIME)) {
                    result = client.sadd(eventEntity.getKey(), eventEntity.getValue());
                    command = "sadd";
                    client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_WITH_TIME_SET)) {
                    result = client.sadd(eventEntity.getKey(), ObjectUtils.setBytes(eventEntity.getMembers()));
                    command = "sadd";
                    client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.ZADD)) {
                    result = client.zadd(eventEntity.getKey(), ObjectUtils.zsetBytes(eventEntity.getZaddValue()));
                    command = "zadd";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.ZADD_WITH_TIME)) {
                    result = client.zadd(eventEntity.getKey(), ObjectUtils.zsetBytes(eventEntity.getZaddValue()));
                    command = "zadd";
                    client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.PEXPIRE)) {
                    result = client.pexpire(eventEntity.getKey(), eventEntity.getMs());
                    command = "pexpire";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RESTORE)) {
                    result = client.restore(eventEntity.getKey(), eventEntity.getMs(), eventEntity.getValue());
                    command = "restore";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RESTORREPLCE)) {
                    if (eventEntity.isHighVersion()) {
                        result = client.restoreReplace(eventEntity.getKey(), eventEntity.getMs(), eventEntity.getValue());
                        command = "restoreReplace";
                    } else {
                        client.del(eventEntity.getKey());
                        result = client.restore(eventEntity.getKey(), eventEntity.getMs(), eventEntity.getValue());
                        command = "restoreReplace";
                    }
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.COMMAND)) {
                    command = Strings.byteToString(eventEntity.getCmd()).toUpperCase();
                    /** setnx命令处理相关
                     if(isSetNxWithTime(eventEntity.getCmd(),eventEntity.getValueList())){
                     byte[][]data=eventEntity.getValueList();
                     SetParams setParams=getSetParams(data);
                     result=client.set(data[0],data[1],setParams);
                     }else{
                     result=client.sendCommand(JedisProtocolCommand.builder().raw(eventEntity.getCmd()).build(), eventEntity.getValueList());
                     }
                     **/
                    if (eventEntity.getValueList() == null) {
                        result = client.sendCommand(JedisProtocolCommand.builder().raw(eventEntity.getCmd()).build());
                    } else {
                        result = client.sendCommand(JedisProtocolCommand.builder().raw(eventEntity.getCmd()).build(), eventEntity.getValueList());
                    }
                    //非幂等性命令
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.APPEND)) {
                    result = sendIdempotentCommand(appendMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "APPEND";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCR)) {
                    result = sendIdempotentCommand(incrMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "INCR";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBY)) {
                    result = sendIdempotentCommand(incrMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "INCRBY";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBYFLOAT)) {
                    result = sendIdempotentCommand(incrDoubleMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "INCRBYFLOAT";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECR)) {
                    result = sendIdempotentCommand(incrMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "DECR";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECRBY)) {
                    result = sendIdempotentCommand(incrMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "DECRBY";
                }
                key = String.valueOf(eventEntity.getStringKey());
                if (!commandCompensatorUtils.isObjectSuccess(result, command)) {
                    log.error("command [{}]:key [{}]:response[{}]补偿失败，被抛弃", command, key, result);
                }
                /**
                 * todo 抛异常测试
                 */
//                throw new Exception();
            } catch (Exception e) {
                log.warn("key[{}]同步失败被抛弃,原因：[{}]", eventEntity.getStringKey(), e.getMessage());
                if (errorCount >= 0) {
                    long error=SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId).getErrorNums().incrementAndGet();
                    if (error >= errorCount) {
                        brokenTaskByConnectError("被抛弃key数量到达阈值[" + errorCount + "],exception reason["+e.getMessage()+"]");
                    }
                }
                e.printStackTrace();
            } finally {
                if (null != client) {
                    client.close();
                }
            }
        } finally {
            commitLock.unlock();
        }

    }


    /**
     * 创建 jedis
     * 6.0以下  auth password
     * 6.0 auth username password
     * password = username password
     * @param host
     * @param port
     * @param password
     * @return
     */
    protected Jedis createJedis(String host,int port,String password){
        Jedis jedis=new Jedis(host,port);
        if (!StringUtils.isEmpty(password)) {
            jedis.auth(password);
        }
        if(CMD.PONG.equalsIgnoreCase(jedis.ping())){
            return jedis;
        }
        return null;
    }

    /**
     * send 非幂等->幂等命令
     * @param data
     * @param eventEntity
     * @param client
     * @return
     */
    Object sendIdempotentCommand(Object data, EventEntity eventEntity, Jedis client) {
        long pttl = eventEntity.getMs();
        Object result = "OK";
        if (pttl > 0L) {
            result = client.set(eventEntity.getStringKey(), String.valueOf(data), SetParams.setParams().px(pttl));
        } else {
            long targetPttl = client.pttl(eventEntity.getStringKey());
            if (targetPttl > 0) {
                result = client.set(eventEntity.getStringKey(), String.valueOf(data), SetParams.setParams().px(targetPttl));
            } else {
                result = client.set(eventEntity.getStringKey(), String.valueOf(data));
            }
        }
        return result;
    }


    void brokenTaskByConnectError(String msg) {
        if (!connectError) {
            SingleTaskDataManagerUtils.brokenStatusAndLog(msg, this.getClass(), taskId);
            connectError = true;
        }
    }

    void brokenTaskByConnectError(Exception e) {
        if (!connectError) {
            SingleTaskDataManagerUtils.brokenStatusAndLog(e, this.getClass(), taskId);
            connectError = true;
        }
    }

    void insertCompensationCommand(EventEntity data) {
        try {
            String key = data.getStringKey();
            if (StringUtils.isEmpty(key)) {
                key = "";
            }
            DataCompensationModel dataCompensationModel = DataCompensationModel
                    .builder()
                    .command(Strings.byteToString(data.getCmd()))
                    .groupId(TaskGetUtils.getRunningTaskGroupId(taskId))
                    .key(key)
                    .taskId(taskId)
                    .times(1)
                    .value("")
                    .build();
            DbDataCommitQueue.put(SqliteCommitEntity.builder().type(20).object(dataCompensationModel).msg("补偿数据写入").build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
