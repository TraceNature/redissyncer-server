package syncer.syncerservice.util;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.constant.RedisCommandTypeEnum;
import syncer.syncerplusredis.replicator.Constants;


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
