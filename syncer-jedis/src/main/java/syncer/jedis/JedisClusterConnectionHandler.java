package syncer.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import syncer.jedis.exceptions.JedisConnectionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.util.Map;
import java.util.Set;

/**
 * 扩展模块 该模块主要初始化Cache模块
 */
public abstract class JedisClusterConnectionHandler implements Closeable {
  protected JedisClusterInfoCache cache;
  private Map<String,String>nodesMap;

  /**
   * 扩展
   * @param nodes
   * @param poolConfig
   * @param connectionTimeout
   * @param soTimeout
   * @param password
   * @param nodesMap
   */
  public JedisClusterConnectionHandler(Set<HostAndPort> nodes,
                                           final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password,Map<String,String>nodesMap) {
    this(nodes, poolConfig, connectionTimeout, soTimeout, password, null,nodesMap);
  }
  public JedisClusterConnectionHandler(Set<HostAndPort> nodes,
                                       final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password) {
    this(nodes, poolConfig, connectionTimeout, soTimeout, password, null,true);
  }

  public JedisClusterConnectionHandler(Set<HostAndPort> nodes,
                                       final GenericObjectPoolConfig poolConfig,
                                       int connectionTimeout,
                                       int soTimeout,
                                       String password,
                                       String clientName,boolean status) {
    this(nodes, poolConfig, connectionTimeout, soTimeout, password, clientName, false, null, null, null, null);
  }

  public JedisClusterConnectionHandler(Set<HostAndPort> nodes,
                                       final GenericObjectPoolConfig poolConfig,
                                       int connectionTimeout,
                                       int soTimeout,
                                       String password,
                                       String clientName) {
    this(nodes, poolConfig, connectionTimeout, soTimeout, password, clientName, false, null, null, null, null);
  }


  /**
   * 加入映射表
   * @param nodes
   * @param poolConfig
   * @param connectionTimeout
   * @param soTimeout
   * @param password
   * @param clientName
   * @param nodesMap
   */

  public JedisClusterConnectionHandler(Set<HostAndPort> nodes,
                                           final GenericObjectPoolConfig poolConfig,
                                       int connectionTimeout,
                                       int soTimeout,
                                       String password,
                                       String clientName,
                                       Map<String,String>nodesMap) {
    this(nodes, poolConfig, connectionTimeout, soTimeout, password, clientName, false, null, null, null, null,nodesMap);
  }


  /**
   * 加入映射表
   * @param nodes
   * @param poolConfig
   * @param connectionTimeout
   * @param soTimeout
   * @param password
   * @param clientName
   * @param ssl
   * @param sslSocketFactory
   * @param sslParameters
   * @param hostnameVerifier
   * @param portMap
   * @param nodesMap
   */
  public JedisClusterConnectionHandler(Set<HostAndPort> nodes,
                                           final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password, String clientName,
                                           boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
                                           HostnameVerifier hostnameVerifier, JedisClusterHostAndPortMap portMap,Map<String,String>nodesMap) {
    this.cache = new JedisClusterInfoCache(poolConfig, connectionTimeout, soTimeout, password, clientName,
            ssl, sslSocketFactory, sslParameters, hostnameVerifier, portMap,nodesMap);
    initializeSlotsCache(nodes, poolConfig, connectionTimeout, soTimeout, password, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier,nodesMap);
  }





  public JedisClusterConnectionHandler(Set<HostAndPort> nodes,
      final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password, String clientName,
      boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier, JedisClusterHostAndPortMap portMap) {
    this.cache = new JedisClusterInfoCache(poolConfig, connectionTimeout, soTimeout, password, clientName,
        ssl, sslSocketFactory, sslParameters, hostnameVerifier, portMap);
    initializeSlotsCache(nodes, connectionTimeout, soTimeout, password, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
  }

  abstract Jedis getConnection();

  abstract Jedis getConnectionFromSlot(int slot);

  public Jedis getConnectionFromNode(HostAndPort node) {
    return cache.setupNodeIfNotExist(node).getResource();
  }
  
  public Map<String, JedisPool> getNodes() {
    return cache.getNodes();
  }

  private void initializeSlotsCache(Set<HostAndPort> startNodes,
      int connectionTimeout, int soTimeout, String password, String clientName,
      boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
    for (HostAndPort hostAndPort : startNodes) {
      Jedis jedis = null;
      try {
        jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
        if (password != null) {
          jedis.auth(password);
        }
        if (clientName != null) {
          jedis.clientSetname(clientName);
        }
        cache.discoverClusterNodesAndSlots(jedis);
        break;
      } catch (JedisConnectionException e) {
        // try next nodes
      } finally {
        if (jedis != null) {
          jedis.close();
        }
      }
    }
  }


  private void initializeSlotsCache(Set<HostAndPort> startNodes, GenericObjectPoolConfig poolConfig,
                                    int connectionTimeout, int soTimeout, String password, String clientName,
                                    boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
    for (HostAndPort hostAndPort : startNodes) {
      Jedis jedis = null;
      try {
        jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
        if (password != null) {
          jedis.auth(password);
        }
        if (clientName != null) {
          jedis.clientSetname(clientName);
        }
        cache.discoverClusterNodesAndSlots(jedis);
        break;
      } catch (JedisConnectionException e) {
        // try next nodes
      } finally {
        if (jedis != null) {
          jedis.close();
        }
      }
    }
  }


  /**
   * 扩展映射关系表
   * @param startNodes
   * @param poolConfig
   * @param connectionTimeout
   * @param soTimeout
   * @param password
   * @param clientName
   * @param ssl
   * @param sslSocketFactory
   * @param sslParameters
   * @param hostnameVerifier
   * @param nodesMap
   */
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
    cache.renewClusterSlots(null);
  }

  public void renewSlotCache(Jedis jedis) {
    cache.renewClusterSlots(jedis);
  }

  /**
   * 扩展内外网映射表
   * @param jedis
   * @param nodesMap
   */
  public void renewSlotCache(Jedis jedis,Map<String,String> nodesMap) {
    cache.renewClusterSlots(jedis,nodesMap);
  }

  @Override
  public void close() {
    cache.reset();
  }
}
