package syncer.transmission.lock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: Eq Zhan
 * @create: 2021-02-23
 **/
@Slf4j
@AllArgsConstructor
public class DefaultEtcdLockCommandRunner implements EtcdLockCommandRunner{
    private String lockName;
    private int grant;
    @Override
    public void run() {
        try {
            log.info("start lock");
            Thread.sleep(100);
            log.info("end lock");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String lockName() {
        return this.lockName;
    }

    @Override
    public int grant() {
        if(this.grant<=0){
            return 30;
        }
        return this.grant;
    }
}
