package syncerservice.syncerplusservice.util.Jedis;

import syncerservice.syncerplusservice.util.Jedis.pool.JDJedisPoolAbstract;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

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

    public JDJedis(String host, int port) {
        super(host, port);
    }

    public JDJedis(String host, int port, int timeout) {
        super(host, port, timeout);
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

    @Override
    public void close() {
        if (dataSource != null) {
            JDJedisPoolAbstract pool = this.dataSource;
            this.dataSource = null;
            if (client.isBroken()) {
                pool.returnBrokenResource(this);
            } else {
                pool.returnResource(this);
            }
        } else {
            super.close();
        }
//        super.close();
    }
}