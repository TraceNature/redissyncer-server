package syncer.transmission.lock;

/**
 * 分布式锁执行主体
 * @author: Eq Zhan
 * @create: 2021-02-22
 **/
public interface EtcdLockCommandRunner {
    void run();
    String lockName();
    int grant();
}
