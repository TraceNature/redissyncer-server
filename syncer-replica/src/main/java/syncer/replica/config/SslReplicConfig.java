package syncer.replica.config;


import syncer.replica.socket.SslContextFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

public class SslReplicConfig {
    private SslReplicConfig() {
    }

    /**
     * factory
     *
     * @return SslConfiguration
     */
    public static SslReplicConfig defaultSetting() {
        return new SslReplicConfig();
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

    public SslReplicConfig setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public SslContextFactory getSslContextFactory() {
        return sslContextFactory;
    }

    public SslReplicConfig setSslContextFactory(SslContextFactory sslContextFactory) {
        this.sslContextFactory = sslContextFactory;
        return this;
    }

    public SSLParameters getSslParameters() {
        return sslParameters;
    }

    public SslReplicConfig setSslParameters(SSLParameters sslParameters) {
        this.sslParameters = sslParameters;
        return this;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public SslReplicConfig setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    @Override
    public String toString() {
        return "SslReplicConfig{" +
                "sslSocketFactory=" + sslSocketFactory +
                ", sslContextFactory=" + sslContextFactory +
                ", sslParameters=" + sslParameters +
                ", hostnameVerifier=" + hostnameVerifier +
                '}';
    }
}
