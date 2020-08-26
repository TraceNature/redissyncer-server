package syncer.syncerservice.util.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import syncer.syncerservice.config.ServerConfig;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/17
 */
@Component
public class Montitor {
    @Autowired
    ServerConfig serverConfig;

    public  long jvmMemoryMax(){
        try {

            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return heapMemoryUsage.getMax();
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }


    public  long jvmMemoryUsed(){
        try {
            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return heapMemoryUsage.getUsed();
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }


}
