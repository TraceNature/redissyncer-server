package syncer.transmission.util.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 最近最少使用淘汰算法
 * @author zhanenqiang
 * @Description 描述
 * @Date 2019/12/30
 */
public class LruCache <k, v> extends LinkedHashMap<k, v> {
    private final int MAX_SIZE;
    public LruCache(int capcity) {
        super(8, 0.75f,true);
        this.MAX_SIZE = capcity;
    }

    @Override
    public boolean removeEldestEntry(Map.Entry<k, v> eldest) {
        if (size() > MAX_SIZE) {
            eldest.getValue();
//            System.out.println("移除的元素为：" + eldest.getValue());
        }
        return size() > MAX_SIZE;
    }

}