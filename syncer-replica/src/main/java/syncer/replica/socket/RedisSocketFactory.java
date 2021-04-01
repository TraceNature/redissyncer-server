package syncer.replica.socket;

import syncer.replica.config.ReplicConfig;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.*;

/**
 * socket工厂
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
public class RedisSocketFactory  extends SocketFactory {
    private final ReplicConfig config;

    public RedisSocketFactory(ReplicConfig config) {
        this.config = config;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        if (config.isSsl()) {
            return buildSsl(build(config.getSslSocketFactory().createSocket(host, port)), host);
        } else {
            return build(new Socket(host, port));
        }
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        if (config.isSsl()) {
            return buildSsl(build(config.getSslSocketFactory().createSocket(host, port, localAddr, localPort)), host);
        } else {
            return build(new Socket(host, port, localAddr, localPort));
        }
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        if (config.isSsl()) {
            return buildSsl(build(config.getSslSocketFactory().createSocket(address, port)), address.getHostAddress());
        } else {
            return build(new Socket(address, port));
        }
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        if (config.isSsl()) {
            return buildSsl(build(config.getSslSocketFactory().createSocket(address, port, localAddr, localPort)), address.getHostAddress());
        } else {
            return build(new Socket(address, port, localAddr, localPort));
        }
    }

    public Socket createSocket(String host, int port, int timeout) throws IOException {
        Socket socket = new Socket();
        build(socket);
        socket.connect(new InetSocketAddress(host, port), timeout);
        if (config.isSsl()) {
            socket = config.getSslSocketFactory().createSocket(socket, host, port, true);
            return buildSsl(socket, host);
        } else {
            return socket;
        }
    }

    private Socket build(Socket socket) throws SocketException {
        socket.setReuseAddress(true);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        socket.setSoLinger(true, 0);
        if (config.getReadTimeout() > 0) {
            socket.setSoTimeout(config.getReadTimeout());
        }
        if (config.getReceiveBufferSize() > 0) {
            socket.setReceiveBufferSize(config.getReceiveBufferSize());
        }
        if (config.getSendBufferSize() > 0) {
            socket.setSendBufferSize(config.getSendBufferSize());
        }
        return socket;
    }

    private Socket buildSsl(Socket socket, String host) throws SocketException {
        if (config.getSslParameters() != null) {
            ((SSLSocket) socket).setSSLParameters(config.getSslParameters());
        }
        if (config.getHostnameVerifier() != null && !config.getHostnameVerifier().verify(host, ((SSLSocket) socket).getSession())) {
            throw new SocketException("the connection to " + host + " failed ssl/tls hostname verification.");
        }
        return socket;
    }
}
