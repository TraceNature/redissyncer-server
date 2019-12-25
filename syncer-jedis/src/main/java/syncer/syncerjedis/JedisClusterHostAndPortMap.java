package syncer.syncerjedis;

public interface JedisClusterHostAndPortMap {
  HostAndPort getSSLHostAndPort(String host, int port);
}
