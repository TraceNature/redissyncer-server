package syncer.syncerplusservice.util.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 平行时空
 * @created 2018-06-14 21:40
 **/
public interface IJedisClient {
    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */

     String get(String key);

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
     Object getObject(String key);

    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
     String set(String key, String value, int cacheSeconds);

    List<String>  keys(String key);


    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
     String setObject(String key, Object value, int cacheSeconds);

    /**
     * 获取List缓存
     * @param key 键
     * @return 值
     */
     List<String> getList(String key);

    /**
     * 获取List缓存
     * @param key 键
     * @return 值
     */
     List<Object> getObjectList(String key);

     String setbyteObject(byte[] key, byte[] value, Integer cacheSeconds);
    /**
     * 设置List缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
     long setList(String key, List<String> value, int cacheSeconds);

    /**
     * 设置List缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
     long setObjectList(String key, List<Object> value, int cacheSeconds);

    /**
     * 向List缓存中添加值
     * @param key 键
     * @param value 值
     * @return
     */
     long listAdd(String key, String... value);

    /**
     * 向List缓存中添加值
     * @param key 键
     * @param value 值
     * @return
     */
     long listObjectAdd(String key, Object... value);

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
     Set<String> getSet(String key);

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
     Set<Object> getObjectSet(String key);

    /**
     * 设置Set缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
     long setSet(String key, Set<String> value, int cacheSeconds);

    /**
     * 设置Set缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
     long setObjectSet(String key, Set<Object> value, int cacheSeconds);

    /**
     * 向Set缓存中添加值
     * @param key 键
     * @param value 值
     * @return
     */
     long setSetAdd(String key, String... value);

    /**
     * 向Set缓存中添加值
     * @param key 键
     * @param value 值
     * @return
     */
     long setSetObjectAdd(String key, Object... value);

    /**
     * 获取Map缓存
     * @param key 键
     * @return 值
     */
     Map<String, String> getMap(String key);

    /**
     * 获取Map缓存
     * @param key 键
     * @return 值
     */
     Map<String, Object> getObjectMap(String key);

    /**
     * 设置Map缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
     String setMap(String key, Map<String, String> value, int cacheSeconds);

    /**
     * 设置Map缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
     String setObjectMap(String key, Map<String, Object> value,
                         int cacheSeconds);

    /**
     * 向Map缓存中添加值
     * @param key 键
     * @param value 值
     * @return
     */
     String mapPut(String key, Map<String, String> value);

    /**
     * 向Map缓存中添加值
     * @param key 键
     * @param value 值
     * @return
     */
     String mapObjectPut(String key, Map<String, Object> value);

    /**
     * 移除Map缓存中的值
     * @param key 键
     * @param mapKey 值
     * @return
     */
     long mapRemove(String key, String mapKey);

    /**
     * 移除Map缓存中的值
     * @param key 键
     * @param mapKey 值
     * @return
     */
     long mapObjectRemove(String key, String mapKey);

    /**
     * 判断Map缓存中的Key是否存在
     * @param key 键
     * @param mapKey 值
     * @return
     */
    public boolean mapExists(String key, String mapKey);

    /**
     * 判断Map缓存中的Key是否存在
     * @param key 键
     * @param mapKey 值
     * @return
     */
     boolean mapObjectExists(String key, String mapKey);

    /**
     * 删除缓存
     * @param key 键
     * @return
     */
     long del(String key);

    /**
     * 删除缓存
     * @param key 键
     * @return
     */
     long delObject(String key);

    /**
     * 缓存是否存在
     * @param key 键
     * @return
     */
     boolean exists(String key);

    /**
     * 缓存是否存在
     * @param key 键
     * @return
     */
     boolean existsObject(String key);

    /**
     * 清除模糊缓存
     * @param key
     */
    void flushlikekey(String key);
    void flushlikekey(String... keys);
    void flushlikekey_foreach(String key);

    /**
     * 返回剩余时间 毫秒
     * @param key
     * @return
     */
    long pttl(String key);

    /**
     * 返回剩余时间 秒
     * @param key
     * @return
     */
    long ttl(String key);

    /**
     * 设置生存时间 秒
     * @param key
     * @return
     */
    long expire(String key, int seconds);

    /**
     * 设置生存时间 毫秒
     * @param key
     * @return
     */
    long pexpire(String key, long milliseconds);

    public  Set<String>  allkeys(String redisKeyStartWith);

    public  void deleteRedisKeyStartWith(String redisKeyStartWith);
}
