package syncer.syncerservice.util.common;

import syncer.syncerplusredis.rdb.datatype.ZSetEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisCommon {
    public static synchronized byte[][]  listBytes(List<byte[]> datas){
        byte[][] array = new byte[datas.size()][];
        datas.toArray(array);
        return array;
    }

    public static synchronized  byte[][]  setBytes(Set<byte[]> datas){
        byte[][] array = new byte[datas.size()][];
        datas.toArray(array);
        return array;
    }

    public static synchronized  Map<byte[], Double> zsetBytes(Set<ZSetEntry> datas){
        Map<byte[], Double> map = new HashMap<>(10);
        datas.forEach(zset -> {
            map.put(zset.getElement(), zset.getScore());
        });
        return map;
    }

}
