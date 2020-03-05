package syncer.syncerpluscommon.util.os;

import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/15
 */

@Slf4j
public class SystemUsageUtils {
    private static SystemInfo systemInfo = new SystemInfo();

    /**
     * 获取内存的使用率
     *
     * @return 内存使用率 0.36
     */
    public static double getMemoryUsage() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        GlobalMemory memory = hal.getMemory();
        long available = memory.getAvailable();
        long total = memory.getTotal();
        log.info("getMemoryUsage available={},total={}", available, total);
        double useRate = NumberUtil.div(available, total, 2);
        return useRate;
    }

    /**
     * 获取CPU的使用率
     *
     * @return CPU使用率 0.36
     */
    public static double getCpuUsage() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        CentralProcessor processor = hal.getProcessor();
        double useRate = processor.getSystemCpuLoadBetweenTicks();
        log.info("getCpuUsage useRate={}", useRate);
        return NumberUtil.div(useRate, 1, 2);
    }

    /**
     * 获取磁盘的使用率
     *
     * @return CPU使用率 0.36
     */
    public static double getDiskUsage() {
        if (isWindows()) {
            return getWinDiskUsage();
        }
        return getUnixDiskUsage();
    }


    /**
     * 判断系统是否为windows
     *
     * @return 是否
     */
    private static boolean isWindows() {
        return System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");
    }

    /**
     * 获取linux 磁盘使用率
     *
     * @return 磁盘使用率
     */
    private static double getUnixDiskUsage() {
        String ioCmdStr = "df -h /";
        String resultInfo = runCommand(ioCmdStr);
        String[] data = resultInfo.split(" +");
        double total = Double.parseDouble(data[10].replace("%", ""));
        return total / 100;
    }

    /**
     * 获取linux 磁盘使用率
     *
     * @return 磁盘使用率
     */
    private static double getWinDiskUsage() {

        HardwareAbstractionLayer hal = systemInfo.getHardware();
        HWDiskStore[] diskStores = hal.getDiskStores();
        long total = 0;
        long used = 0;
        if (diskStores != null && diskStores.length > 0) {
            for (HWDiskStore diskStore : diskStores) {
                long size = diskStore.getSize();
                long writeBytes = diskStore.getWriteBytes();
                total += size;
                used += writeBytes;
            }
        }
        return NumberUtil.div(used, total, 2);
    }


    /**
     * 执行系统命令
     *
     * @param CMD 命令
     * @return 字符串结果
     */
    private static String runCommand(String CMD) {
        StringBuilder info = new StringBuilder();
        try {
            Process pos = Runtime.getRuntime().exec(CMD);
            pos.waitFor();
            InputStreamReader isr = new InputStreamReader(pos.getInputStream());
            LineNumberReader lnr = new LineNumberReader(isr);
            String line;
            while ((line = lnr.readLine()) != null) {
                info.append(line).append("\n");
            }
        } catch (Exception e) {
            info = new StringBuilder(e.toString());
        }
        return info.toString();
    }

}
