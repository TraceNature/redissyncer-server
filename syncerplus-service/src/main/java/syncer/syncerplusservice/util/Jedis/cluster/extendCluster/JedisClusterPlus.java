package syncer.syncerplusservice.util.Jedis.cluster.extendCluster;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;
import redis.clients.jedis.commands.JedisClusterCommands;
import redis.clients.jedis.commands.JedisClusterScriptingCommands;
import redis.clients.jedis.commands.MultiKeyJedisClusterCommands;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;
import redis.clients.jedis.util.JedisClusterHashTagUtil;
import redis.clients.jedis.util.KeyMergeUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public  class JedisClusterPlus extends BinaryJedisClusterPlus implements JedisClusterCommands,
        MultiKeyJedisClusterCommands, JedisClusterScriptingCommands {
    private Map<String,String>nodesMap;
    public JedisClusterPlus(HostAndPort node,Map<String,String>nodesMap) {
        this(Collections.singleton(node),nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int timeout,Map<String,String>nodesMap) {
        this(Collections.singleton(node), timeout,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int timeout, int maxAttempts,Map<String,String>nodesMap) {
        this(Collections.singleton(node), timeout, maxAttempts,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        this(Collections.singleton(node), poolConfig,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int timeout, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        this(Collections.singleton(node), timeout, poolConfig,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int timeout, int maxAttempts,
                            final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        this(Collections.singleton(node), timeout, maxAttempts, poolConfig,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int connectionTimeout, int soTimeout,
                            int maxAttempts, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, poolConfig,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int connectionTimeout, int soTimeout,
                            int maxAttempts, String password, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, password, poolConfig,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int connectionTimeout, int soTimeout,
                            int maxAttempts, String password, String clientName, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, password, clientName, poolConfig,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int connectionTimeout, int soTimeout,
                            int maxAttempts, String password, String clientName, final GenericObjectPoolConfig poolConfig,
                            boolean ssl,Map<String,String>nodesMap) {
        this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, password, clientName, poolConfig, ssl,nodesMap);
    }

    public JedisClusterPlus(HostAndPort node, int connectionTimeout, int soTimeout,
                            int maxAttempts, String password, String clientName, final GenericObjectPoolConfig poolConfig,
                            boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
                            HostnameVerifier hostnameVerifier, JedisClusterHostAndPortMap hostAndPortMap,Map<String,String>nodesMap) {
        this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, password, clientName, poolConfig,
                ssl, sslSocketFactory, sslParameters, hostnameVerifier, hostAndPortMap,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> nodes,Map<String,String>nodesMap) {
        this(nodes, DEFAULT_TIMEOUT,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> nodes, int timeout,Map<String,String>nodesMap) {
        this(nodes, timeout, DEFAULT_MAX_ATTEMPTS,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> nodes, int timeout, int maxAttempts,Map<String,String>nodesMap) {
        this(nodes, timeout, maxAttempts, new GenericObjectPoolConfig(),nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> nodes, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        this(nodes, DEFAULT_TIMEOUT, DEFAULT_MAX_ATTEMPTS, poolConfig,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> nodes, int timeout, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        this(nodes, timeout, DEFAULT_MAX_ATTEMPTS, poolConfig,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> jedisClusterNode, int timeout, int maxAttempts,
                            final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        super(jedisClusterNode, timeout, maxAttempts, poolConfig,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout,
                            int maxAttempts, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        super(jedisClusterNode, connectionTimeout, soTimeout, maxAttempts, poolConfig,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout,
                            int maxAttempts, String password, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        super(jedisClusterNode, connectionTimeout, soTimeout, maxAttempts, password, poolConfig,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout,
                            int maxAttempts, String password, String clientName, final GenericObjectPoolConfig poolConfig,Map<String,String>nodesMap) {
        super(jedisClusterNode, connectionTimeout, soTimeout, maxAttempts, password, clientName, poolConfig,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout,
                            int maxAttempts, String password, String clientName, final GenericObjectPoolConfig poolConfig,
                            boolean ssl,Map<String,String>nodesMap) {
        super(jedisClusterNode, connectionTimeout, soTimeout, maxAttempts, password, clientName, poolConfig, ssl,nodesMap);
    }

    public JedisClusterPlus(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout,
                            int maxAttempts, String password, String clientName, final GenericObjectPoolConfig poolConfig,
                            boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
                            HostnameVerifier hostnameVerifier, JedisClusterHostAndPortMap hostAndPortMap,Map<String,String>nodesMap) {
        super(jedisClusterNode, connectionTimeout, soTimeout, maxAttempts, password, clientName, poolConfig,
                ssl, sslSocketFactory, sslParameters, hostnameVerifier, hostAndPortMap,nodesMap);
    }

    @Override
    public String set(final String key, final String value) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.set(key, value);
            }
        }.run(key);
    }

    @Override
    public String set(final String key, final String value, final SetParams params) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.set(key, value, params);
            }
        }.run(key);
    }

    @Override
    public String get(final String key) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.get(key);
            }
        }.run(key);
    }

    @Override
    public Boolean exists(final String key) {
        return new JedisClusterCommandPlus<Boolean>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.exists(key);
            }
        }.run(key);
    }

    @Override
    public Long exists(final String... keys) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.exists(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long persist(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.persist(key);
            }
        }.run(key);
    }

    @Override
    public String type(final String key) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.type(key);
            }
        }.run(key);
    }

    @Override
    public byte[] dump(final String key) {
        return new JedisClusterCommandPlus<byte[]>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.dump(key);
            }
        }.run(key);
    }

    @Override
    public String restore(final String key, final int ttl, final byte[] serializedValue) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.restore(key, ttl, serializedValue);
            }
        }.run(key);
    }

    @Override
    public Long expire(final String key, final int seconds) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.expire(key, seconds);
            }
        }.run(key);
    }

    @Override
    public Long pexpire(final String key, final long milliseconds) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pexpire(key, milliseconds);
            }
        }.run(key);
    }

    @Override
    public Long expireAt(final String key, final long unixTime) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.expireAt(key, unixTime);
            }
        }.run(key);
    }

    @Override
    public Long pexpireAt(final String key, final long millisecondsTimestamp) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pexpireAt(key, millisecondsTimestamp);
            }
        }.run(key);
    }

    @Override
    public Long ttl(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.ttl(key);
            }
        }.run(key);
    }

    @Override
    public Long pttl(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pttl(key);
            }
        }.run(key);
    }

    @Override
    public Long touch(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.touch(key);
            }
        }.run(key);
    }

    @Override
    public Long touch(final String... keys) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.touch(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Boolean setbit(final String key, final long offset, final boolean value) {
        return new JedisClusterCommandPlus<Boolean>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public Boolean setbit(final String key, final long offset, final String value) {
        return new JedisClusterCommandPlus<Boolean>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public Boolean getbit(final String key, final long offset) {
        return new JedisClusterCommandPlus<Boolean>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.getbit(key, offset);
            }
        }.run(key);
    }

    @Override
    public Long setrange(final String key, final long offset, final String value) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.setrange(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public String getrange(final String key, final long startOffset, final long endOffset) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.getrange(key, startOffset, endOffset);
            }
        }.run(key);
    }

    @Override
    public String getSet(final String key, final String value) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.getSet(key, value);
            }
        }.run(key);
    }

    @Override
    public Long setnx(final String key, final String value) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.setnx(key, value);
            }
        }.run(key);
    }

    @Override
    public String setex(final String key, final int seconds, final String value) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.setex(key, seconds, value);
            }
        }.run(key);
    }

    @Override
    public String psetex(final String key, final long milliseconds, final String value) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.psetex(key, milliseconds, value);
            }
        }.run(key);
    }

    @Override
    public Long decrBy(final String key, final long decrement) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.decrBy(key, decrement);
            }
        }.run(key);
    }

    @Override
    public Long decr(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.decr(key);
            }
        }.run(key);
    }

    @Override
    public Long incrBy(final String key, final long increment) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.incrBy(key, increment);
            }
        }.run(key);
    }

    @Override
    public Double incrByFloat(final String key, final double increment) {
        return new JedisClusterCommandPlus<Double>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Double execute(Jedis connection) {
                return connection.incrByFloat(key, increment);
            }
        }.run(key);
    }

    @Override
    public Long incr(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.incr(key);
            }
        }.run(key);
    }

    @Override
    public Long append(final String key, final String value) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.append(key, value);
            }
        }.run(key);
    }

    @Override
    public String substr(final String key, final int start, final int end) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.substr(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Long hset(final String key, final String field, final String value) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hset(key, field, value);
            }
        }.run(key);
    }

    @Override
    public Long hset(final String key, final Map<String, String> hash) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hset(key, hash);
            }
        }.run(key);
    }

    @Override
    public String hget(final String key, final String field) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.hget(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hsetnx(final String key, final String field, final String value) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hsetnx(key, field, value);
            }
        }.run(key);
    }

    @Override
    public String hmset(final String key, final Map<String, String> hash) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.hmset(key, hash);
            }
        }.run(key);
    }

    @Override
    public List<String> hmget(final String key, final String... fields) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.hmget(key, fields);
            }
        }.run(key);
    }

    @Override
    public Long hincrBy(final String key, final String field, final long value) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hincrBy(key, field, value);
            }
        }.run(key);
    }

    @Override
    public Boolean hexists(final String key, final String field) {
        return new JedisClusterCommandPlus<Boolean>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.hexists(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hdel(final String key, final String... field) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hdel(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hlen(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hlen(key);
            }
        }.run(key);
    }

    @Override
    public Set<String> hkeys(final String key) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.hkeys(key);
            }
        }.run(key);
    }

    @Override
    public List<String> hvals(final String key) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.hvals(key);
            }
        }.run(key);
    }

    @Override
    public Map<String, String> hgetAll(final String key) {
        return new JedisClusterCommandPlus<Map<String, String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Map<String, String> execute(Jedis connection) {
                return connection.hgetAll(key);
            }
        }.run(key);
    }

    @Override
    public Long rpush(final String key, final String... string) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.rpush(key, string);
            }
        }.run(key);
    }

    @Override
    public Long lpush(final String key, final String... string) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lpush(key, string);
            }
        }.run(key);
    }

    @Override
    public Long llen(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.llen(key);
            }
        }.run(key);
    }

    @Override
    public List<String> lrange(final String key, final long start, final long stop) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.lrange(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public String ltrim(final String key, final long start, final long stop) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.ltrim(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public String lindex(final String key, final long index) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.lindex(key, index);
            }
        }.run(key);
    }

    @Override
    public String lset(final String key, final long index, final String value) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.lset(key, index, value);
            }
        }.run(key);
    }

    @Override
    public Long lrem(final String key, final long count, final String value) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lrem(key, count, value);
            }
        }.run(key);
    }

    @Override
    public String lpop(final String key) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.lpop(key);
            }
        }.run(key);
    }

    @Override
    public String rpop(final String key) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.rpop(key);
            }
        }.run(key);
    }

    @Override
    public Long sadd(final String key, final String... member) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.sadd(key, member);
            }
        }.run(key);
    }

    @Override
    public Set<String> smembers(final String key) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.smembers(key);
            }
        }.run(key);
    }

    @Override
    public Long srem(final String key, final String... member) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.srem(key, member);
            }
        }.run(key);
    }

    @Override
    public String spop(final String key) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.spop(key);
            }
        }.run(key);
    }

    @Override
    public Set<String> spop(final String key, final long count) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.spop(key, count);
            }
        }.run(key);
    }

    @Override
    public Long scard(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.scard(key);
            }
        }.run(key);
    }

    @Override
    public Boolean sismember(final String key, final String member) {
        return new JedisClusterCommandPlus<Boolean>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.sismember(key, member);
            }
        }.run(key);
    }

    @Override
    public String srandmember(final String key) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.srandmember(key);
            }
        }.run(key);
    }

    @Override
    public List<String> srandmember(final String key, final int count) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.srandmember(key, count);
            }
        }.run(key);
    }

    @Override
    public Long strlen(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.strlen(key);
            }
        }.run(key);
    }

    @Override
    public Long zadd(final String key, final double score, final String member) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zadd(key, score, member);
            }
        }.run(key);
    }

    @Override
    public Long zadd(final String key, final double score, final String member,
                     final ZAddParams params) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zadd(key, score, member, params);
            }
        }.run(key);
    }

    @Override
    public Long zadd(final String key, final Map<String, Double> scoreMembers) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zadd(key, scoreMembers);
            }
        }.run(key);
    }

    @Override
    public Long zadd(final String key, final Map<String, Double> scoreMembers, final ZAddParams params) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zadd(key, scoreMembers, params);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrange(final String key, final long start, final long stop) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrange(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Long zrem(final String key, final String... members) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrem(key, members);
            }
        }.run(key);
    }

    @Override
    public Double zincrby(final String key, final double increment, final String member) {
        return new JedisClusterCommandPlus<Double>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Double execute(Jedis connection) {
                return connection.zincrby(key, increment, member);
            }
        }.run(key);
    }

    @Override
    public Double zincrby(final String key, final double increment, final String member,
                          final ZIncrByParams params) {
        return new JedisClusterCommandPlus<Double>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Double execute(Jedis connection) {
                return connection.zincrby(key, increment, member, params);
            }
        }.run(key);
    }

    @Override
    public Long zrank(final String key, final String member) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrank(key, member);
            }
        }.run(key);
    }

    @Override
    public Long zrevrank(final String key, final String member) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrevrank(key, member);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrange(final String key, final long start, final long stop) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrange(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeWithScores(final String key, final long start, final long stop) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeWithScores(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long stop) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeWithScores(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Long zcard(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcard(key);
            }
        }.run(key);
    }

    @Override
    public Double zscore(final String key, final String member) {
        return new JedisClusterCommandPlus<Double>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Double execute(Jedis connection) {
                return connection.zscore(key, member);
            }
        }.run(key);
    }

    @Override
    public List<String> sort(final String key) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.sort(key);
            }
        }.run(key);
    }

    @Override
    public List<String> sort(final String key, final SortingParams sortingParameters) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.sort(key, sortingParameters);
            }
        }.run(key);
    }

    @Override
    public Long zcount(final String key, final double min, final double max) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long zcount(final String key, final String min, final String max) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max,
                                     final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final String max, final String min) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max,
                                     final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min,
                                        final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max,
                                              final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final String max, final String min,
                                        final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max, final String min) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max,
                                              final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max,
                                                 final double min, final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max,
                                                 final String min, final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByRank(final String key, final long start, final long stop) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByRank(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByScore(final String key, final double min, final double max) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByScore(final String key, final String min, final String max) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long zlexcount(final String key, final String min, final String max) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zlexcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByLex(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max,
                                   final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByLex(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByLex(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min,
                                      final int offset, final int count) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByLex(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByLex(final String key, final String min, final String max) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByLex(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long linsert(final String key, final ListPosition where, final String pivot,
                        final String value) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.linsert(key, where, pivot, value);
            }
        }.run(key);
    }

    @Override
    public Long lpushx(final String key, final String... string) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lpushx(key, string);
            }
        }.run(key);
    }

    @Override
    public Long rpushx(final String key, final String... string) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.rpushx(key, string);
            }
        }.run(key);
    }

    @Override
    public Long del(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.del(key);
            }
        }.run(key);
    }

    @Override
    public Long unlink(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.unlink(key);
            }
        }.run(key);
    }

    @Override
    public Long unlink(final String... keys) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.unlink(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public String echo(final String string) {
        // note that it'll be run from arbitrary node
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.echo(string);
            }
        }.run(string);
    }

    @Override
    public Long bitcount(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.bitcount(key);
            }
        }.run(key);
    }

    @Override
    public Long bitcount(final String key, final long start, final long end) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.bitcount(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Set<String> keys(final String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getSimpleName()
                    + " only supports KEYS commands with non-empty patterns");
        }
        if (!JedisClusterHashTagUtil.isClusterCompliantMatchPattern(pattern)) {
            throw new IllegalArgumentException(this.getClass().getSimpleName()
                    + " only supports KEYS commands with patterns containing hash-tags ( curly-brackets enclosed strings )");
        }
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.keys(pattern);
            }
        }.run(pattern);
    }

    @Override
    public ScanResult<String> scan(final String cursor, final ScanParams params) {

        String matchPattern = null;
        ScanParamsPlus paramsPlus= (ScanParamsPlus) params;
        if (params == null || (matchPattern = paramsPlus.match()) == null || matchPattern.isEmpty()) {
            throw new IllegalArgumentException(JedisCluster.class.getSimpleName()
                    + " only supports SCAN commands with non-empty MATCH patterns");
        }

        if (!JedisClusterHashTagUtil.isClusterCompliantMatchPattern(matchPattern)) {
            throw new IllegalArgumentException(JedisCluster.class.getSimpleName()
                    + " only supports SCAN commands with MATCH patterns containing hash-tags ( curly-brackets enclosed strings )");
        }

        return new JedisClusterCommandPlus<ScanResult<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public ScanResult<String> execute(Jedis connection) {
                return connection.scan(cursor, params);
            }
        }.run(matchPattern);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor) {
        return new JedisClusterCommandPlus<ScanResult<Map.Entry<String, String>>>(connectionHandler,
                maxAttempts,nodesMap) {
            @Override
            public ScanResult<Map.Entry<String, String>> execute(Jedis connection) {
                return connection.hscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<String> sscan(final String key, final String cursor) {
        return new JedisClusterCommandPlus<ScanResult<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public ScanResult<String> execute(Jedis connection) {
                return connection.sscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<Tuple> zscan(final String key, final String cursor) {
        return new JedisClusterCommandPlus<ScanResult<Tuple>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public ScanResult<Tuple> execute(Jedis connection) {
                return connection.zscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public Long pfadd(final String key, final String... elements) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pfadd(key, elements);
            }
        }.run(key);
    }

    @Override
    public long pfcount(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pfcount(key);
            }
        }.run(key);
    }

    @Override
    public List<String> blpop(final int timeout, final String key) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.blpop(timeout, key);
            }
        }.run(key);
    }

    @Override
    public List<String> brpop(final int timeout, final String key) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.brpop(timeout, key);
            }
        }.run(key);
    }

    @Override
    public Long del(final String... keys) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.del(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public List<String> blpop(final int timeout, final String... keys) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.blpop(timeout, keys);
            }
        }.run(keys.length, keys);

    }

    @Override
    public List<String> brpop(final int timeout, final String... keys) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.brpop(timeout, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public List<String> mget(final String... keys) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.mget(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public String mset(final String... keysvalues) {
        String[] keys = new String[keysvalues.length / 2];

        for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
            keys[keyIdx] = keysvalues[keyIdx * 2];
        }

        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.mset(keysvalues);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long msetnx(final String... keysvalues) {
        String[] keys = new String[keysvalues.length / 2];

        for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
            keys[keyIdx] = keysvalues[keyIdx * 2];
        }

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.msetnx(keysvalues);
            }
        }.run(keys.length, keys);
    }

    @Override
    public String rename(final String oldkey, final String newkey) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.rename(oldkey, newkey);
            }
        }.run(2, oldkey, newkey);
    }

    @Override
    public Long renamenx(final String oldkey, final String newkey) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.renamenx(oldkey, newkey);
            }
        }.run(2, oldkey, newkey);
    }

    @Override
    public String rpoplpush(final String srckey, final String dstkey) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.rpoplpush(srckey, dstkey);
            }
        }.run(2, srckey, dstkey);
    }

    @Override
    public Set<String> sdiff(final String... keys) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.sdiff(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long sdiffstore(final String dstkey, final String... keys) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, keys);

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.sdiffstore(dstkey, keys);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public Set<String> sinter(final String... keys) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.sinter(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long sinterstore(final String dstkey, final String... keys) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, keys);

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.sinterstore(dstkey, keys);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public Long smove(final String srckey, final String dstkey, final String member) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.smove(srckey, dstkey, member);
            }
        }.run(2, srckey, dstkey);
    }

    @Override
    public Long sort(final String key, final SortingParams sortingParameters, final String dstkey) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.sort(key, sortingParameters, dstkey);
            }
        }.run(2, key, dstkey);
    }

    @Override
    public Long sort(final String key, final String dstkey) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.sort(key, dstkey);
            }
        }.run(2, key, dstkey);
    }

    @Override
    public Set<String> sunion(final String... keys) {
        return new JedisClusterCommandPlus<Set<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.sunion(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long sunionstore(final String dstkey, final String... keys) {
        String[] wholeKeys = KeyMergeUtil.merge(dstkey, keys);

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.sunionstore(dstkey, keys);
            }
        }.run(wholeKeys.length, wholeKeys);
    }

    @Override
    public Long zinterstore(final String dstkey, final String... sets) {
        String[] wholeKeys = KeyMergeUtil.merge(dstkey, sets);

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zinterstore(dstkey, sets);
            }
        }.run(wholeKeys.length, wholeKeys);
    }

    @Override
    public Long zinterstore(final String dstkey, final ZParams params, final String... sets) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, sets);

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zinterstore(dstkey, params, sets);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public Long zunionstore(final String dstkey, final String... sets) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, sets);

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zunionstore(dstkey, sets);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public Long zunionstore(final String dstkey, final ZParams params, final String... sets) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, sets);

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zunionstore(dstkey, params, sets);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public String brpoplpush(final String source, final String destination, final int timeout) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.brpoplpush(source, destination, timeout);
            }
        }.run(2, source, destination);
    }

    @Override
    public Long publish(final String channel, final String message) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.publish(channel, message);
            }
        }.runWithAnyNode();
    }

    @Override
    public void subscribe(final JedisPubSub jedisPubSub, final String... channels) {
        new JedisClusterCommandPlus<Integer>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Integer execute(Jedis connection) {
                connection.subscribe(jedisPubSub, channels);
                return 0;
            }
        }.runWithAnyNode();
    }

    @Override
    public void psubscribe(final JedisPubSub jedisPubSub, final String... patterns) {
        new JedisClusterCommandPlus<Integer>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Integer execute(Jedis connection) {
                connection.psubscribe(jedisPubSub, patterns);
                return 0;
            }
        }.runWithAnyNode();
    }

    @Override
    public Long bitop(final BitOP op, final String destKey, final String... srcKeys) {
        String[] mergedKeys = KeyMergeUtil.merge(destKey, srcKeys);

        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.bitop(op, destKey, srcKeys);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public String pfmerge(final String destkey, final String... sourcekeys) {
        String[] mergedKeys = KeyMergeUtil.merge(destkey, sourcekeys);

        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.pfmerge(destkey, sourcekeys);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public long pfcount(final String... keys) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pfcount(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Object eval(final String script, final int keyCount, final String... params) {
        return new JedisClusterCommandPlus<Object>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Object execute(Jedis connection) {
                return connection.eval(script, keyCount, params);
            }
        }.run(keyCount, params);
    }

    @Override
    public Object eval(final String script, final String sampleKey) {
        return new JedisClusterCommandPlus<Object>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Object execute(Jedis connection) {
                return connection.eval(script);
            }
        }.run(sampleKey);
    }

    @Override
    public Object eval(final String script, final List<String> keys, final List<String> args) {
        return new JedisClusterCommandPlus<Object>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Object execute(Jedis connection) {
                return connection.eval(script, keys, args);
            }
        }.run(keys.size(), keys.toArray(new String[keys.size()]));
    }

    @Override
    public Object evalsha(final String sha1, final int keyCount, final String... params) {
        return new JedisClusterCommandPlus<Object>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Object execute(Jedis connection) {
                return connection.evalsha(sha1, keyCount, params);
            }
        }.run(keyCount, params);
    }

    @Override
    public Object evalsha(final String sha1, final List<String> keys, final List<String> args) {
        return new JedisClusterCommandPlus<Object>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Object execute(Jedis connection) {
                return connection.evalsha(sha1, keys, args);
            }
        }.run(keys.size(), keys.toArray(new String[keys.size()]));
    }

    @Override
    public Object evalsha(final String sha1, final String sampleKey) {
        return new JedisClusterCommandPlus<Object>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Object execute(Jedis connection) {
                return connection.evalsha(sha1);
            }
        }.run(sampleKey);
    }

    @Override
    public Boolean scriptExists(final String sha1, final String sampleKey) {
        return new JedisClusterCommandPlus<Boolean>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.scriptExists(sha1);
            }
        }.run(sampleKey);
    }

    @Override
    public List<Boolean> scriptExists(final String sampleKey, final String... sha1) {
        return new JedisClusterCommandPlus<List<Boolean>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<Boolean> execute(Jedis connection) {
                return connection.scriptExists(sha1);
            }
        }.run(sampleKey);
    }

    @Override
    public String scriptLoad(final String script, final String sampleKey) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.scriptLoad(script);
            }
        }.run(sampleKey);
    }

    @Override
    public String scriptFlush(final String sampleKey) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.scriptFlush();
            }
        }.run(sampleKey);
    }

    @Override
    public String scriptKill(final String sampleKey) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.scriptKill();
            }
        }.run(sampleKey);
    }

    @Override
    public Long geoadd(final String key, final double longitude, final double latitude,
                       final String member) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.geoadd(key, longitude, latitude, member);
            }
        }.run(key);
    }

    @Override
    public Long geoadd(final String key, final Map<String, GeoCoordinate> memberCoordinateMap) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.geoadd(key, memberCoordinateMap);
            }
        }.run(key);
    }

    @Override
    public Double geodist(final String key, final String member1, final String member2) {
        return new JedisClusterCommandPlus<Double>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Double execute(Jedis connection) {
                return connection.geodist(key, member1, member2);
            }
        }.run(key);
    }

    @Override
    public Double geodist(final String key, final String member1, final String member2,
                          final GeoUnit unit) {
        return new JedisClusterCommandPlus<Double>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Double execute(Jedis connection) {
                return connection.geodist(key, member1, member2, unit);
            }
        }.run(key);
    }

    @Override
    public List<String> geohash(final String key, final String... members) {
        return new JedisClusterCommandPlus<List<String>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.geohash(key, members);
            }
        }.run(key);
    }

    @Override
    public List<GeoCoordinate> geopos(final String key, final String... members) {
        return new JedisClusterCommandPlus<List<GeoCoordinate>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoCoordinate> execute(Jedis connection) {
                return connection.geopos(key, members);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadius(final String key, final double longitude,
                                             final double latitude, final double radius, final GeoUnit unit) {
        return new JedisClusterCommandPlus<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoRadiusResponse> execute(Jedis connection) {
                return connection.georadius(key, longitude, latitude, radius, unit);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final String key, final double longitude,
                                                     final double latitude, final double radius, final GeoUnit unit) {
        return new JedisClusterCommandPlus<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoRadiusResponse> execute(Jedis connection) {
                return connection.georadiusReadonly(key, longitude, latitude, radius, unit);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadius(final String key, final double longitude,
                                             final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new JedisClusterCommandPlus<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoRadiusResponse> execute(Jedis connection) {
                return connection.georadius(key, longitude, latitude, radius, unit, param);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final String key, final double longitude,
                                                     final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new JedisClusterCommandPlus<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoRadiusResponse> execute(Jedis connection) {
                return connection.georadiusReadonly(key, longitude, latitude, radius, unit, param);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final String key, final String member,
                                                     final double radius, final GeoUnit unit) {
        return new JedisClusterCommandPlus<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoRadiusResponse> execute(Jedis connection) {
                return connection.georadiusByMember(key, member, radius, unit);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final String key, final String member,
                                                             final double radius, final GeoUnit unit) {
        return new JedisClusterCommandPlus<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoRadiusResponse> execute(Jedis connection) {
                return connection.georadiusByMemberReadonly(key, member, radius, unit);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final String key, final String member,
                                                     final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new JedisClusterCommandPlus<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoRadiusResponse> execute(Jedis connection) {
                return connection.georadiusByMember(key, member, radius, unit, param);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final String key, final String member,
                                                             final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new JedisClusterCommandPlus<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<GeoRadiusResponse> execute(Jedis connection) {
                return connection.georadiusByMemberReadonly(key, member, radius, unit, param);
            }
        }.run(key);
    }

    @Override
    public List<Long> bitfield(final String key, final String... arguments) {
        return new JedisClusterCommandPlus<List<Long>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<Long> execute(Jedis connection) {
                return connection.bitfield(key, arguments);
            }
        }.run(key);
    }

    @Override
    public Long hstrlen(final String key, final String field) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hstrlen(key, field);
            }
        }.run(key);
    }

    @Override
    public StreamEntryID xadd(final String key, final StreamEntryID id, final Map<String, String> hash) {
        return new JedisClusterCommandPlus<StreamEntryID>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public StreamEntryID execute(Jedis connection) {
                return connection.xadd(key, id, hash);
            }
        }.run(key);
    }

    @Override
    public StreamEntryID xadd(final String key, final StreamEntryID id, final Map<String, String> hash, final long maxLen, final boolean approximateLength) {
        return new JedisClusterCommandPlus<StreamEntryID>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public StreamEntryID execute(Jedis connection) {
                return connection.xadd(key, id, hash, maxLen, approximateLength);
            }
        }.run(key);
    }

    @Override
    public Long xlen(final String key) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.xlen(key);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xrange(final String key, final StreamEntryID start, final StreamEntryID end, final int count) {
        return new JedisClusterCommandPlus<List<StreamEntry>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<StreamEntry> execute(Jedis connection) {
                return connection.xrange(key, start, end, count);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xrevrange(final String key, final StreamEntryID end, final StreamEntryID start, final int count) {
        return new JedisClusterCommandPlus<List<StreamEntry>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<StreamEntry> execute(Jedis connection) {
                return connection.xrevrange(key, end, start, count);
            }
        }.run(key);
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xread(final int count, final long block, final Map.Entry<String, StreamEntryID>... streams) {
        String[] keys = new String[streams.length];
        for(int i=0; i<streams.length; ++i) {
            keys[i] = streams[i].getKey();
        }

        return new JedisClusterCommandPlus<List<Map.Entry<String, List<StreamEntry>>>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<Map.Entry<String, List<StreamEntry>>> execute(Jedis connection) {
                return connection.xread(count, block, streams);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long xack(final String key, final String group, final StreamEntryID... ids) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.xack(key, group, ids);
            }
        }.run(key);
    }

    @Override
    public String xgroupCreate(final String key, final String groupname, final StreamEntryID id, final boolean makeStream) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.xgroupCreate(key, groupname, id, makeStream);
            }
        }.run(key);
    }

    @Override
    public String xgroupSetID(final String key, final String groupname, final StreamEntryID id) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.xgroupSetID(key, groupname, id);
            }
        }.run(key);
    }

    @Override
    public Long xgroupDestroy(final String key, final String groupname) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.xgroupDestroy(key, groupname);
            }
        }.run(key);
    }

    @Override
    public String xgroupDelConsumer(final String key, final String groupname, final String consumername) {
        return new JedisClusterCommandPlus<String>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public String execute(Jedis connection) {
                return connection.xgroupDelConsumer(key, groupname, consumername);
            }
        }.run(key);
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xreadGroup(final String groupname, final String consumer, final int count, final long block,
                                                                 final boolean noAck, final Map.Entry<String, StreamEntryID>... streams) {

        String[] keys = new String[streams.length];
        for(int i=0; i<streams.length; ++i) {
            keys[i] = streams[i].getKey();
        }

        return new JedisClusterCommandPlus<List<Map.Entry<String, List<StreamEntry>>>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<Map.Entry<String, List<StreamEntry>>> execute(Jedis connection) {
                return connection.xreadGroup(groupname, consumer, count, block, noAck, streams);
            }
        }.run(keys.length, keys);
    }

    @Override
    public List<StreamPendingEntry> xpending(final String key, final String groupname, final StreamEntryID start, final StreamEntryID end, final int count,
                                             final String consumername) {
        return new JedisClusterCommandPlus<List<StreamPendingEntry>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<StreamPendingEntry> execute(Jedis connection) {
                return connection.xpending(key, groupname, start, end, count, consumername);
            }
        }.run(key);
    }

    @Override
    public Long xdel(final String key, final StreamEntryID... ids) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.xdel(key, ids);
            }
        }.run(key);
    }

    @Override
    public Long xtrim(final  String key, final long maxLen, final boolean approximateLength) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.xtrim(key, maxLen, approximateLength);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xclaim(final String key, final String group, final String consumername, final long minIdleTime, final long newIdleTime,
                                    final int retries, final boolean force, final StreamEntryID... ids) {
        return new JedisClusterCommandPlus<List<StreamEntry>>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public List<StreamEntry> execute(Jedis connection) {
                return connection.xclaim(key, group, consumername, minIdleTime, newIdleTime, retries, force, ids);
            }
        }.run(key);
    }

    public Long waitReplicas(final String key, final int replicas, final long timeout) {
        return new JedisClusterCommandPlus<Long>(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Long execute(Jedis connection) {
                return connection.waitReplicas(replicas, timeout);
            }
        }.run(key);
    }

    public Object sendCommand(final String sampleKey, final ProtocolCommand cmd, final String... args) {
        return new JedisClusterCommandPlus(connectionHandler, maxAttempts,nodesMap) {
            @Override
            public Object execute(Jedis connection){


                return connection.sendCommand(cmd, args);
            }
        }.run(sampleKey);
    }


}
