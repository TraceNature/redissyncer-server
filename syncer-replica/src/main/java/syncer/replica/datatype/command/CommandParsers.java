package syncer.replica.datatype.command;

import syncer.replica.util.strings.Strings;

import java.math.BigDecimal;

/**
 * @author: Eq Zhan
 * @create: 2021-03-19
 **/
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

    public static float toFloat(Object object) {
        return Float.parseFloat(toRune(object));
    }

    public static int toInt(Object object) {
        return new BigDecimal(toRune(object)).intValueExact();
    }

    public static long toLong(Object object) {
        return new BigDecimal(toRune(object)).longValueExact();
    }

}
