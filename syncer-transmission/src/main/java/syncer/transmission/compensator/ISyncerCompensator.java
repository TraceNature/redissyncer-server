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

package syncer.transmission.compensator;


import syncer.replica.datatype.rdb.zset.ZSetEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 补偿机制
 */
public interface ISyncerCompensator {

    //STRING
    void set(final Long dbNum,final byte[]key,final byte[]value,String res);
    void set(final Long dbNum,final byte[]key,final byte[]value,long ms,String res);
    void append(final Long dbNum,final byte[] key, final byte[] value,Long res);
    //LIST
    void lpush(final Long dbNum,final  byte[]key,final byte[][]value,Long res);
    void lpush(final Long dbNum,final byte[]key,long ms,final byte[][]value,Long res);

    void lpush(final Long dbNum, final  byte[]key, final List<byte[]> value, Long res);
    void lpush(final Long dbNum,final byte[]key,long ms,final List<byte[]>value,Long res);


    void rpush(final Long dbNum,final  byte[]key,final byte[][]value,Long res);
    void rpush(final Long dbNum,final byte[]key,long ms,final byte[][]value,Long res);

    void rpush(final Long dbNum,final  byte[]key,final List<byte[]> value,Long res);
    void rpush(final Long dbNum,final byte[]key,long ms,final List<byte[]>value,Long res);

    //SET
    void sadd(final Long dbNum,final byte[] key, final byte[][] members,Long res);
    void sadd(final Long dbNum,final byte[] key,long ms, final byte[][] members,Long res);
    void sadd(final Long dbNum, final byte[] key, final Set<byte[]> members, Long res);
    void sadd(final Long dbNum,final byte[] key,long ms, final Set<byte[]> members,Long res);
    //ZSET
    void zadd(final Long dbNum, byte[]key, Set<ZSetEntry> value, Long res);
    void zadd(final Long dbNum,byte[]key,Set<ZSetEntry> value,long ms,Long res);
    //HASH
    void hmset(final Long dbNum, final byte[] key, final Map<byte[], byte[]> hash, String res);
    void hmset(final Long dbNum,final byte[] key, final Map<byte[], byte[]> hash,long ms,String res);
    //DUMP
    void restore(final Long dbNum,final byte[] key, final long ttl, final byte[] serializedValue,String res);
    void restoreReplace(final Long dbNum,final byte[] key, final long ttl, final byte[] serializedValue,String res);
    void restoreReplace(final Long dbNum,byte[] key, long ttl, byte[] serializedValue,boolean highVersion,String res);
    void send(final byte[] cmd,Object res, final byte[]... args) throws Exception;
    void select(final Integer dbNum);


}
