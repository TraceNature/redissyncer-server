package syncer.syncerservice.util.JDRedisClient.jimdb;
import com.jd.jim.cli.*;
import com.jd.jim.cli.config.ConfigLongPollingClientFactory;
import com.jd.jim.cli.protocol.CommandType;
import syncer.syncerplusredis.rdb.datatype.ZSetEntry;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.jedis.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.TimeUnit;

/**
 * @author zhanenqiang
 * @Description jimdb 2.x版本客户端
 * @Date 2020/4/27
 */
public class JimDb2Client implements JDRedisClient {
    private  String jimUrl;
    private String cfsUrl;
    private String taskId;
    private Cluster client;
    private final String OK="OK";
    private ConfigLongPollingClientFactory configClientFactory;
    private ReloadableJimClientFactory factory;
    public JimDb2Client(String jimUrl, String cfsUrl,String taskId) {
        this.jimUrl = jimUrl;
        this.cfsUrl = cfsUrl;
        this.taskId=taskId;
        loading();
    }

    public void loading(){
         configClientFactory = new ConfigLongPollingClientFactory(
                cfsUrl);

         factory = new ReloadableJimClientFactory();
        //不设置默认是0
        factory.setConfigId("0");
        factory.setJimUrl(jimUrl);
        factory.setConfigClient(configClientFactory.create());
        client = factory.getClient();
    }

    @Override
    public String get(Long dbNum, byte[] key) {
        return Strings.byteToString(client.get(key));
    }

    @Override
    public String get(Long dbNum, String key) {
        return client.get(key);
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value) {
        client.set(key,value);
        return OK;
    }

    @Override
    public String set(Long dbNum, byte[] key, byte[] value, long ms) {

        client.set(key,value);
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return OK;
    }

    @Override
    public Long append(Long dbNum, byte[] key, byte[] value) {
        return client.append(key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, byte[]... value) {
        return client.lPush(key,value);
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        Long res=client.lPush(key,value);
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return res;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, List<byte[]> value) {
        Long res= client.lPush(key, ObjectUtils.listBytes(value));
        return res;
    }

    @Override
    public Long lpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        Long res= client.lPush(key, ObjectUtils.listBytes(value));
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, byte[]... value) {
        Long res= client.rPush(key, value);
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, byte[]... value) {
        Long res= client.rPush(key, value);
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, List<byte[]> value) {
        Long res= client.rPush(key,  ObjectUtils.listBytes(value));
        return res;
    }

    @Override
    public Long rpush(Long dbNum, byte[] key, long ms, List<byte[]> value) {
        Long res= client.rPush(key,  ObjectUtils.listBytes(value));
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, byte[]... members) {
        Long res= client.sAdd(key,  members);
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, byte[]... members) {
        Long res= client.sAdd(key,  members);
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, Set<byte[]> members) {
        Long res= client.sAdd(key,   ObjectUtils.setBytes(members));
        return res;
    }

    @Override
    public Long sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members) {
        Long res= client.sAdd(key,   ObjectUtils.setBytes(members));
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return res;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value) {
        Long res= client.zAdd(key,ObjectUtils.jimDbZsetBytes(value));
        return res;
    }

    @Override
    public Long zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms) {
        Long res= client.zAdd(key,ObjectUtils.jimDbZsetBytes(value));
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return res;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash) {
       client.hMSet(key,hash);
        return OK;
    }

    @Override
    public String hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms) {
        client.hMSet(key,hash);
        client.pExpire(key,ms, TimeUnit.MILLISECONDS);
        return OK;
    }

    @Override
    public String restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        return client.restore(key,serializedValue,ttl,TimeUnit.MILLISECONDS);
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue) {
        String res;
        if (client.del(key) >= 0) {
            res = client.restore(key, serializedValue, ttl,TimeUnit.MILLISECONDS);
        }else {
            client.del(key);
            res = client.restore(key, serializedValue, ttl,TimeUnit.MILLISECONDS);
        }
        return res;
    }

    @Override
    public String restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion) {
        return restoreReplace(dbNum,key,ttl,serializedValue,highVersion);
    }

    @Override
    public Object send(byte[] cmd, byte[]... args) {
        try {
            return client.sendCommand(CommandType.valueOf(Strings.byteToString(cmd)),args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return OK;
//        return client.c;
    }

    @Override
    public void select(Integer dbNum) {

    }

    @Override
    public Long pexpire(Long dbNum, byte[] key, long ms) {
        client.pExpire(key,ms,TimeUnit.MILLISECONDS);
        return 1L;
    }
}
