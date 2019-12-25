package syncer.syncerservice.util.jedis.cluster;


import org.springframework.util.StringUtils;
import syncer.syncerjedis.Jedis;
import syncer.syncerjedis.JedisCluster;
import syncer.syncerjedis.JedisPool;
import syncer.syncerjedis.JedisPoolConfig;
import syncer.syncerservice.util.jedis.IJedisClient;
import syncer.syncerservice.util.jedis.JeUtil;
import syncer.syncerservice.util.jedis.JedisClusterFactory;


import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SyncJedisClusterClient implements IJedisClient {
    private JedisClusterFactory jedisClusterFactory=null;
    private JedisPoolConfig config=null;
    private Set<String> jedisClusterNodes=null;

    private  String jedisaddress;
    private  String password;
    private Integer maxTotal;
    private Integer minIdle;
    private long timeOut;
    private long connectTimeout;

    private JedisCluster jedisCluster=null;

    public SyncJedisClusterClient(String jedisaddress, String password, Integer maxTotal, Integer minIdle, long timeOut, int connectTimeout) throws ParseException {
        this.jedisaddress = jedisaddress;
        this.password = password;
        this.maxTotal = maxTotal;
        this.minIdle = minIdle;
        this.timeOut = timeOut;
        this.connectTimeout = connectTimeout;

        jedisClusterFactory=new JedisClusterFactory();
        config=new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMinIdle(10);
        jedisClusterFactory.setSoTimeout(100000);

        jedisClusterFactory.setConnectionTimeout(connectTimeout);
        if(!StringUtils.isEmpty(password)){
            jedisClusterFactory.setPassWord(password);
        }
        jedisClusterNodes=new HashSet<>();
        String[]jedisClusterAddress=jedisaddress.split(";");
        for (String address:
                jedisClusterAddress) {

            jedisClusterNodes.add(address);

        }

        jedisClusterFactory.setJedisClusterNodes(jedisClusterNodes);
        jedisClusterFactory.setJedisPoolConfig(config);
        jedisCluster=jedisClusterFactory.getJedisCluster();
    }

    //    @Bean
    public JedisCluster jedisCluster() throws ParseException {
        if(jedisClusterFactory!=null){
            return jedisClusterFactory.getJedisCluster();
        }

        return null;
    }


    @Override
    public  Set<String>  allkeys(String redisKeyStartWith) {
        Set<String>allkey=new HashSet<>();
        try {
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();



            for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {

//                if(!set.contains(entry.getKey())){
//                    continue;
//                }


                Jedis jedis = entry.getValue().getResource();

                //         判断非从节点(因为若主从复制，从节点会跟随主节点的变化而变化)
                if (!jedis.info("replication").contains("role:slave")) {
                    Set<String> keys = JeUtil.getScanSet(jedis,redisKeyStartWith);
                    //旧版 keys 会堵塞主线程
//                    Set<String> keys = jedis.keys(redisKeyStartWith);
                    if (keys.size() > 0) {

                        allkey.addAll(keys);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return allkey;
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
    public void deleteRedisKeyStartWith(String redisKeyStartWith) {

    }
}
