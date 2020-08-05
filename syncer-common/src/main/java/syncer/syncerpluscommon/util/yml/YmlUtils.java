package syncer.syncerpluscommon.util.yml;

import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class YmlUtils {
    //递归解析map对象
    public static void loadRecursion(Map<String, Object> map, String key, Map<String, Object> conf){
        map.forEach((k,v) -> {
            if(isParent(v)){
                Map<String, Object> nextValue = (Map<String, Object>) v;
                loadRecursion(nextValue,(("".equals(key) ? "" : key + ".")+ k),conf);
            }else{
                conf.put(key+"."+k,v);
            }
        });
    }

    //判断是否还有子节点
    public static boolean isParent(Object o){
        if (!(o instanceof String || o instanceof Character || o instanceof Byte)) {
            try {
                Number n = (Number) o;
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }
}