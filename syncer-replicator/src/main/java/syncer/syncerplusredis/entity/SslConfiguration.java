package syncer.syncerplusredis.entity;

import syncer.syncerplusredis.net.SslContextFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public class SslConfiguration {

    private SslConfiguration() {
    }

    /**
     * factory
     *
     * @return SslConfiguration
     */
    public static SslConfiguration defaultSetting() {
        return new SslConfiguration();
    }

    /**
     * ssl socket factory
     */
    private SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    /**
     * ssl context factory
     */
    private SslContextFactory sslContextFactory;

    /**
     * ssl parameters
     */
    private SSLParameters sslParameters;

    /**
     * hostname verifier
     */
    private HostnameVerifier hostnameVerifier;

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public SslConfiguration setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public SslContextFactory getSslContextFactory() {
        return sslContextFactory;
    }

    public SslConfiguration setSslContextFactory(SslContextFactory sslContextFactory) {
        this.sslContextFactory = sslContextFactory;
        return this;
    }

    public SSLParameters getSslParameters() {
        return sslParameters;
    }

    public SslConfiguration setSslParameters(SSLParameters sslParameters) {
        this.sslParameters = sslParameters;
        return this;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public SslConfiguration setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    @Override
    public String toString() {
        return "SslConfiguration{" +
                "sslSocketFactory=" + sslSocketFactory +
                ", sslContextFactory=" + sslContextFactory +
                ", sslParameters=" + sslParameters +
                ", hostnameVerifier=" + hostnameVerifier +
                '}';
    }
}
