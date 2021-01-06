package syncer.common.montitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import syncer.common.config.ServerConfig;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
@Component
public class Montitor {
    public final static double DEFAULT_THRESHOLD=8.0;
    @Autowired
    ServerConfig serverConfig;

    public long jvmMemoryMax() {
        try {

            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return heapMemoryUsage.getMax();
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }


    public long jvmMemoryUsed() {
        try {
            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return heapMemoryUsage.getUsed();
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }


    /**
     * 是否高于阈值
     *
     * @param threshold
     * @return
     */
    public boolean isAboveThreshold(double threshold) {
        double montitors = new BigDecimal((float) jvmMemoryUsed() / jvmMemoryMax()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (montitors >= threshold) {
            return true;
        }
        return false;
    }

}
