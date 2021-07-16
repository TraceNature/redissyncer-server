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

import lombok.extern.slf4j.Slf4j;
import syncer.jedis.JedisCluster;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class RedisJedisClusterClient implements RedisClient {



    private String host;
    //任务id
    private String taskId;
    private JedisCluster redisClient;





    public RedisJedisClusterClient(String host, String password, String taskId) {
        this.host = host;
        this.taskId = taskId;

        try {
            SyncJedisClusterClient pool=new SyncJedisClusterClient( host,password,10,5000,5000,5000);
            redisClient=pool.jedisCluster();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void updateCommitTime(){
        //记录任务最后一次update时间
        try {
            if (SingleTaskDataManagerUtils.getAliveThreadHashMap().containsKey(taskId)) {
                TaskDataEntity dataEntity = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId);
                dataEntity.getTaskModel().setLastKeyCommitTime(System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("[{}] cluster update last commit time error", taskId);
        }
    }

    @Override
    public String get(final Long dbNum,byte[] key) {
        return  redisClient.get(StringUtils.toString(key));
    }

    @Override
    public String get(final Long dbNum,String key) {
        return  redisClient.get(key);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        updateCommitTime();
        String res=redisClient.set(key,value);
        return res;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        updateCommitTime();
        return redisClient.set(key,value, SetParams.setParams().px(ms));
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        updateCommitTime();
        return redisClient.append(key, value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        updateCommitTime();
        return redisClient.lpush(key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        updateCommitTime();
        Long res= redisClient.lpush(key,value);
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        updateCommitTime();
        return  redisClient.lpush(key, ObjectUtils.listBytes(value));
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        updateCommitTime();
        Long res= redisClient.lpush(key, ObjectUtils.listBytes(value));
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        updateCommitTime();
        return redisClient.rpush(key,value);
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        updateCommitTime();
        Long res= redisClient.rpush(key,value);
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, List<byte[]> value) {
        updateCommitTime();
        return  redisClient.rpush(key, ObjectUtils.listBytes(value));
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        updateCommitTime();
        Long res= redisClient.rpush(key,ObjectUtils.listBytes(value));
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        updateCommitTime();
        return redisClient.sadd(key,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        updateCommitTime();
        Long res= redisClient.sadd(key,members);
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        updateCommitTime();
        Long res= redisClient.sadd(key,ObjectUtils.setBytes(members));
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        updateCommitTime();
        Long res= redisClient.sadd(key,ObjectUtils.setBytes(members));
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        updateCommitTime();
        return redisClient.zadd(key,ObjectUtils.zsetBytes(value));
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        updateCommitTime();
        Long res= redisClient.zadd(key,ObjectUtils.zsetBytes(value));
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        updateCommitTime();
        return redisClient.hmset(key,hash);
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        updateCommitTime();
        String res= redisClient.hmset(key,hash);
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        updateCommitTime();
        return redisClient.restore(key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        updateCommitTime();
        return redisClient.restoreReplace(key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        updateCommitTime();
        return redisClient.restoreReplace(key,ttl,serializedValue);
    }

    @Override
    public Object send(byte[] cmd, byte[]... args) {
        updateCommitTime();
        if(Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("FLUSHALL")
                ||Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("MULTI")
                ||Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("EXEC")
        ){
            return "OK";
        }
        if(Objects.isNull(args)||args.length<1){
            return redisClient.sendCommand(ClusterProtocolCommand.builder().raw(cmd).build(),args);
        }else {
            return redisClient.sendCommand(args[0], ClusterProtocolCommand.builder().raw(cmd).build(),args);
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

    }


    @Override
    public void select(Integer dbNum) {

    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        updateCommitTime();
        return redisClient.pexpire(key,ms);
    }

}
