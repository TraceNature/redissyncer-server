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

package syncer.transmission.util;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.constant.Constants;
import syncer.transmission.constants.RedisCommandTypeEnum;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/23
 */
@Slf4j
public class RedisCommandTypeUtils {
    public static synchronized RedisCommandTypeEnum getRedisCommandTypeEnum(int rdbType){
        if (rdbType == Constants.RDB_TYPE_SET){    //2
            return RedisCommandTypeEnum.SET;
        }else if(rdbType== Constants.RDB_TYPE_ZSET){  //3
            return RedisCommandTypeEnum.ZSET;
        }else if(rdbType== Constants.RDB_TYPE_STRING){  //0
            return RedisCommandTypeEnum.STRING;
        }else if(rdbType== Constants.RDB_TYPE_HASH){  //4
            return RedisCommandTypeEnum.HASH;
        }else if(rdbType== Constants.RDB_TYPE_LIST){  //1
            return RedisCommandTypeEnum.LIST;
        }else if(rdbType==Constants.RDB_TYPE_HASH_ZIPLIST){ //13
            return RedisCommandTypeEnum.HASH;
        }else if(rdbType==Constants.RDB_TYPE_HASH_ZIPMAP){  //9
            return RedisCommandTypeEnum.HASH;
        }else if(rdbType==Constants.RDB_TYPE_SET_INTSET){ //11
            return RedisCommandTypeEnum.SET;
        }else if(rdbType==Constants.RDB_TYPE_ZSET_ZIPLIST){  //12
            return RedisCommandTypeEnum.ZSET;
        }else if(rdbType==Constants.RDB_TYPE_LIST_QUICKLIST){ //14
            return RedisCommandTypeEnum.LIST;
        }else if(rdbType==Constants.RDB_TYPE_ZSET_2){  //5
            return RedisCommandTypeEnum.ZSET;
        }else if(rdbType==Constants.RDB_TYPE_LIST_ZIPLIST){  //10
            return RedisCommandTypeEnum.LIST;
        }
        return RedisCommandTypeEnum.STRING;

    }
}
