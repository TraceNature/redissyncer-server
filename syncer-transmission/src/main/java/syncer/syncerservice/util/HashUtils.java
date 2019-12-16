package syncer.syncerservice.util;

import org.springframework.util.StringUtils;

public class HashUtils {

    //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
    public synchronized static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }

    //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
    public synchronized static int getHash(String str,int count) {
        if(StringUtils.isEmpty(str)) {
            return 0;
        }
        return getHash(str)%count;
    }


    public static void main(String[] args) {
        System.out.println(getHash("dddhhhddhs",3));
    }
}
