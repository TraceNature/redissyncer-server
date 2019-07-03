package com.i1314i.syncerpluscommon.util.redis;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import com.i1314i.syncerpluscommon.util.common.TemplateUtils;
import com.i1314i.syncerpluscommon.util.redis.other.ObjectUtils;
import com.i1314i.syncerpluscommon.util.redis.other.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.util.JedisClusterCRC16;

import java.util.*;

/**
 * 集群方式redis客户端操作
 * @author 平行时空
 * @created 2018-06-14 22:10
 **/

//@Service
public class JedisClusterClient implements IJedisClient {
    private Logger logger = LoggerFactory.getLogger(JedisClusterClient.class);

//
    @Autowired
    private JedisCluster jedisCluster;
//	public   final String KEY_PREFIX = Global.getConfig("redis.keyPrefix");

	/* (non-Javadoc)
	 * @see com.jeeplus.common.redis.IJedisClient#get(java.lang.String)
	 */

    @Override
    public  String get(String key) {
        String value = null;
        try {
            if (jedisCluster.exists(key)) {
                value = jedisCluster.get(key);
                value = StringUtils.isNotBlank(value) && !"nil".equalsIgnoreCase(value) ? value : null;
                logger.debug("get {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("get {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#getObject(java.lang.String)
     */
    @Override
    public   Object getObject(String key) {
        Object value = null;
        try {
            if (jedisCluster.exists(getBytesKey(key))) {
                value = toObject(jedisCluster.get(getBytesKey(key)));
                logger.debug("getObject {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getObject {} = {}", key, value, e);
        } finally {
            //returnResource(jedis);
//			try {
//				jedisCluster.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#set(java.lang.String, java.lang.String, int)
     */
    @Override
    public   String set(String key, String value, int cacheSeconds) {
        String result = null;

        try {

            result = jedisCluster.set(key, value);
            if (cacheSeconds != 0) {
                jedisCluster.expire(key, cacheSeconds);
            }
            logger.debug("set {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("set {} = {}", key, value, e);
        } finally {
            //returnResource(jedis);
        }
        return result;
    }


//    public long getKeys(String key,boolean isDel) throws Exception {
//        Long startTime = System.currentTimeMillis();
//        long resSum=0;
//        List<Jedis> jedisList = new ArrayList<Jedis>();// jedis 操作集合
//        for (HostAndPort hostAndport : jedisCluster.getHaps()) {
//            jedisList.add(new Jedis(hostAndport.getHost(), hostAndport
//                    .getPort()));
//        }
//        int size =jedisList.size();
//
//        if (null != jedisList && size > 0) {
//            ScanParams params = new ScanParams();
//            params.match(key+"*");/**模糊匹配**/
//            //首选计算主node数量
//            List<Jedis> masterList = new  ArrayList<Jedis>();
//            for (int i = 0; i < size; i++) {
//                Jedis jedis = null;
//                jedis = jedisList.get(i);
//                if (JedisUtils.isMaster(jedis)) {
//                    masterList.add(jedis);
//                }else{
//                    /**关掉slave连接 **/
//                    jedis.close();
//                }
//            }
//            int masterSize =masterList.size();
//            logger.info("jedis操作实例创建完毕,master数量:" +masterSize);
//
//            if(null!=masterList&&masterSize>0){
//                CountDownLatch countDownLatch = new CountDownLatch(masterSize);
//                @SuppressWarnings("rawtypes")
//                Future[] future_Arr = new Future[masterSize] ;
//                ExecutorService es = ThreadSinleton.getExecutorService();
//                for (int j =0; j < masterSize; j++) {
//                    Jedis jedis = null;
//                    Pipeline pipeline=null;
//                    jedis = masterList.get(j);
//                    future_Arr[j] = es.submit(new CacheBodyThread(
//                            "子线程"+j, jedis, pipeline, isDel, params, countDownLatch));
//                }
//                try {
////					LOGGER.info("*******主线程正在汇总************");
//                    countDownLatch.await();
//                    try {
//                        if(null!=future_Arr&&future_Arr.length>0){
//                            for (int i = 0; i < future_Arr.length; i++) {
//                                resSum+=(long)future_Arr[i].get();
//                            }
//                        }
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        logger.error(e.getMessage());
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                        logger.error(e.getMessage());
//                    }
//                    //es.shutdown(); 线程池不需关闭
//                    logger.info("*******线程池关闭，主线程正在汇总完毕==========,"+resSum);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    logger.error(e.getMessage());
//                }
//            }
//        }
//        Long endTime = System.currentTimeMillis();
//        logger.error((true==isDel?"清理":"统计")+"缓存,[执行模糊查找所有键]end,待处理集合数据长度："+resSum+",using time is<耗时>:" + (endTime - startTime)+"ms");
//        return resSum;
//    }
//

    public  void deleteRedisKeyStartWith(String redisKeyStartWith) {
        try {
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            String jedisaddress= TemplateUtils.getPropertiesdata("dao.properties","jedis.cluster.address");
            String[]jedisclusteraddress=jedisaddress.split(",");
            Set set=new HashSet();
            for (String s:jedisclusteraddress
                    ) {
                set.add(s);
            }
            for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {

//                if(!set.contains(entry.getKey())){
//                    continue;
//                }
                Jedis jedis = entry.getValue().getResource();
                // 判断非从节点(因为若主从复制，从节点会跟随主节点的变化而变化)
                if (!jedis.info("replication").contains("role:slave")) {
                    Set<String> keys = jedis.keys(redisKeyStartWith + "*");
                    if (keys.size() > 0) {
                        Map<Integer, List<String>> map = new HashMap<>();
                        for (String key : keys) {
                            // cluster模式执行多key操作的时候，这些key必须在同一个slot上，不然会报:JedisDataException:
                            // CROSSSLOT Keys in request don't hash to the same slot
                            int slot = JedisClusterCRC16.getSlot(key);
                            // 按slot将key分组，相同slot的key一起提交
                            if (map.containsKey(slot)) {
                                map.get(slot).add(key);
                            } else {
                                map.put(slot, Lists.newArrayList(key));
                            }
                        }
                        for (Map.Entry<Integer, List<String>> integerListEntry : map.entrySet()) {
                            jedis.del(integerListEntry.getValue().toArray(new String[integerListEntry.getValue().size()]));

//                            for (String s:integerListEntry.getValue()
//                                 ) {
//                                System.out.println(s);
//                            }
//                            System.out.println(integerListEntry.getValue().toArray(new String[integerListEntry.getValue().size()]));
                        }
                    }
                }
            }
            logger.info("success deleted redisKeyStartWith:{}", redisKeyStartWith);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public  Set<String>  allkeys(String redisKeyStartWith) {
        Set<String>allkey=new HashSet<>();
        try {
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            System.out.println(clusterNodes.size());
            String jedisaddress= TemplateUtils.getPropertiesdata("dao.properties","jedis.cluster.address");
            String[]jedisclusteraddress=jedisaddress.split(",");
            Set set=new HashSet();
            for (String s:jedisclusteraddress
                 ) {
                set.add(s);
            }
            for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {

//                if(!set.contains(entry.getKey())){
//                    continue;
//                }
                System.out.println(entry.getKey());
                Jedis jedis = entry.getValue().getResource();

        //         判断非从节点(因为若主从复制，从节点会跟随主节点的变化而变化)
                if (!jedis.info("replication").contains("role:slave")) {
                    Set<String> keys = jedis.keys(redisKeyStartWith);

                    if (keys.size() > 0) {
                        System.out.println(keys.size());
                        allkey.addAll(keys);
                    }
                }
            }
            logger.info("success deleted redisKeyStartWith:{}", redisKeyStartWith);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return allkey;
    }



    /**
     * keys
     * @param key
     * @return
     */

    @Override
    public List<String>  keys(String key) {
        logger.info("开始模糊查询【{}】keys", key);
        List<String> keys = new ArrayList<>();
        //获取所有连接池节点
        Map<String, JedisPool> nodes = jedisCluster.getClusterNodes();
        //遍历所有连接池，逐个进行模糊查询
        for(String k : nodes.keySet()){
            logger.debug("从【{}】获取keys", k);
            JedisPool pool = nodes.get(k);
            //获取Jedis对象，Jedis对象支持keys模糊查询
            Jedis connection = pool.getResource();

            try {
                keys.addAll(connection.keys(key));
            } catch(Exception e){
                logger.error("获取key异常", e);
            } finally{
                logger.info("关闭连接");
                //一定要关闭连接！
                connection.close();
            }
        }
        logger.info("已获取所有keys");
        return keys;

    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setObject(java.lang.String, java.lang.Object, int)
     */
    @Override
    public   String setObject(String key, Object value, int cacheSeconds) {
        String result = null;

        try {
            result = jedisCluster.set(getBytesKey(key), toBytes(value));
            if (cacheSeconds != 0) {
                jedisCluster.expire(key, cacheSeconds);
            }
            logger.debug("setObject {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setObject {} = {}", key, value, e);
        } finally {
            //returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#getList(java.lang.String)
     */
    @Override
    public List<String> getList(String key) {
        List<String> value = null;
        try {
            if (jedisCluster.exists(key)) {
                value = jedisCluster.lrange(key, 0, -1);
                logger.debug("getList {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getList {} = {}", key, value, e);
        } finally {
            //returnResource(jedis);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#getObjectList(java.lang.String)
     */
    @Override
    public   List<Object> getObjectList(String key) {
        List<Object> value = null;
        try {
            if (jedisCluster.exists(getBytesKey(key))) {
                List<byte[]> list = jedisCluster.lrange(getBytesKey(key), 0, -1);
                value = Lists.newArrayList();
                for (byte[] bs : list){
                    value.add(toObject(bs));
                }
                logger.debug("getObjectList {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getObjectList {} = {}", key, value, e);
        } finally {
            //returnResource(jedis);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setList(java.lang.String, java.util.List, int)
     */
    @Override
    public   long setList(String key, List<String> value, int cacheSeconds) {
        long result = 0;
        try {
            if (jedisCluster.exists(key)) {
                jedisCluster.del(key);
            }
            result = jedisCluster.rpush(key, (String[])value.toArray());
            if (cacheSeconds != 0) {
                jedisCluster.expire(key, cacheSeconds);
            }
            logger.debug("setList {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setList {} = {}", key, value, e);
        } finally {
            //returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setObjectList(java.lang.String, java.util.List, int)
     */
    @Override
    public   long setObjectList(String key, List<Object> value, int cacheSeconds) {
        long result = 0;
        try {
            if (jedisCluster.exists(getBytesKey(key))) {
                jedisCluster.del(key);
            }
            List<byte[]> list = Lists.newArrayList();
            for (Object o : value){
                list.add(toBytes(o));
            }
            result = jedisCluster.rpush(getBytesKey(key), (byte[][])list.toArray());
            if (cacheSeconds != 0) {
                jedisCluster.expire(key, cacheSeconds);
            }
            logger.debug("setObjectList {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setObjectList {} = {}", key, value, e);
        } finally {
            //returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#listAdd(java.lang.String, java.lang.String)
     */
    @Override
    public   long listAdd(String key, String... value) {
        long result = 0;
        try {
            result = jedisCluster.rpush(key, value);
            logger.debug("listAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("listAdd {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#listObjectAdd(java.lang.String, java.lang.Object)
     */
    @Override
    public   long listObjectAdd(String key, Object... value) {
        long result = 0;
        try {
            List<byte[]> list = Lists.newArrayList();
            for (Object o : value){
                list.add(toBytes(o));
            }
            result = jedisCluster.rpush(getBytesKey(key), (byte[][])list.toArray());
            logger.debug("listObjectAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("listObjectAdd {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#getSet(java.lang.String)
     */
    @Override
    public Set<String> getSet(String key) {
        Set<String> value = null;
        try {
            if (jedisCluster.exists(key)) {
                value = jedisCluster.smembers(key);
                logger.debug("getSet {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getSet {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#getObjectSet(java.lang.String)
     */
    @Override
    public   Set<Object> getObjectSet(String key) {
        Set<Object> value = null;
        try {
            if (jedisCluster.exists(getBytesKey(key))) {
                value = Sets.newHashSet();
                Set<byte[]> set = jedisCluster.smembers(getBytesKey(key));
                for (byte[] bs : set){
                    value.add(toObject(bs));
                }
                logger.debug("getObjectSet {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getObjectSet {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setSet(java.lang.String, java.util.Set, int)
     */
    @Override
    public   long setSet(String key, Set<String> value, int cacheSeconds) {
        long result = 0;
        try {
            if (jedisCluster.exists(key)) {
                jedisCluster.del(key);
            }
            result = jedisCluster.sadd(key, (String[])value.toArray());
            if (cacheSeconds != 0) {
                jedisCluster.expire(key, cacheSeconds);
            }
            logger.debug("setSet {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setSet {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setObjectSet(java.lang.String, java.util.Set, int)
     */
    @Override
    public   long setObjectSet(String key, Set<Object> value, int cacheSeconds) {
        long result = 0;
        try {
            if (jedisCluster.exists(getBytesKey(key))) {
                jedisCluster.del(key);
            }
            Set<byte[]> set = Sets.newHashSet();
            for (Object o : value){
                set.add(toBytes(o));
            }
            result = jedisCluster.sadd(getBytesKey(key), (byte[][])set.toArray());
            if (cacheSeconds != 0) {
                jedisCluster.expire(key, cacheSeconds);
            }
            logger.debug("setObjectSet {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setObjectSet {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setSetAdd(java.lang.String, java.lang.String)
     */
    @Override
    public   long setSetAdd(String key, String... value) {
        long result = 0;
        try {
            result = jedisCluster.sadd(key, value);
            logger.debug("setSetAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setSetAdd {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setSetObjectAdd(java.lang.String, java.lang.Object)
     */
    @Override
    public   long setSetObjectAdd(String key, Object... value) {
        long result = 0;
        try {
            Set<byte[]> set = Sets.newHashSet();
            for (Object o : value){
                set.add(toBytes(o));
            }
            result = jedisCluster.rpush(getBytesKey(key), (byte[][])set.toArray());
            logger.debug("setSetObjectAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setSetObjectAdd {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#getMap(java.lang.String)
     */
    @Override
    public Map<String, String> getMap(String key) {
        Map<String, String> value = null;
        try {
            if (jedisCluster.exists(key)) {
                value = jedisCluster.hgetAll(key);
                logger.debug("getMap {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getMap {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#getObjectMap(java.lang.String)
     */
    @Override
    public   Map<String, Object> getObjectMap(String key) {
        Map<String, Object> value = null;
        try {
            if (jedisCluster.exists(getBytesKey(key))) {
                value = Maps.newHashMap();
                Map<byte[], byte[]> map = jedisCluster.hgetAll(getBytesKey(key));
                for (Map.Entry<byte[], byte[]> e : map.entrySet()){
                    value.put(StringUtils.toString(e.getKey()), toObject(e.getValue()));
                }
                logger.debug("getObjectMap {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getObjectMap {} = {}", key, value, e);
        } finally {
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setMap(java.lang.String, java.util.Map, int)
     */
    @Override
    public   String setMap(String key, Map<String, String> value, int cacheSeconds) {
        String result = null;
        try {
            if (jedisCluster.exists(key)) {
                jedisCluster.del(key);
            }
            result = jedisCluster.hmset(key, value);
            if (cacheSeconds != 0) {
                jedisCluster.expire(key, cacheSeconds);
            }
            logger.debug("setMap {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setMap {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#setObjectMap(java.lang.String, java.util.Map, int)
     */
    @Override
    public   String setObjectMap(String key, Map<String, Object> value, int cacheSeconds) {
        String result = null;
        try {
            if (jedisCluster.exists(getBytesKey(key))) {
                jedisCluster.del(key);
            }
            Map<byte[], byte[]> map = Maps.newHashMap();
            for (Map.Entry<String, Object> e : value.entrySet()){
                map.put(getBytesKey(e.getKey()), toBytes(e.getValue()));
            }
            result = jedisCluster.hmset(getBytesKey(key), (Map<byte[], byte[]>)map);
            if (cacheSeconds != 0) {
                jedisCluster.expire(key, cacheSeconds);
            }
            logger.debug("setObjectMap {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setObjectMap {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#mapPut(java.lang.String, java.util.Map)
     */
    @Override
    public   String mapPut(String key, Map<String, String> value) {
        String result = null;
        try {
            result = jedisCluster.hmset(key, value);
            logger.debug("mapPut {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("mapPut {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#mapObjectPut(java.lang.String, java.util.Map)
     */
    @Override
    public   String mapObjectPut(String key, Map<String, Object> value) {
        String result = null;
        try {
            Map<byte[], byte[]> map = Maps.newHashMap();
            for (Map.Entry<String, Object> e : value.entrySet()){
                map.put(getBytesKey(e.getKey()), toBytes(e.getValue()));
            }
            result = jedisCluster.hmset(getBytesKey(key), (Map<byte[], byte[]>)map);
            logger.debug("mapObjectPut {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("mapObjectPut {} = {}", key, value, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#mapRemove(java.lang.String, java.lang.String)
     */
    @Override
    public   long mapRemove(String key, String mapKey) {
        long result = 0;
        try {
            result = jedisCluster.hdel(key, mapKey);
            logger.debug("mapRemove {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapRemove {}  {}", key, mapKey, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#mapObjectRemove(java.lang.String, java.lang.String)
     */
    @Override
    public   long mapObjectRemove(String key, String mapKey) {
        long result = 0;
        try {
            result = jedisCluster.hdel(getBytesKey(key), getBytesKey(mapKey));
            logger.debug("mapObjectRemove {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapObjectRemove {}  {}", key, mapKey, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#mapExists(java.lang.String, java.lang.String)
     */
    @Override
    public   boolean mapExists(String key, String mapKey) {
        boolean result = false;
        try {
            result = jedisCluster.hexists(key, mapKey);
            logger.debug("mapExists {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapExists {}  {}", key, mapKey, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#mapObjectExists(java.lang.String, java.lang.String)
     */
    @Override
    public   boolean mapObjectExists(String key, String mapKey) {
        boolean result = false;
        try {
            result = jedisCluster.hexists(getBytesKey(key), getBytesKey(mapKey));
            logger.debug("mapObjectExists {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapObjectExists {}  {}", key, mapKey, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#del(java.lang.String)
     */
    @Override
    public   long del(String key) {
        long result = 0;
        try {
            if (jedisCluster.exists(key)){
                result = jedisCluster.del(key);
                logger.debug("del {}", key);
            }else{
                logger.debug("del {} not exists", key);
            }
        } catch (Exception e) {
            logger.warn("del {}", key, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#delObject(java.lang.String)
     */
    @Override
    public   long delObject(String key) {
        long result = 0;
        try {
            if (jedisCluster.exists(getBytesKey(key))){
                result = jedisCluster.del(getBytesKey(key));
                logger.debug("delObject {}", key);
            }else{
                logger.debug("delObject {} not exists", key);
            }
        } catch (Exception e) {
            logger.warn("delObject {}", key, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.jeeplus.common.redis.IJedisClient#exists(java.lang.String)
     */
    @Override
    public   boolean exists(String key) {
        boolean result = false;
        try {
            result = jedisCluster.exists(key);
            logger.debug("exists {}", key);
        } catch (Exception e) {
            logger.warn("exists {}", key, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }


    @Override
    public   boolean existsObject(String key) {
        boolean result = false;
        try {
            result = jedisCluster.exists(getBytesKey(key));
            logger.debug("existsObject {}", key);
        } catch (Exception e) {
            logger.warn("existsObject {}", key, e);
        } finally {
//			returnResource(jedis);
        }
        return result;
    }

    @Override
    public void flushlikekey(String key) {
        deleteRedisKeyStartWith(key);
    }

    @Override
    public void flushlikekey(String... keys) {
        try {
            jedisCluster.del(keys);
            logger.debug("existsObject {}", keys);
        } catch (Exception e) {
            logger.warn("existsObject {}", keys, e);
        } finally {

        }


    }

    @Override
    public void flushlikekey_foreach(String key) {
        deleteRedisKeyStartWith(key);
    }

    /**
     * 获取剩余时间 毫秒
     * @param key
     * @return
     */
    @Override
    public long pttl(String key) {
        long time=-1;
        try {
            time=jedisCluster.pttl(key);
            logger.debug("existsObject {}", key);
        } catch (Exception e) {
            logger.warn("existsObject {}", key, e);
        } finally {

        }

        return time;
    }

    /**
     *  获取剩余时间 秒
     * @param key
     * @return
     */
    @Override
    public long ttl(String key) {
        long time=-1;
        try {
            time=jedisCluster.ttl(key);
            logger.debug("existsObject {}", key);
        } catch (Exception e) {
            logger.warn("existsObject {}", key, e);
        } finally {

        }

        return time;
    }


    /**
     * 设置生存时间 秒
     * @param key
     * @param seconds
     * @return
     */
    @Override
    public long expire(String key, int seconds) {
        long status=0;
        try {
            status=jedisCluster.expire(key,seconds);
            logger.debug("existsObject {}", key);
        } catch (Exception e) {
            logger.warn("existsObject {}", key, e);
        } finally {

        }

        return status;
    }

    /**
     * 设置生存时间 秒
     * @param key
     * @param milliseconds
     * @return
     */
    @Override
    public long pexpire(String key, long milliseconds) {
        long status=0;
        try {
            status=jedisCluster.pexpire(key,milliseconds);
            logger.debug("existsObject {}", key);
        } catch (Exception e) {
            logger.warn("existsObject {}", key, e);
        } finally {

        }
        return status;
    }


    /**
     * 获取byte[]类型Key
     * @param
     * @return
     */
    public static  byte[] getBytesKey(Object object){
        if(object instanceof String){
            return StringUtils.getBytes((String)object);
        }else{
            return ObjectUtils.serialize(object);
        }
    }

    /**
     * Object转换byte[]类型
     * @param
     * @return
     */
    public static  byte[] toBytes(Object object){
        return ObjectUtils.serialize(object);
    }

    /**
     * byte[]型转换Object
     * @param
     * @return
     */
    public static Object toObject(byte[] bytes){
        return ObjectUtils.unserialize(bytes);
    }

}
