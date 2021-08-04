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

package syncer.transmission.client.impl.sentinel;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.jedis.*;
import syncer.jedis.params.SetParams;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.util.strings.Strings;
import syncer.transmission.client.RedisClient;
import syncer.transmission.client.jedis.impl.SyncJedisClusterClient;
import syncer.transmission.cmd.ClusterProtocolCommand;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.util.object.ObjectUtils;
import syncer.transmission.util.strings.StringUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class RedisJedisSentinelClient implements RedisClient {
    private String host;
    //任务id
    private String taskId;
    public static final String OK="OK";
    //private JedisSentinelPool pool;
    CheckFailover pool;
    private String masterName;
    private String currentMaster;
    private AtomicBoolean status;
    private Jedis sysclient;
    public RedisJedisSentinelClient(String host, String password,String masterName, String taskId) {
        this.host = host;
        this.taskId = taskId;
        this.masterName=masterName;
        status=new AtomicBoolean(true);

//        pool=new JedisSentinelPool(masterName, hostAndportSet(), jedisPoolConfig, 10000, password);
        pool=new CheckFailover(masterName, hostAndportSet(), password,10000, 10000 );
    }

    void updateCommitTime(){
        //记录任务最后一次update时间
        try {
            if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskId)) {
                TaskDataEntity dataEntity = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId);
                dataEntity.getTaskModel().setLastKeyCommitTime(System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("[{}] sentinel update last commit time error", taskId);
        }
    }

    @Override
    public String get(final Long dbNum,byte[] key) {
        Jedis client=createClient();
        String res=client.get(StringUtils.toString(key));
        return res;
    }

    @Override
    public String get(final Long dbNum,String key) {
        Jedis redisClient=createClient();
        return  redisClient.get(key);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.set(key,value);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.set(key,value, SetParams.setParams().px(ms));
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.append(key, value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.lpush(key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        Long res= redisClient.lpush(key,value);
            redisClient.pexpire(key,ms);
            return res;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return  redisClient.lpush(key, ObjectUtils.listBytes(value));
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        Long res= redisClient.lpush(key, ObjectUtils.listBytes(value));
            redisClient.pexpire(key,ms);
            return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.rpush(key,value);
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        Long res= redisClient.rpush(key,value);
            redisClient.pexpire(key,ms);
            return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, List<byte[]> value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return  redisClient.rpush(key, ObjectUtils.listBytes(value));
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        Long res= redisClient.rpush(key,ObjectUtils.listBytes(value));
            redisClient.pexpire(key,ms);
            return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.sadd(key,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        updateCommitTime();
        Jedis redisClient=createClient();
        Long res= redisClient.sadd(key,members);
            redisClient.pexpire(key,ms);
            return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        updateCommitTime();
        Jedis redisClient=createClient();
        Long res= redisClient.sadd(key,ObjectUtils.setBytes(members));
            return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        updateCommitTime();
        Jedis redisClient=createClient();
        Long res= redisClient.sadd(key,ObjectUtils.setBytes(members));
            redisClient.pexpire(key,ms);
            return res;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.zadd(key,ObjectUtils.zsetBytes(value));
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        updateCommitTime();
        Jedis redisClient=createClient();
        Long res= redisClient.zadd(key,ObjectUtils.zsetBytes(value));
            redisClient.pexpire(key,ms);
            return res;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.hmset(key,hash);
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        updateCommitTime();
        Jedis redisClient=createClient();
            String res= redisClient.hmset(key,hash);
            redisClient.pexpire(key,ms);
            return res;
    }

    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.restore(key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.restoreReplace(key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.restoreReplace(key,ttl,serializedValue);
    }

    @Override
    public Object send(byte[] cmd, byte[]... args) throws Exception {
        updateCommitTime();
        if(Objects.isNull(args)||args.length<1){
            log.error("[{}] commands not supported by cluster mode [{}]",taskId,Strings.byteToString(cmd));
            throw new Exception("["+taskId+"] commands not supported by cluster mode ["+Strings.byteToString(cmd)+"]");
        }else {
            Jedis redisClient=createClient();
            return redisClient.sendCommand(ClusterProtocolCommand.builder().raw(cmd).build(),args);
        }
    }

    /**
     * 更新最后一个replid和offset
     * @param replid
     * @param offset
     */
    @Override
    public void updateLastReplidAndOffset(String replid, long offset){
    }

    @Override
    public void commitCheckPoint() {

    }

    @Override
    public void close() {
        if(Objects.nonNull(pool)){
            pool.destroy();
        }
        if(Objects.nonNull(sysclient)){
            sysclient.close();
        }
    }


    @Override
    public void select(Integer dbNum) {

    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        updateCommitTime();
        Jedis redisClient=createClient();
        return redisClient.pexpire(key,ms);

    }

    protected Set<String>hostAndportSet(){
        return Arrays.stream(host.split(";")).filter(hs->{
            return Objects.nonNull(hs);
        }).distinct().collect(Collectors.toSet());
    }

    Jedis createClient(){
        if(Objects.nonNull(pool)){
            String address=pool.getCurrentHostMaster().toString();
            if(Objects.isNull(currentMaster)||!currentMaster.equalsIgnoreCase(address)){
                if(Objects.isNull(currentMaster)){
                    log.warn("[{}] connected to [{}]",taskId,address);
                }else {
                    log.warn("[{}] failover from [{}] to [{}]",taskId,currentMaster,address);
                }
                currentMaster=address;
            }
            return pool.getClient();
        }
        return null;
    }

    class CheckFailover extends SentinelFailOverListener{
        public CheckFailover(String masterName, Set<String> sentinels, String password, int sentinelConnectionTimeout, int sentinelSoTimeout) {
            super(masterName, sentinels, password, sentinelConnectionTimeout, sentinelSoTimeout);
        }

        public CheckFailover(String masterName, Set<String> sentinels, String password) {
            super(masterName, sentinels, password);
        }


        @Override
        protected void initClient(HostAndPort master) {
            super.initClient(master);
            sysclient=new Jedis(master.getHost(), master.getPort());
            if(Objects.nonNull(password)){
                sysclient.auth(password);
            }
        }

        protected Jedis getClient(){
            if(Objects.isNull(sysclient)){
                initClient(getCurrentHostMaster());
            }
            return sysclient;
        }

    }
}
