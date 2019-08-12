package com.i1314i.syncerplusservice.util.Jedis;

import com.i1314i.syncerplusservice.util.Jedis.pool.JDJedisPoolAbstract;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolAbstract;
import redis.clients.jedis.JedisShardInfo;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

/**
 * Jedis扩展类，用于多线程模式下记录链接的当前db值
 * 仅适用于本项目
 */
public class JDJedis extends Jedis {
    @Setter@Getter
    private Long dbNum=0L;
    protected JDJedisPoolAbstract dataSource = null;

    public JDJedis(String host, int port, int connectionTimeout, int soTimeout, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, connectionTimeout, soTimeout, ssl, sslSocketFactory,sslParameters,hostnameVerifier);
    }

    @Override
    public String select(final int index) {
        checkIsInMultiOrPipeline();
        client.select(index);
        String statusCodeReply = client.getStatusCodeReply();
        client.setDb(index);
        this.dbNum= Long.valueOf(index);
        return statusCodeReply;
    }

    public void setDataSource(JDJedisPoolAbstract jedisPool) {
        this.dataSource = jedisPool;
    }
}