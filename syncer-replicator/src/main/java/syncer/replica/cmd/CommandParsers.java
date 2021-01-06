package syncer.replica.cmd;

import syncer.replica.util.objectutil.Strings;

import java.math.BigDecimal;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/10
 */
public class CommandParsers {

    public static byte[] toBytes(Object object) {
        return (byte[]) object;
    }

    public static String toRune(Object object) {
        return Strings.toString(object);
    }

    public static double toDouble(Object object) {
        return Double.parseDouble(toRune(object));
    }

    public static int toInt(Object object) {
        return new BigDecimal(toRune(object)).intValueExact();
    }

    public static long toLong(Object object) {
        return new BigDecimal(toRune(object)).longValueExact();
    }

}
