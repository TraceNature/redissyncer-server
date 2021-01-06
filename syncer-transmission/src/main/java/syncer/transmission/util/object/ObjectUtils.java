package syncer.transmission.util.object;

import com.jd.jim.cli.protocol.ZSetTuple;
import syncer.common.util.file.SafeObjectInputStream;
import syncer.jedis.Jedis;
import syncer.replica.rdb.datatype.ZSetEntry;
import syncer.transmission.util.strings.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * @author 平行时空
 * @created 2018-06-14 21:52
 **/
public class ObjectUtils {
    /**
     * 序列化对象
     * @param object
     * @return
     */
    public static byte[] serialize(Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            if (object != null){
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(object);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反序列化对象
     * @param bytes
     * @return
     */
    public static Object unserialize(byte[] bytes) {
        ByteArrayInputStream bais = null;
        try {
            if (bytes != null && bytes.length > 0){
                bais = new ByteArrayInputStream(bytes);
                SafeObjectInputStream ois = new SafeObjectInputStream(bais);
//                ObjectInputStream ois = new Obj@ectInputStream(bais);
                return ois.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    /**
     * 获取byte[]类型Key
     * @param
     * @return
     */
    public synchronized static  byte[] getBytesKey(Object object){
        if(object instanceof String){
            return StringUtils.getBytes((String)object);
        }else{
            return ObjectUtils.serialize(object);
        }
    }


    /**
     * 对象转数组
     * @param obj
     * @return
     */
    public synchronized static byte[] toBytes (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 数组转对象
     * @param bytes
     * @return
     */
    public synchronized static Object toObject (byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
            SafeObjectInputStream ois = new SafeObjectInputStream (bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception ex) {
            return null;
        }
        return obj;
    }


//    /**
//     * Object转换byte[]类型
//     * @param
//     * @return
//     */
//    public synchronized static  byte[] toBytes(Object object){
//        return ObjectUtils.serialize(object);
//    }
//
//    /**
//     * byte[]型转换Object
//     * @param
//     * @return
//     */
//    public synchronized static Object toObject(byte[] bytes){
//        return ObjectUtils.unserialize(bytes);
//    }
//

    public synchronized static byte[][]  listBytes(List<byte[]> datas){
        byte[][] array = new byte[datas.size()][];
        datas.toArray(array);
        return array;
    }

    public synchronized static byte[][]  setBytes(Set<byte[]> datas){
        byte[][] array = new byte[datas.size()][];
        datas.toArray(array);
        return array;
    }

    public synchronized static Map<byte[], Double> zsetBytes(Set<ZSetEntry> datas){
        Map<byte[], Double> map = new HashMap<>();
        datas.forEach(zset -> {
            map.put(zset.getElement(), zset.getScore());
        });
        return map;
    }

    /**
     * JimDb
     * @param datas
     * @return
     */
    public synchronized static Set<ZSetTuple<byte[]>> jimDbZsetBytes(Set<ZSetEntry> datas){
        Set<ZSetTuple<byte[]>> map = new HashSet<>();
        datas.forEach(zset -> {
            map.add(new ZSetTuple<byte[]>(zset.getElement(), zset.getScore()));
        });
        return map;
    }


    public synchronized static Map<byte[], Double> zsetByteP(Set<byte[]> datas, Jedis source, byte[]key){
        Map<byte[], Double> map = new HashMap<>();
        datas.forEach(zset -> {
            try {
                ZSetEntry zSetEntry=new ZSetEntry(zset,source.zscore(key,zset));
                map.put(zSetEntry.getElement(), zSetEntry.getScore());
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        return map;
    }


    public static Object bytesToObject(byte[] bytes) throws Exception {
        //byte转object
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        SafeObjectInputStream sIn = new SafeObjectInputStream(in);
        return sIn.readObject();

    }
}