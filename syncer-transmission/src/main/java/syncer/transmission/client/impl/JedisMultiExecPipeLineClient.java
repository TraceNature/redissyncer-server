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

package syncer.transmission.client.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.common.util.ThreadPoolUtils;
import syncer.jedis.Jedis;
import syncer.jedis.Pipeline;
import syncer.jedis.Response;
import syncer.jedis.exceptions.JedisConnectionException;
import syncer.jedis.params.SetParams;
import syncer.replica.cmd.CMD;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.replication.Replication;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.MultiRedisClient;
import syncer.transmission.client.RedisClient;
import syncer.transmission.cmd.JedisProtocolCommand;
import syncer.transmission.compensator.PipeLineCompensatorEnum;
import syncer.transmission.entity.*;
import syncer.transmission.exception.MultiCommitEndException;
import syncer.transmission.exception.MultiCommitStartException;
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

/**
 * @author zhanenqiang
 * @Description 单机redis pipeleine版本  已修复补偿机制问题  基于Jedis
 * 断点续传基于事务机制，尽最大可能保证续传offset最新
 * 在目标中每一个库写入一个hash结构  syncer-hash-offset-checkpoint
 *
 * {ip}:{port}-runid
 * {ip}:{port}-offset
 * pointcheckVersion
 *
 * @Date 2020/12/14
 */
@Slf4j
public class JedisMultiExecPipeLineClient implements RedisClient {
    protected String host;
    protected Integer port;
    protected String sourceHost;
    protected Integer sourcePort;
    protected String password;
    protected String user;
    protected Jedis targetClient;
    protected Pipeline pipelined;
    private Integer currentDbNum = 0;

    private Replication replication;
    //批次数
    protected Integer count = 1000;
    /**
     * 上一次pipeline提交时间记录
     */
    protected Date date = new Date();
    //任务id
    private String taskId;
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
    private  AtomicBoolean multiLock=new AtomicBoolean(true);

    private volatile boolean commitMultiLockStatus=true;

    /**
     * 前面有multi
     */
    private AtomicBoolean hasMulti=new AtomicBoolean(false);


    public JedisMultiExecPipeLineClient(String host, Integer port,String user, String password,String sourceHost, Integer sourcePort,int count, long errorCount, String taskId) {
        this.host = host;
        this.port = port;
        this.taskId = taskId;
        this.password=password;
        this.sourceHost=sourceHost;
        this.sourcePort=sourcePort;
        this.user=user;
        if (count != 0) {
            this.count = count;
        }

        if (errorCount >= -1L) {
            this.errorCount = errorCount;
        }

        targetClient=createJedis(this.host,this.port,user,password);

        pipelined = targetClient.pipelined();
        retry=new ConnectErrorRetry(taskId);
        //定时回收线程

        ThreadPoolUtils.exec(new JedisMultiExecPipeLineClient.PipelineSubmitThread(taskId));
    }

    @Override
    public String get(Long dbNum, byte[] key) {
        return null;
    }

    @Override
    public String get(Long dbNum, String key) {
        return null;
    }




    /**
     * 更新最后一个replid和offset
     * @param replid
     * @param offset
     */
    @Override
    public void updateLastReplidAndOffset(String replid, long offset){
        if(Objects.nonNull(replid)){
            this.lastReplid=replid;
        }
        if(offset>-1L){
            this.lastOffset=offset;
        }
    }

    /**
     * 添加检查点Point
     */
    void addCheckPoint(){
        //checkpoint
        Map<String,String>chekpoint=new HashMap<>();
        String hostName=sourceHost+":"+sourceHost;
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
     * 本函数用于增量开始时首次提交offset
     */
    @Override
    public void commitCheckPoint(){
        commitLock.lock();
        try {
            log.info("commit first checkPoint");
            if(!hasMulti.get()){
                multi();
            }
            submitCommandNumNow();
        }finally {
            commitLock.unlock();
        }



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
                .cmd("SET".getBytes())
                .build());
        cleanData(key);
        pipelined.set(key, value);
        addCommandNum();
        return null;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        selectDb(dbNum);
        cleanData(key);
        pipelined.set(key, value, SetParams.setParams().px(ms));
        kvPersistence.addKey(EventEntity
                .builder()
                .key(key)
                .value(value)
                .stringKey(Strings.byteToString(key))
                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SET_WITH_TIME)
                .dbNum(dbNum)
                .cmd("SET".getBytes())
                .ms(ms)
                .build());
        addCommandNum();
        return null;
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {

        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.append(key, value);
            EventEntity entity = EventEntity
                    .builder()
                    .key(key)
                    .value(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.APPEND)
                    .dbNum(dbNum)
                    .cmd("APPEND".getBytes())
                    .build();
            kvPersistence.addKey(entity);

            compensatorMap(entity);
            addCommandNum();
        } finally {
            commitLock.unlock();
        }
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        selectDb(dbNum);
        commitLock.lock();

        try {
            pipelined.lpush(key, value);

            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH)
                    .dbNum(dbNum)
                    .cmd("LPUSH".getBytes())
                    .build());
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
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
                    .cmd("LPUSH".getBytes())
                    .ms(ms)
                    .build());
            pexpire(dbNum, key, ms);
        } finally {
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
                    .cmd("LPUSH".getBytes())
                    .build());
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.lpush(key, ObjectUtils.listBytes(value));
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .lpush_value(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.LPUSH_WITH_TIME_LIST)
                    .dbNum(dbNum)
                    .cmd("LPUSH".getBytes())
                    .ms(ms)
                    .build());
            pexpire(dbNum, key, ms);
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.rpush(key, value);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RPUSH)
                    .dbNum(dbNum)
                    .cmd("RPUSH".getBytes())
                    .build());
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
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
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RPUSH_WITH_TIME)
                    .dbNum(dbNum)
                    .cmd("RPUSH".getBytes())
                    .ms(ms)
                    .build());
            pexpire(dbNum, key, ms);
        } finally {
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
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RPUSH_LIST)
                    .dbNum(dbNum)
                    .cmd("RPUSH".getBytes())
                    .build());
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.rpush(key, ObjectUtils.listBytes(value));
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .lpush_value(value)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RPUSH_WITH_TIME_LIST)
                    .dbNum(dbNum)
                    .cmd("RPUSH".getBytes())
                    .ms(ms)
                    .build());
            pexpire(dbNum, key, ms);
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.sadd(key, members);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(members)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SADD)
                    .dbNum(dbNum)
                    .cmd("SADD".getBytes())
                    .build());
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.sadd(key, members);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .valueList(members)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SADD_WITH_TIME)
                    .dbNum(dbNum)
                    .cmd("SADD".getBytes())
                    .ms(ms)
                    .build());
            pexpire(dbNum, key, ms);
        } finally {
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
                    .cmd("SADD".getBytes())
                    .build());
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.sadd(key, ObjectUtils.setBytes(members));
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .members(members)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SADD_WITH_TIME_SET)
                    .dbNum(dbNum)
                    .cmd("SADD".getBytes())
                    .ms(ms)
                    .build());
            pexpire(dbNum, key, ms);
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
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
                    .cmd("ZADD".getBytes())
                    .build());
        } finally {
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
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.ZADD_WITH_TIME)
                    .dbNum(dbNum)
                    .cmd("ZADD".getBytes())
                    .ms(ms)
                    .build());
            pexpire(dbNum, key, ms);
        } finally {
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
                    .cmd("HMSET".getBytes())
                    .build());
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.hmset(key, hash);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .key(key)
                    .hash_value(hash)
                    .stringKey(Strings.byteToString(key))
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.HMSET_WITH_TIME)
                    .dbNum(dbNum)
                    .cmd("HMSET".getBytes())
                    .ms(ms)
                    .build());
            pexpire(dbNum, key, ms);
        } finally {
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
                    .cmd("RESTORE".getBytes())
                    .dbNum(dbNum)
                    .ms(ttl)
                    .build());
        } finally {
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
                    .cmd("RESTORE".getBytes())
                    .ms(ttl)
                    .build());
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            if (highVersion) {
                pipelined.restoreReplace(key, ttl, serializedValue);
                kvPersistence.addKey(EventEntity
                        .builder()
                        .key(key)
                        .value(serializedValue)
                        .stringKey(Strings.byteToString(key))
                        .pipeLineCompensatorEnum(PipeLineCompensatorEnum.RESTORREPLCE)
                        .dbNum(dbNum)
                        .cmd("RESTORE".getBytes())
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
                        .cmd("DEL".getBytes())
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
                        .cmd("RESTORE".getBytes())
                        .ms(ttl)
                        .highVersion(highVersion)
                        .build());

            }
        } finally {
            commitLock.unlock();
        }
        addCommandNum();
        return null;
    }


    /**
     * 开启事物
     */
    public void multi(){
        pipelined.multi();
        hasMulti.set(true);
        multiLock.set(false);
        kvPersistence.addKey(EventEntity
                .builder()
                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.MULTI)
                .cmd("MULTI".getBytes())
                .build());
    }

    public void multi1(){
        pipelined.multi();
        kvPersistence.addKey(EventEntity
                .builder()
                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.MULTI)
                .cmd("MULTI".getBytes())
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
     * 命令是否是multi
     *
     * 先判断
     *
     * @param cmd
     */
    void ifCommandIsMultiExec(byte[]cmd) throws MultiCommitEndException {
        String command=Strings.byteToString(cmd);
        //非嵌套,前方无multi
        if(multiLock.get()){
            if (CMD.MULTI.equalsIgnoreCase(command)){
                commitLock.lock();
                try {
                    addCheckPoint();
                    exec();
                    submitCommandNumNow();
                    multiLock.set(false);
                }finally {
                    commitLock.unlock();
                }
            }
        }

        //嵌套
        if(!multiLock.get()){
            if(CMD.EXEC.equalsIgnoreCase(command)) {
                addCheckPoint();
                exec();
                submitCommandNumNow();
                multiLock.set(true);
                throw new MultiCommitEndException();
            }
        }
    }


    //当firstMulti为false时 提交pipeline会追加exec
    //为true会追加multi
    void ifCommandIsMultiExec1(byte[]cmd) throws MultiCommitEndException, MultiCommitStartException {
        String command=Strings.byteToString(cmd);

        if(CMD.MULTI.equalsIgnoreCase(command)){
            commitMultiLockStatus=false;
            //若为非嵌套，锁住，禁止异步提交pipeline
//           boolean sts= multiLock.compareAndSet(true,false);
           if(multiLock.get()){
               multiLock.set(false);
               multi();
               throw new MultiCommitStartException();
           }
            //嵌套，若为multi，先提交前面的multi，再锁住异步提交
            if(!multiLock.get()){
                commitLock.lock();
                try {
                    exec();
                    submitCommandNumNow();
                    multiLock.set(false);
                    multi();
                    throw new MultiCommitStartException();
                }finally {
                    commitLock.unlock();
                }
            }
        }

        if(CMD.EXEC.equalsIgnoreCase(command)){
            if(!multiLock.get()){
                exec();
                submitCommandNumNow();
                multiLock.set(true);
                throw new MultiCommitEndException();
            }
            commitMultiLockStatus=true;
        }



    }

    @Override
    public Object send(byte[] cmd, byte[]... args) {
        commitLock.lock();
        try {
            ifCommandIsMultiExec1(cmd);
            if(!hasMulti.get()){
                multi();
                commitMultiLockStatus=true;
            }
            if (isSetNxWithTime(cmd, args)) {
                SetParams setParams = getSetParams(args);
                pipelined.set(args[0], args[1], setParams);

            } else {
                pipelined.sendCommand(JedisProtocolCommand.builder().raw(cmd).build(), args);
            }
            if (Strings.byteToString(cmd).toUpperCase().indexOf("SET") >= 0 || Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("RESTORE") || Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("RESTOREREPLACE") || Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("DEL")) {
                cleanData(Strings.byteToString(args[0]));
            }
            if (isSetNxWithTime(cmd, args)) {
                String byte3 = Strings.byteToString(args[3]);

                kvPersistence.addKey(EventEntity
                        .builder()
                        .key(args[0])
                        .value(args[1])
                        .stringKey(Strings.byteToString(args[0]))
                        .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SET_WITH_TIME)
                        .dbNum(Long.valueOf(currentDbNum))
                        .cmd("SET".getBytes())
                        .ms(Long.parseLong(byte3))
                        .build());
                addCommandNum();
            } else {
                if (args == null || args.length <= 0) {
                    kvPersistence.addKey(EventEntity
                            .builder()
                            .key("no key".getBytes())
                            .cmd(cmd)
                            .valueList(args)
                            .dbNum(Long.valueOf(currentDbNum))
                            .pipeLineCompensatorEnum(PipeLineCompensatorEnum.COMMAND)
                            .build());
                } else {
                    //判断幂等非幂等命令
                    if (compensatorUtils.isIdempotentCommand(cmd)) {
                        EventEntity entity = EventEntity
                                .builder()
                                .key(args[0])
                                .cmd(cmd)
                                .valueList(args)
                                .stringKey(Strings.byteToString(args[0]))
                                .dbNum(Long.valueOf(currentDbNum))
                                .pipeLineCompensatorEnum(compensatorUtils.getIdempotentCommand(cmd))
                                .build();
                        kvPersistence.addKey(entity);
                    } else {
                        kvPersistence.addKey(EventEntity
                                .builder()
                                .key(args[0])
                                .cmd(cmd)
                                .valueList(args)
                                .stringKey(Strings.byteToString(args[0]))
                                .dbNum(Long.valueOf(currentDbNum))
                                .pipeLineCompensatorEnum(PipeLineCompensatorEnum.COMMAND)
                                .build());
                    }
                }
            }
            addCommandNum();
        } catch (MultiCommitEndException e) {
            if(!multiLock.get()){
                multiLock.set(true);
            }
        } catch (MultiCommitStartException e) {

        } finally {
            commitLock.unlock();
        }
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
                    .cmd("SELECT".getBytes())
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SELECT)
                    .build());
            addCommandNum();
        } finally {
            commitLock.unlock();
        }
    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        selectDb(dbNum);
        commitLock.lock();
        try {
            pipelined.pexpire(key, ms);
            kvPersistence.addKey(EventEntity
                    .builder()
                    .dbNum(Long.valueOf(dbNum))
                    .cmd("PEXPIRE".getBytes())
                    .ms(ms)
                    .pipeLineCompensatorEnum(PipeLineCompensatorEnum.PEXPIRE)
                    .build());
        } finally {
            commitLock.unlock();
            addCommandNum();
        }
        return null;
    }


    /**
     * 判断是否是setnx带时间(SET)的命令
     *
     * @param cmd
     * @param args
     * @return
     */
    boolean isSetNxWithTime(byte[] cmd, byte[]... args) {
        String command = Strings.byteToString(cmd).trim().toUpperCase();
        return "SET".equalsIgnoreCase(command) && args.length == 5;
    }

    SetParams getSetParams(byte[]... args) {
        byte[][] data = args;
        SetParams setParams = SetParams.setParams();
        String byte2 = Strings.byteToString(data[2]).toLowerCase();
        String byte4 = Strings.byteToString(data[4]).toLowerCase();
        String byte3 = Strings.byteToString(data[3]);
        if (byte2.equalsIgnoreCase("ex")) {
            setParams.ex(Integer.parseInt(byte3));
        } else if (byte2.equalsIgnoreCase("px")) {
            setParams.px(Long.parseLong(byte3));
        }
        if (byte4.equalsIgnoreCase("nx")) {
            setParams.nx();
        } else if (byte4.equalsIgnoreCase("xx")) {
            setParams.xx();
        }
        return setParams;
    }


    void selectDb(Long dbNum) {
        commitLock.lock();
        try {

            if (dbNum != null && !currentDbNum.equals(dbNum.intValue())) {
                currentDbNum = dbNum.intValue();
                pipelined.select(dbNum.intValue());
                kvPersistence.addKey(EventEntity
                        .builder()
                        .dbNum(Long.valueOf(dbNum))
                        .pipeLineCompensatorEnum(PipeLineCompensatorEnum.SELECT)
                        .cmd("SELECT".getBytes())
                        .build());
                addCommandNum();
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
    protected Jedis createJedis(String host,int port,String user,String password){
        Jedis jedis=new Jedis(host,port);
        if (!StringUtils.isEmpty(user)&&!StringUtils.isEmpty(password)) {
            jedis.auth(user,password);
        }else if (!StringUtils.isEmpty(password)) {
            jedis.auth(password);
        }
        if(CMD.PONG.equalsIgnoreCase(jedis.ping())){
            return jedis;
        }
        return null;
    }

    ///死锁
    void submitCommandNum() {
        if (SingleTaskDataManagerUtils.isTaskClose(taskId) && taskId != null) {
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
                retry.retry(new JedisPipelineSubmitMultiCommandRetryRunner(this));
            }catch (JedisConnectionException ex){
                log.error("[TASKDI {}] pipelined retry fail",taskId);
                brokenTaskByConnectError(ex);
            }

        } finally {
            commitLock.unlock();
        }

    }

    void addCommandNum() {
        commitLock.lock();
        try {

            int num = commandNums.incrementAndGet();
            if (num >= count&&multiLock.get()) {
                if(hasMulti.get()) {
                    exec();
                }
                List<Object> resultList = pipelined.syncAndReturnAll();

                //补偿入口
                commitCompensator(resultList);
            }
        } catch (JedisConnectionException e) {
            try {
                retry.retry(new JedisPipeLineMultiRetryRunner(this));
            }catch (JedisConnectionException ex){
                log.error("[TASKDI {}] pipelined retry fail",taskId);
                brokenTaskByConnectError(ex);
            }

        } finally {
            commitLock.unlock();
        }
    }

    void submitCommandNumNow() {
        commitLock.lock();
        try {
            if(hasMulti.get()) {
                exec();
            }
            List<Object> resultList = pipelined.syncAndReturnAll();
            //补偿入口
            commitCompensator(resultList);
        } catch (JedisConnectionException e) {
            brokenTaskByConnectError(e);
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

    void compensator(EventEntity eventEntity) {
        if (SingleTaskDataManagerUtils.isTaskClose(taskId) && taskId != null) {
            return;
        }
        commitLock.lock();
        try {
            Jedis client = null;
            try {
                client = createJedis(this.host,this.port,user,password);
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
                    result = sendMI(appendMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "APPEND";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCR)) {
                    result = sendMI(incrMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "INCR";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBY)) {
                    result = sendMI(incrMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "INCRBY";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBYFLOAT)) {
                    result = sendMI(incrDoubleMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "INCRBYFLOAT";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECR)) {
                    result = sendMI(incrMap.get(eventEntity.getStringKey()), eventEntity, client);
                    command = "DECR";
                } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECRBY)) {
                    result = sendMI(incrMap.get(eventEntity.getStringKey()), eventEntity, client);
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
     * multi相关的数据补偿
     * 未做完
     *
     * TODO
     *
     * @param eventEntityList
     */
    void compensator(List<Object> resultList,List<EventEntity> eventEntityList){

        if (SingleTaskDataManagerUtils.isTaskClose(taskId) && taskId != null) {
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







    void compensatorMap(EventEntity eventEntity) {
        Jedis client = null;
        try {
            if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.COMMAND)) {
                return;
                //非幂等性命令
            } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.APPEND)) {
                if (appendMap.containsKey(eventEntity.getStringKey())) {
                    appendMap.get(eventEntity.getStringKey()).getValue().append(Strings.byteToString(eventEntity.getValue()));
                } else {
                    client = createJedis(host,port,user,password);
                    String oldValue = client.get(eventEntity.getStringKey());
                    StringBuilder stringBuilder = new StringBuilder();
                    if (org.springframework.util.StringUtils.isEmpty(oldValue)) {
                        stringBuilder.append(Strings.byteToString(eventEntity.getValue()));
                    } else {
                        stringBuilder.append(oldValue);
                        stringBuilder.append(Strings.byteToString(eventEntity.getValue()));
                    }
                    appendMap.put(eventEntity.getStringKey(), StringCompensatorEntity
                            .builder()
                            .stringKey(eventEntity.getStringKey())
                            .value(stringBuilder)
                            .key(eventEntity.getKey())
                            .dbNum(eventEntity.getDbNum())
                            .build());
                }

            } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCR)) {
                submitCommandNumNow();
                client = createJedis(host,port,user,password);
                String oldValue = client.get(eventEntity.getStringKey());
                Integer newValue = 0;
                if (org.springframework.util.StringUtils.isEmpty(oldValue) || oldValue.equalsIgnoreCase("null")) {
                    newValue++;
                } else {
                    newValue = Integer.valueOf(oldValue) + 1;
                }
                incrMap.put(eventEntity.getStringKey(), newValue);
                incrMap.get(eventEntity.getStringKey());
            } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBY)) {
                submitCommandNumNow();
                client = createJedis(host,port,user,password);
                String oldValue = client.get(eventEntity.getStringKey());
                int newValue = 0;
                String numData = Strings.byteToString(eventEntity.getValueList()[1]);
                Integer numDataInt = Integer.valueOf(numData);
                if (org.springframework.util.StringUtils.isEmpty(oldValue) || oldValue.equalsIgnoreCase("null")) {
                    newValue = numDataInt;
                } else {
                    Integer oldValueNum = Integer.valueOf(oldValue);
                    newValue = oldValueNum + numDataInt;
                }
                incrMap.put(eventEntity.getStringKey(), newValue);

            } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.INCRBYFLOAT)) {
                submitCommandNumNow();
                client = createJedis(host,port,user,password);
                String oldValue = client.get(eventEntity.getStringKey());
                float newValue = 0;
                if (org.springframework.util.StringUtils.isEmpty(oldValue) || oldValue.equalsIgnoreCase("null")) {
                    newValue -= newValue + Float.valueOf(Strings.byteToString(eventEntity.getValueList()[1]));
                } else {
                    newValue = Float.valueOf(oldValue) + Float.valueOf(Strings.byteToString(eventEntity.getValueList()[1]));
                }
                incrDoubleMap.put(eventEntity.getStringKey(), newValue);
            } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECR)) {
                submitCommandNumNow();
                client = createJedis(host,port,user,password);
                String oldValue = client.get(eventEntity.getStringKey());
                Integer newValue = 0;
                if (org.springframework.util.StringUtils.isEmpty(oldValue)) {
                    newValue--;
                } else {
                    newValue = Integer.valueOf(oldValue) - 1;
                }

                incrMap.put(eventEntity.getStringKey(), newValue);

            } else if (eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.DECRBY)) {
                submitCommandNumNow();
                client =createJedis(host,port,user,password);
                String oldValue = client.get(eventEntity.getStringKey());
                Integer newValue = 0;
                if (org.springframework.util.StringUtils.isEmpty(oldValue)) {
                    newValue -= newValue - Integer.valueOf(Strings.byteToString(eventEntity.getValueList()[1]));
                } else {
                    String num = Strings.byteToString(eventEntity.getValueList()[1]);
                    newValue = Integer.valueOf(oldValue) - Integer.valueOf(num);
                }
                incrMap.put(eventEntity.getStringKey(), newValue);
            }
        } catch (NumberFormatException ex) {
            log.warn("key[{}]非幂等转幂等单位计算错误,原因：[{}]", eventEntity.getStringKey(), ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            log.warn("key[{}]同步失败被抛弃,原因：[{}]", eventEntity.getStringKey(), e.getMessage());
            e.printStackTrace();
        } finally {
            if (null != client) {
                client.close();
            }
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

    int getCommandNums(List<Object> resultList) {
        int num = 0;
        for (Object result : resultList) {
            if (null != result && !"PONG".equalsIgnoreCase(compensatorUtils.getRes(result))) {
                num++;
            }
        }
        return num;
    }




    Object sendMI(Object data, EventEntity eventEntity, Jedis client) {
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


    void brokenTaskByConnectError(Exception e) {
        if (!connectError) {
            SingleTaskDataManagerUtils.brokenStatusAndLog(e, this.getClass(), taskId);
            connectError = true;
        }
    }

    void brokenTaskByConnectError(String msg) {
        if (!connectError) {
            SingleTaskDataManagerUtils.brokenStatusAndLog(msg, this.getClass(), taskId);
            connectError = true;
        }
    }




    /**
     * 清理非幂等命令内存结构缓存
     *
     * @param keyName
     */
    void cleanData(byte[] keyName) {
        cleanData(Strings.byteToString(keyName));
    }

    void cleanData(String keyName) {
        try {
            incrMap.remove(keyName);
            incrDoubleMap.remove(keyName);
            appendMap.remove(keyName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 超时自动提交线程
     */
    //死锁
    class PipelineSubmitThread implements Runnable {
        String taskId;
        private boolean status = true;
        private boolean startStatus = true;
        public PipelineSubmitThread(String taskId) {
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
//                if(!multiLock.get()){
//                    System.out.println("-------"+multiLock.get());
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    continue;
//                }

                compensatorLock.lock();
                try {
                    submitCommandNum();
                    if (SingleTaskDataManagerUtils.isTaskClose(taskId) && taskId != null) {
                        log.warn("task[{}]数据传输模块进入关闭保护状态,不再接收新数据", taskId);
                        Date time = new Date(date.getTime());
                        if (status) {
                            while (System.currentTimeMillis() - time.getTime() < 1000 * 60 * 1) {
                                submitCommandNum();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Thread.currentThread().interrupt();
                            status = false;
                            addCommandNum();
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
}
