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

package syncer.transmission.constants;

/**
 * 补偿命令集合
 */
public enum CmdEnum {
    ////////////////////////////////////幂等性命令
    /**
     * String
     */
    SET,

    /**
     * List类型
     */
    LPUSH,
    /**
     *Set类型
     */
    SADD,
    /**
     *ZSet类型
     */
    ZADD,
    /**
     *Hash类型
     */
    HMSET,

    /**
     * dump
     */
    RESTORE,

    /**
     * dump替换
     */
    RESOREREPLACE,

    /**
     * command命令格式
     */
    SEND,


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