package syncer.syncerreplication.util.objectUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
public class Arrays {

    public static String deepToString(Object[] obj) {
        return deepToString(obj, "[", "]", ", ");
    }

    public static String deepToString(Object[] obj, String st, String ed, String sep) {
        if (obj == null) {
            return "null";
        }
        int bufLen = 20 * obj.length;
        if (obj.length != 0 && bufLen <= 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder buf = new StringBuilder(bufLen);
        deepToString(obj, buf, new HashSet<Object[]>(), st, ed, sep);
        return buf.toString();
    }

    private static void deepToString(Object[] obj, StringBuilder buf, Set<Object[]> set, String st, String ed, String sep) {
        if (obj == null) {
            buf.append("null");
            return;
        }
        int iMax = obj.length - 1;
        if (iMax == -1) {
            buf.append(st).append(ed);
            return;
        }

        set.add(obj);
        buf.append(st);
        for (int i = 0; ; i++) {
            Object element = obj[i];
            if (element == null) {
                buf.append("null");
            } else {
                Class<?> eClass = element.getClass();

                if (eClass.isArray()) {
                    if (eClass == byte[].class) {
                        buf.append(toString((byte[]) element));
                    } else if (eClass == char[].class) {
                        buf.append(toString((char[]) element));
                    } else if (eClass == short[].class) {
                        buf.append(java.util.Arrays.toString((short[]) element));
                    } else if (eClass == int[].class) {
                        buf.append(java.util.Arrays.toString((int[]) element));
                    } else if (eClass == long[].class) {
                        buf.append(java.util.Arrays.toString((long[]) element));
                    } else if (eClass == float[].class) {
                        buf.append(java.util.Arrays.toString((float[]) element));
                    } else if (eClass == double[].class) {
                        buf.append(java.util.Arrays.toString((double[]) element));
                    } else if (eClass == boolean[].class) {
                        buf.append(java.util.Arrays.toString((boolean[]) element));
                    } else { // element is an array of object references
                        if (set.contains(element)) {
                            buf.append(st).append("...").append(ed);
                        } else {
                            deepToString((Object[]) element, buf, set, st, ed, sep);
                        }
                    }
                } else {  // element is non-null and not an array
                    buf.append(element.toString());
                }
            }
            if (i == iMax){
                break;
            }
            buf.append(sep);
        }
        buf.append(ed);
        set.remove(obj);
    }

    public static String toString(char[] a) {
        if (a == null) {
            return "null";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "";
        }
        return new String(a);
    }

    private static String toString(byte[] a) {
        if (a == null){
            return "null";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "";
        }
        return Strings.toString(a);
    }

}

