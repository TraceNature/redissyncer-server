package syncer.syncerplusservice.util.Jedis.cluster.pipelineCluster;


import syncer.syncerplusservice.util.Jedis.cluster.extendCluster.*;
import syncer.syncerplusservice.util.Jedis.cluster.extendCluster.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import redis.clients.jedis.exceptions.JedisMovedDataException;
import redis.clients.jedis.exceptions.JedisRedirectionException;
import redis.clients.jedis.util.JedisClusterCRC16;
import redis.clients.jedis.util.SafeEncoder;
import syncer.syncerplusservice.util.Jedis.cluster.extendCluster.*;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.*;

public class JedisClusterPipeline extends PipelineBase implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisClusterPipeline.class);
    private static final String SPLIT_WORD = ":";

    // 部分字段没有对应的获取方法，只能采用反射来做
    // 你也可以去继承JedisCluster和JedisSlotBasedConnectionHandler来提供访问接口
    private static final Field FIELD_CONNECTION_HANDLER;
    private static final Field FIELD_CACHE;

    static {
        FIELD_CONNECTION_HANDLER = getField(BinaryJedisClusterPlus.class, "connectionHandler");
        FIELD_CACHE = getField(JedisClusterConnectionHandlerPlus.class, "cache");
    }

    private JedisSlotBasedConnectionHandlerPlus connectionHandler;
    private JedisClusterInfoCachePlus clusterInfoCache;
    private Queue<Client> clients = new LinkedList<Client>();   // 根据顺序存储每个命令对应的Client
    private Map<JedisPool, Map<Long, Jedis>> jedisMap = new HashMap<JedisPool, Map<Long, Jedis>>();   // 用于缓存连接
    private boolean hasDataInBuf = false;   // 是否有数据在缓存区


    public JedisClusterPipeline(JedisClusterPlus jedisCluster) {
        setJedisCluster(jedisCluster);
    }


    /**
     * 刷新集群信息，当集群信息发生变更时调用
     *
     * @param
     * @return
     */
    public void refreshCluster() {
        connectionHandler.renewSlotCache();
    }

    /**
     * 同步读取所有数据. 与syncAndReturnAll()相比，sync()只是没有对数据做反序列化
     */
    public void sync() {
        innerSync(null);
    }

    @Override
    public void close() {
        clean();
        clients.clear();
        for (Map.Entry<JedisPool, Map<Long, Jedis>> poolEntry : jedisMap.entrySet()) {
            for (Map.Entry<Long, Jedis> jedisEntry : poolEntry.getValue().entrySet()) {
                if (hasDataInBuf) {
                    flushCachedData(jedisEntry.getValue());
                }
                jedisEntry.getValue().close();
            }
        }
        jedisMap.clear();
        hasDataInBuf = false;
    }

    /**
     * 同步读取所有数据 并按命令顺序返回一个列表
     *
     * @return 按照命令的顺序返回所有的数据
     */
    public List<Object> syncAndReturnAll() {
        List<Object> responseList = new ArrayList<Object>();
        innerSync(responseList);
        return responseList;
    }

    private void setJedisCluster(JedisClusterPlus jedis) {
        connectionHandler = getValue(jedis, FIELD_CONNECTION_HANDLER);
        clusterInfoCache = getValue(connectionHandler, FIELD_CACHE);
    }

    private void innerSync(List<Object> formatted) {
        HashSet<Client> clientSet = new HashSet<Client>();
        try {
            for (Client client : clients) {
                // 在sync()调用时其实是不需要解析结果数据的，但是如果不调用get方法，发生了JedisMovedDataException这样的错误应用是不知道的，因此需要调用get()来触发错误。
                // 其实如果Response的data属性可以直接获取，可以省掉解析数据的时间，然而它并没有提供对应方法，要获取data属性就得用反射，不想再反射了，所以就这样了
                Object data = generateResponse(client.getOne()).get();
                if (null != formatted) {
                    formatted.add(data);
                }
                // size相同说明所有的client都已经添加，就不用再调用add方法了
                if (clientSet.size() != jedisMap.size()) {
                    clientSet.add(client);
                }
            }
        } catch (JedisRedirectionException jre) {
            if (jre instanceof JedisMovedDataException) {
                // if MOVED redirection occurred, rebuilds cluster's slot cache,
                // recommended by Redis cluster specification
                refreshCluster();
            }
            throw jre;
        } finally {
            if (clientSet.size() != jedisMap.size()) {
                // 所有还没有执行过的client要保证执行(flush)，防止放回连接池后后面的命令被污染
                for (Map.Entry<JedisPool, Map<Long, Jedis>> poolEntry : jedisMap.entrySet()) {
                    for (Map.Entry<Long, Jedis> jedisEntry : poolEntry.getValue().entrySet()) {
                        if (clientSet.contains(jedisEntry.getValue().getClient())) {
                            continue;
                        }
                        flushCachedData(jedisEntry.getValue());
                    }
                }
            }
            hasDataInBuf = false;
            close();
        }
    }

    private void flushCachedData(Jedis jedis) {
        try {
//            jedis.getClient().
            jedis.getClient().getMultiBulkReply();
//            jedis.getClient().getOne();
//            jedis.getClient().getAll();
        } catch (RuntimeException ex) {
        }
    }

    @Override
    protected Client getClient(String key) {
        byte[] bKey = SafeEncoder.encode(key);
        return getClient(bKey);
    }

    @Override
    protected Client getClient(byte[] key) {
        Jedis jedis = getJedis(JedisClusterCRC16.getSlot(key));
        Client client = jedis.getClient();
        clients.add(client);
        return client;
    }

    private Jedis getJedis(int slot) {

        // 根据线程id从缓存中获取Jedis
        Jedis jedis = null;
        Map<Long, Jedis> tmpMap = null;
        //获取线程id
        long id = Thread.currentThread().getId();
        //获取jedispool
        JedisPool pool = clusterInfoCache.getSlotPool(slot);

        if (jedisMap.containsKey(pool)) {
            tmpMap = jedisMap.get(pool);
            if (tmpMap.containsKey(id)) {
                jedis = tmpMap.get(id);
            } else {
                jedis = pool.getResource();
                tmpMap.put(id, jedis);
            }
        } else {
            tmpMap = new HashMap<Long, Jedis>();
            jedis = pool.getResource();
            tmpMap.put(id, jedis);
            jedisMap.put(pool,tmpMap);
        }
        hasDataInBuf = true;
        return jedis;
    }

    private static Field getField(Class<?> cls, String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException("cannot find or access field '" + fieldName + "' from " + cls.getName(), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private static <T> T getValue(Object obj, Field field) {
        try {
            return (T) field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.error("get value fail", e);
            throw new RuntimeException(e);
        }
    }


}