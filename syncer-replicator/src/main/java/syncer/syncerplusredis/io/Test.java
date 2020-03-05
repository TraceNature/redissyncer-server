package syncer.syncerplusredis.io;

import java.math.BigDecimal;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/14
 */
public class Test {
    /**
     * byte(字节)根据长度转成kb(千字节)和mb(兆字节)
     *
     * @param bytes
     * @return
     */
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1){
            return (returnValue + "MB");
        }

        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "KB");
    }

    public static void main(String[] args) {
        System.out.println(bytes2kb( 2 * 1024 * 1024));
    }
}
