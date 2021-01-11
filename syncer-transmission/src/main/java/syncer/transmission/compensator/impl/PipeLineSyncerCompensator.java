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

package syncer.transmission.compensator.impl;

import syncer.jedis.Jedis;
import syncer.replica.rdb.datatype.ZSetEntry;
import syncer.transmission.compensator.ISyncerCompensator;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2019/12/24
 */
public class PipeLineSyncerCompensator implements ISyncerCompensator {


    private Jedis client;

    @Override
    public void set(Long dbNum, byte[] key, byte[] value, String res) {

    }

    @Override
    public void set(Long dbNum, byte[] key, byte[] value, long ms, String res) {

    }

    @Override
    public void append(Long dbNum, byte[] key, byte[] value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, byte[][] value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, byte[][] value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, List<byte[]> value, Long res) {

    }

    @Override
    public void lpush(Long dbNum, byte[] key, long ms, List<byte[]> value, Long res) {

    }

    @Override
    public void rpush(Long dbNum, byte[] key, byte[][] value, Long res) {

    }

    @Override
    public void rpush(Long dbNum, byte[] key, long ms, byte[][] value, Long res) {

    }

    @Override
    public void rpush(Long dbNum, byte[] key, List<byte[]> value, Long res) {

    }

    @Override
    public void rpush(Long dbNum, byte[] key, long ms, List<byte[]> value, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, byte[][] members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, long ms, byte[][] members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, Set<byte[]> members, Long res) {

    }

    @Override
    public void sadd(Long dbNum, byte[] key, long ms, Set<byte[]> members, Long res) {

    }

    @Override
    public void zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, Long res) {

    }

    @Override
    public void zadd(Long dbNum, byte[] key, Set<ZSetEntry> value, long ms, Long res) {

    }

    @Override
    public void hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, String res) {

    }

    @Override
    public void hmset(Long dbNum, byte[] key, Map<byte[], byte[]> hash, long ms, String res) {

    }

    @Override
    public void restore(Long dbNum, byte[] key, long ttl, byte[] serializedValue, String res) {

    }

    @Override
    public void restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, String res) {

    }

    @Override
    public void restoreReplace(Long dbNum, byte[] key, long ttl, byte[] serializedValue, boolean highVersion, String res) {

    }

    @Override
    public void send(byte[] cmd, Object res, byte[]... args) {

    }

    @Override
    public void select(Integer dbNum) {
    }
}
