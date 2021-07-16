package syncer.syncerservice.util.JDRedisClient;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerjedis.JedisCluster;
import syncer.syncerjedis.params.SetParams;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.cmd.ClusterProtocolCommand;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.jedis.ObjectUtils;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerservice.util.jedis.cluster.SyncJedisClusterClient;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class JDRedisJedisClusterClient implements JDRedisClient {



    private String host;
    //任务id
    private String taskId;
    private JedisCluster redisClient;





    public JDRedisJedisClusterClient(String host,  String password,String taskId) {
        this.host = host;
        this.taskId = taskId;

        try {
            SyncJedisClusterClient pool=new SyncJedisClusterClient( host,password,10,5000,5000,5000);
            redisClient=pool.jedisCluster();

        } catch (ParseException e) {
            e.printStackTrace();
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
        String res=redisClient.set(key,value);

        return res;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {
        return redisClient.set(key,value, SetParams.setParams().px(ms));
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        return redisClient.append(key, value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        return redisClient.lpush(key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        Long res= redisClient.lpush(key,value);
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        return  redisClient.lpush(key, ObjectUtils.listBytes(value));
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        Long res= redisClient.lpush(key,ObjectUtils.listBytes(value));
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        return redisClient.rpush(key,value);
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        Long res= redisClient.rpush(key,value);
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, List<byte[]> value) {
        return  redisClient.rpush(key, ObjectUtils.listBytes(value));
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        Long res= redisClient.rpush(key,ObjectUtils.listBytes(value));
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        return redisClient.sadd(key,members);
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        Long res= redisClient.sadd(key,members);
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        Long res= redisClient.sadd(key,ObjectUtils.setBytes(members));
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        Long res= redisClient.sadd(key,ObjectUtils.setBytes(members));
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        return redisClient.zadd(key,ObjectUtils.zsetBytes(value));
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        Long res= redisClient.zadd(key,ObjectUtils.zsetBytes(value));
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
        return redisClient.hmset(key,hash);
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        String res= redisClient.hmset(key,hash);
        redisClient.pexpire(key,ms);
        return res;
    }

    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return redisClient.restore(key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return redisClient.restoreReplace(key,ttl,serializedValue);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        return redisClient.restoreReplace(key,ttl,serializedValue);
    }

    @Override
    public Object send(byte[] cmd, byte[]... args) {
        if(Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("FLUSHALL")
                ||Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("MULTI")
                ||Strings.byteToString(cmd).toUpperCase().equalsIgnoreCase("EXEC")
        ){
            return "OK";
        }
        System.out.println(Strings.byteToString(cmd)+" "+Strings.byteToString(args));
        if(Objects.isNull(args)||args.length<1){
            return redisClient.sendCommand(ClusterProtocolCommand.builder().raw(cmd).build(),args);
        }else {
            return redisClient.sendCommand(args[0], ClusterProtocolCommand.builder().raw(cmd).build(),args);
        }
    }

    @Override
    public void select(Integer dbNum) {

    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        return redisClient.pexpire(key,ms);
    }

}
