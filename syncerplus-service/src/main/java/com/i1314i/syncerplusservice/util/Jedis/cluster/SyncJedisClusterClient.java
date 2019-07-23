package com.i1314i.syncerplusservice.util.Jedis.cluster;

import com.i1314i.syncerplusservice.util.Jedis.IJedisClient;
import com.i1314i.syncerplusservice.util.Jedis.JedisClusterFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SyncJedisClusterClient implements IJedisClient {
    private JedisClusterFactory jedisClusterFactory=null;
    private GenericObjectPoolConfig config=null;
    private Set<String> jedisClusterNodes=null;

    private  String jedisaddress;
    private  String password;
    private Integer maxTotal;
    private Integer minIdle;
    private long timeOut;
    private long connectTimeout;

    private JedisCluster jedisCluster=null;

    public SyncJedisClusterClient(String jedisaddress, String password, Integer maxTotal, Integer minIdle, long timeOut, long connectTimeout) {
        this.jedisaddress = jedisaddress;
        this.password = password;
        this.maxTotal = maxTotal;
        this.minIdle = minIdle;
        this.timeOut = timeOut;
        this.connectTimeout = connectTimeout;
    }

    //    @Bean
    public JedisCluster jedisCluster() throws ParseException {
        jedisClusterFactory=new JedisClusterFactory();
        config=new GenericObjectPoolConfig();
        config.setMaxTotal(100);
        config.setMinIdle(10);
        jedisClusterFactory.setSoTimeout(100000);
        jedisClusterFactory.setConnectionTimeout(100000);
        if(!StringUtils.isEmpty(password)){
            jedisClusterFactory.setPassWord(password);
        }
        jedisClusterNodes=new HashSet<>();
        String[]jedisClusterAddress=jedisaddress.split(",");
        for (String address:
                jedisClusterAddress) {
            log.info("NodeAddress:[{}]",address);
            jedisClusterNodes.add(address);
        }

        jedisClusterFactory.setJedisClusterNodes(jedisClusterNodes);
        jedisClusterFactory.setGenericObjectPoolConfig(config);
        return jedisClusterFactory.getJedisCluster();
    }




    public JedisCluster getJedisCluster(){
        if(jedisCluster!=null){
            return jedisCluster;
        }
        return null;
    }

    public void builder(SyncJedisClusterClient client){
        if(jedisCluster==null){
            try {
                jedisCluster=client.jedisCluster();
            } catch (ParseException e) {
                log.info("jedisCluster is error:【{}】",e.getMessage());
            }
        }
    }

    @Override
    public String setObject(String key, Object value, int cacheSeconds) {


        return    jedisCluster.set(key,"sss");
    }



    @Override
    public String setbyteObject(byte[] key, byte[] value, Integer cacheSeconds) {
        return null;
    }



    public String restore(byte[] key, byte[] value, Integer cacheSeconds) {
        return null;
    }


    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public Object getObject(String key) {
        return null;
    }

    @Override
    public String set(String key, String value, int cacheSeconds) {
        return null;
    }

    @Override
    public List<String> keys(String key) {
        return null;
    }



    @Override
    public List<String> getList(String key) {
        return null;
    }

    @Override
    public List<Object> getObjectList(String key) {
        return null;
    }



    @Override
    public long setList(String key, List<String> value, int cacheSeconds) {
        return 0;
    }

    @Override
    public long setObjectList(String key, List<Object> value, int cacheSeconds) {
        return 0;
    }

    @Override
    public long listAdd(String key, String... value) {
        return 0;
    }

    @Override
    public long listObjectAdd(String key, Object... value) {
        return 0;
    }

    @Override
    public Set<String> getSet(String key) {
        return null;
    }

    @Override
    public Set<Object> getObjectSet(String key) {
        return null;
    }

    @Override
    public long setSet(String key, Set<String> value, int cacheSeconds) {
        return 0;
    }

    @Override
    public long setObjectSet(String key, Set<Object> value, int cacheSeconds) {
        return 0;
    }

    @Override
    public long setSetAdd(String key, String... value) {
        return 0;
    }

    @Override
    public long setSetObjectAdd(String key, Object... value) {
        return 0;
    }

    @Override
    public Map<String, String> getMap(String key) {
        return null;
    }

    @Override
    public Map<String, Object> getObjectMap(String key) {
        return null;
    }

    @Override
    public String setMap(String key, Map<String, String> value, int cacheSeconds) {
        return null;
    }

    @Override
    public String setObjectMap(String key, Map<String, Object> value, int cacheSeconds) {
        return null;
    }

    @Override
    public String mapPut(String key, Map<String, String> value) {
        return null;
    }

    @Override
    public String mapObjectPut(String key, Map<String, Object> value) {
        return null;
    }

    @Override
    public long mapRemove(String key, String mapKey) {
        return 0;
    }

    @Override
    public long mapObjectRemove(String key, String mapKey) {
        return 0;
    }

    @Override
    public boolean mapExists(String key, String mapKey) {
        return false;
    }

    @Override
    public boolean mapObjectExists(String key, String mapKey) {
        return false;
    }

    @Override
    public long del(String key) {
        return 0;
    }

    @Override
    public long delObject(String key) {
        return 0;
    }

    @Override
    public boolean exists(String key) {
        return false;
    }

    @Override
    public boolean existsObject(String key) {
        return false;
    }

    @Override
    public void flushlikekey(String key) {

    }

    @Override
    public void flushlikekey(String... keys) {

    }

    @Override
    public void flushlikekey_foreach(String key) {

    }

    @Override
    public long pttl(String key) {
        return 0;
    }

    @Override
    public long ttl(String key) {
        return 0;
    }

    @Override
    public long expire(String key, int seconds) {
        return 0;
    }

    @Override
    public long pexpire(String key, long milliseconds) {
        return 0;
    }

    @Override
    public Set<String> allkeys(String redisKeyStartWith) {
        return null;
    }

    @Override
    public void deleteRedisKeyStartWith(String redisKeyStartWith) {

    }
}
