// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.
package syncer.common.montitor;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class Montitor {
    public final static double DEFAULT_THRESHOLD=8.0;
    @Autowired
    ServerConfig serverConfig;

    public long jvmMemoryMax() {
        try {

            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return heapMemoryUsage.getMax();
        } catch (Exception e) {
            log.error("get heapMemoryUsage fail {}",e.getMessage());
            return -1L;
        }
    }


    public long jvmMemoryUsed() {
        try {
            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return heapMemoryUsage.getUsed();
        } catch (Exception e) {
            log.error("get jvmMemoryMax fail {}",e.getMessage());
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
        double montitors = new BigDecimal((double) jvmMemoryUsed() / jvmMemoryMax()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (montitors >= threshold) {
            return true;
        }
        return false;
    }

}
