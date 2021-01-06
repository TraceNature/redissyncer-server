package syncer.replica.util.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public class ConcurrentUtils {

    public static long sub(long v1, long v2) {
        return max(max(v1, 0) - max(v2, 0), 0);
    }

    public synchronized static long terminateQuietly(ExecutorService exec, long timeout, TimeUnit unit) {
        final long now = System.nanoTime();
        try {
            return terminate(exec, timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final long elapsedTime = System.nanoTime() - now;
            return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
        }
    }

    /**
     * 关闭心跳检测线程池
     * @param exec
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public synchronized static long terminate(ExecutorService exec, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (exec == null) {
            return timeout;
        }
        if (!exec.isShutdown()){
            exec.shutdown();
        }
        if (timeout <= 0) {
            return 0;
        }
        final long now = System.nanoTime();
        exec.awaitTermination(timeout, unit);
        final long elapsedTime = System.nanoTime() - now;
        return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
    }
}
