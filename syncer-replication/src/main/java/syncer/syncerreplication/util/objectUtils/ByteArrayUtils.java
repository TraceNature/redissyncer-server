package syncer.syncerreplication.util.objectUtils;

import java.io.*;
import java.util.Optional;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/8
 */
public class ByteArrayUtils {

    public static<T> Optional<byte[]> objectToBytes(T obj){
        byte[] bytes = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream sOut;
        try {
            sOut = new ObjectOutputStream(out);
            sOut.writeObject(obj);
            sOut.flush();
            bytes= out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(bytes);
    }

    public static<T> Optional<T> bytesToObject(byte[] bytes) {
        T t = null;
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream sIn;
        try {
            sIn = new ObjectInputStream(in);
            t = (T)sIn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(t);

    }
}
