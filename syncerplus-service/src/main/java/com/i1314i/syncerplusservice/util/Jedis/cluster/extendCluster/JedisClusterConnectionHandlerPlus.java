package com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClusterHostAndPortMap;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * 扩展模块 该模块主要初始化Cache模块
 */
public abstract class JedisClusterConnectionHandlerPlus implements Closeable {
    protected final JedisClusterInfoCachePlus cache;
    private Map<String,String>nodesMap;
    public JedisClusterConnectionHandlerPlus(Set<HostAndPort> nodes,
                                         final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password,Map<String,String>nodesMap) {
        this(nodes, poolConfig, connectionTimeout, soTimeout, password, null,nodesMap);
    }

    public JedisClusterConnectionHandlerPlus(Set<HostAndPort> nodes,
                                         final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password, String clientName,Map<String,String>nodesMap) {
        this(nodes, poolConfig, connectionTimeout, soTimeout, password, clientName, false, null, null, null, null,nodesMap);
    }

    public JedisClusterConnectionHandlerPlus(Set<HostAndPort> nodes,
                                         final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password, String clientName,
                                         boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
                                         HostnameVerifier hostnameVerifier, JedisClusterHostAndPortMap portMap,Map<String,String>nodesMap) {
        this.cache = new JedisClusterInfoCachePlus(poolConfig, connectionTimeout, soTimeout, password, clientName,
                ssl, sslSocketFactory, sslParameters, hostnameVerifier, portMap,nodesMap);
        initializeSlotsCache(nodes, poolConfig, connectionTimeout, soTimeout, password, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier,nodesMap);
    }

    abstract Jedis getConnection();

    abstract Jedis getConnectionFromSlot(int slot);

    public Jedis getConnectionFromNode(HostAndPort node) {
        return cache.setupNodeIfNotExist(node).getResource();
    }

    public Map<String, JedisPool> getNodes() {
        return cache.getNodes();
    }

    private void initializeSlotsCache(Set<HostAndPort> startNodes, GenericObjectPoolConfig poolConfig,
                                      int connectionTimeout, int soTimeout, String password, String clientName,
                                      boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier,Map<String,String>nodesMap) {


        for (HostAndPort hostAndPort : startNodes) {
            Jedis jedis = null;
            try {
//                System.out.println("myDream :"+hostAndPort.getHost());
                jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
                if (password != null) {
                    jedis.auth(password);
                }
                if (clientName != null) {
                    jedis.clientSetname(clientName);
                }
                cache.discoverClusterNodesAndSlots(jedis,nodesMap);
//                break;
            } catch (JedisConnectionException e) {
                // try next nodes
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    public void renewSlotCache() {
        cache.renewClusterSlots(null,null);
    }

    public void renewSlotCache(Jedis jedis,Map<String,String> nodesMap) {
        cache.renewClusterSlots(jedis,nodesMap);
    }

    @Override
    public void close() {
        cache.reset();
    }
}
