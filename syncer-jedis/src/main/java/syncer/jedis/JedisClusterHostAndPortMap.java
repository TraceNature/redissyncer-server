package syncer.jedis;

public interface JedisClusterHostAndPortMap {
  HostAndPort getSSLHostAndPort(String host, int port);
}
