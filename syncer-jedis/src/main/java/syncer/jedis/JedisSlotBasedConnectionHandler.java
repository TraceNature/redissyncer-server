package syncer.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import syncer.jedis.exceptions.JedisException;
import syncer.jedis.exceptions.JedisNoReachableClusterNodeException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JedisSlotBasedConnectionHandler extends JedisClusterConnectionHandler {


  /**
   * 扩展映射关系表
   * @param nodes
   * @param poolConfig
   * @param timeout
   * @param nodesMap
   */
  public JedisSlotBasedConnectionHandler(Set<HostAndPort> nodes,
                                             final GenericObjectPoolConfig poolConfig, int timeout, Map<String,String> nodesMap) {
    this(nodes, poolConfig, timeout, timeout,nodesMap);
  }

  public JedisSlotBasedConnectionHandler(Set<HostAndPort> nodes,
                                         final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout) {
    super(nodes, poolConfig, connectionTimeout, soTimeout, null);
  }

  /**
   * 扩展映射关系表
   * @param nodes
   * @param poolConfig
   * @param connectionTimeout
   * @param soTimeout
   * @param nodesMap
   */

  public JedisSlotBasedConnectionHandler(Set<HostAndPort> nodes,
                                             final GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout,Map<String,String>nodesMap) {
    super(nodes, poolConfig, connectionTimeout, soTimeout, null,nodesMap);
  }


  public JedisSlotBasedConnectionHandler(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password,Map<String,String>nodesMap) {
    super(nodes, poolConfig, connectionTimeout, soTimeout, password,nodesMap);
  }



  public JedisSlotBasedConnectionHandler(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password, String clientName) {
    super(nodes, poolConfig, connectionTimeout, soTimeout, password, clientName);
  }

  public JedisSlotBasedConnectionHandler(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig, int connectionTimeout, int soTimeout, String password, String clientName,
                                         boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier, JedisClusterHostAndPortMap portMap) {
    super(nodes, poolConfig, connectionTimeout, soTimeout, password, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier, portMap);
  }

  @Override
  public Jedis getConnection() {
    // In antirez's redis-rb-cluster implementation,
    // getRandomConnection always return valid connection (able to
    // ping-pong)
    // or exception if all connections are invalid

    List<JedisPool> pools = cache.getShuffledNodesPool();

    for (JedisPool pool : pools) {
      Jedis jedis = null;
      try {
        jedis = pool.getResource();

        if (jedis == null) {
          continue;
        }

        String result = jedis.ping();

        if (result.equalsIgnoreCase("pong")){
          return jedis;
        }

        jedis.close();
      } catch (JedisException ex) {
        if (jedis != null) {
          jedis.close();
        }
      }
    }

    throw new JedisNoReachableClusterNodeException("No reachable node in cluster");
  }

  @Override
  public Jedis getConnectionFromSlot(int slot) {
    JedisPool connectionPool = cache.getSlotPool(slot);
    if (connectionPool != null) {
      // It can't guaranteed to get valid connection because of node
      // assignment
      return connectionPool.getResource();
    } else {
      renewSlotCache(); //It's abnormal situation for cluster mode, that we have just nothing for slot, try to rediscover state
      connectionPool = cache.getSlotPool(slot);
      if (connectionPool != null) {
        return connectionPool.getResource();
      } else {
        //no choice, fallback to new connection to random node
        return getConnection();
      }
    }
  }
}
