// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.client;


import syncer.replica.datatype.rdb.zset.ZSetEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/14
 */
public interface RedisClient {

    /**
     * STRING
     * @param dbNum
     * @param key
     * @return
     */
    String get(final Long dbNum,final byte[]key);
    String get(final Long dbNum,final String key);
    String set(final Long dbNum,final byte[]key,final byte[]value);
    String set(final Long dbNum,final byte[]key,final byte[]value,long ms);
    Long append(final Long dbNum,final byte[] key, final byte[] value);


    /**
     * LIST
     * @param dbNum
     * @param key
     * @param value
     * @return
     */
    Long lpush(final Long dbNum,final  byte[]key,final byte[]...value);
    Long lpush(final Long dbNum,final byte[]key,long ms,final byte[]...value);

    Long lpush(final Long dbNum,final  byte[]key,final List<byte[]> value);
    Long lpush(final Long dbNum,final byte[]key,long ms,final List<byte[]>value);

    Long rpush(final Long dbNum,final  byte[]key,final byte[]...value);
    Long rpush(final Long dbNum,final byte[]key,long ms,final byte[]...value);

    Long rpush(final Long dbNum,final  byte[]key,final List<byte[]> value);
    Long rpush(final Long dbNum,final byte[]key,long ms,final List<byte[]>value);


    /**
     * SET
     * @param dbNum
     * @param key
     * @param members
     * @return
     */
    Long sadd(final Long dbNum,final byte[] key, final byte[]... members);
    Long sadd(final Long dbNum,final byte[] key,long ms, final byte[]... members);
    Long sadd(final Long dbNum,final byte[] key, final Set<byte[]> members);
    Long sadd(final Long dbNum,final byte[] key,long ms, final Set<byte[]> members);


    /**
     * ZSET
     * @param dbNum
     * @param key
     * @param value
     * @return
     */
    Long zadd(final Long dbNum,byte[]key, Set<ZSetEntry> value);
    Long zadd(final Long dbNum, byte[]key, Set<ZSetEntry> value, long ms);

    /**
     * HASH
     * @param dbNum
     * @param key
     * @param hash
     * @return
     */
    String hmset(final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash);
    String hmset(final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash,long ms);


    /**
     * DUMP
     * @param dbNum
     * @param key
     * @param ttl
     * @param serializedValue
     * @return
     */
    String restore(final Long dbNum,final byte[] key, final long ttl, final byte[] serializedValue);
    String restoreReplace(final Long dbNum,final byte[] key, final long ttl, final byte[] serializedValue);
    String restoreReplace(final Long dbNum,byte[] key, long ttl, byte[] serializedValue,boolean highVersion);
    Object send(final byte[] cmd, final byte[]... args);
    void select(final Integer dbNum);
    Long pexpire(Long dbNum,byte[]key,long ms);

    void updateLastReplidAndOffset(String replid,long offset);
}
