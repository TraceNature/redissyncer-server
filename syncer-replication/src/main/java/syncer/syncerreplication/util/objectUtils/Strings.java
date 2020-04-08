package syncer.syncerreplication.util.objectUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
public class Strings {

    public static String toString(Object object) {
        return toString(object, UTF_8);
    }

    public static String toString(Object object, Charset charset) {
        if (object == null) {
            return null;
        }
        return new String((byte[]) object, charset);
    }

    public static boolean isEquals(String o1, String o2) {
        return isEquals(o1, o2, false);
    }

    public static boolean isEquals(String o1, String o2, boolean strict) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        return strict ? o1.equals(o2) : o1.equalsIgnoreCase(o2);
    }

    public static String format(Object[] command) {
        return Arrays.deepToString(command, "[", "]", " ");
    }

    public static ByteBuffer encode(CharBuffer buffer) {
        return encode(UTF_8, buffer);
    }

    public static CharBuffer decode(ByteBuffer buffer) {
        return decode(UTF_8, buffer);
    }

    public static ByteBuffer encode(Charset charset, CharBuffer buffer) {
        return charset.encode(buffer);
    }

    public static CharBuffer decode(Charset charset, ByteBuffer buffer) {
        return charset.decode(buffer);
    }
}

