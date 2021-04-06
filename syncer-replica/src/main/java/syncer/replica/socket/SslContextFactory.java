package syncer.replica.socket;

import javax.net.ssl.SSLContext;

/**
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
public interface SslContextFactory {
    SSLContext create();
}
