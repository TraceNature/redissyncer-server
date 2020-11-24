package syncer.syncerservice.util.common;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/9/17
 */
public class OsUtils {

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getOsSystem() {
        if (isLinux()) {
            return "linux";
        } else if (isWindows()) {
            return "windows";
        } else {
            return "other system";
        }
    }

}
