package syncer.syncerplusredis.sentinel;


import syncer.syncerjedis.HostAndPort;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/25
 */
public interface SentinelListener {

    void onClose(Sentinel sentinel);

    void onSwitch(Sentinel sentinel, HostAndPort host);

}
