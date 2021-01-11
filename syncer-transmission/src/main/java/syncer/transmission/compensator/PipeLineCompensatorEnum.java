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

/**
 * pipeline补偿机制支持命令集合
 * @author zhanenqiang
 * @Description 描述
 * @Date 2019/12/30
 */
public enum PipeLineCompensatorEnum {
    /**
     * String no time
     */
    SET,

    /**
     * String with time
     */
    SET_WITH_TIME,



    LPUSH,

    LPUSH_WITH_TIME,

    LPUSH_LIST,

    LPUSH_WITH_TIME_LIST,

    SADD,

    SADD_WITH_TIME,

    SADD_SET,

    SADD_WITH_TIME_SET,

    ZADD,

    ZADD_WITH_TIME,

    HMSET,

    HMSET_WITH_TIME,

    RESTORE,

    RESTORREPLCE,

    DEL,
    COMMAND,
    SELECT,
    PEXPIRE,


    //////////////////////////////////////////非幂等性命令

    /**
     * String类型
     */
    APPEND,

    /**
     *自增
     */
    INCR,

    INCRBY,

    INCRBYFLOAT,

    /**
     * 自减
     */

    DECR,

    DECRBY
}
