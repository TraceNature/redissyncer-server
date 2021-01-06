package syncer.replica.sentinel;

import syncer.jedis.HostAndPort;

/**
 * @author zhanenqiang
 * @Description 哨兵
 * @Date 2020/8/14
 */
public interface SentinelListener {

    void onClose(Sentinel sentinel);

    void onSwitch(Sentinel sentinel, HostAndPort host);
}
