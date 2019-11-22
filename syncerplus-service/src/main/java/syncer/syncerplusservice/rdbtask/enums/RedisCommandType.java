package syncer.syncerplusservice.rdbtask.enums;

import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.rdb.datatype.DB;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Constants;
import syncer.syncerplusredis.constant.RedisCommandTypeEnum;
import syncer.syncerplusservice.util.Jedis.JDJedis;
import syncer.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
@Slf4j
public class RedisCommandType {
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


    public synchronized static JDJedis getJDJedis(JDJedisClientPool targetJedisClientPool, Event event, Map<Integer, Integer> dbMap) throws Exception {

        DB db = null;
        if(event instanceof BatchedKeyValuePair){
            BatchedKeyValuePair keyValuePair= (BatchedKeyValuePair) event;
            db=keyValuePair.getDb();
        }else if(event instanceof DumpKeyValuePair){
            DumpKeyValuePair keyValuePair= (DumpKeyValuePair) event;
            db=keyValuePair.getDb();
        }
        int dbbnum = (int) db.getDbNumber();
        if (null != dbMap && dbMap.size() > 0) {
            if (dbMap.containsKey((int) db.getDbNumber())) {
                dbbnum = dbMap.get((int) db.getDbNumber());
            } else {
                throw new Exception("mapping映射对应信息不存在，当前数据不执行");
            }
        }

        StringBuffer info = new StringBuffer();
        int index;

        JDJedis targetJedisplus = null;
        try {
            targetJedisplus = targetJedisClientPool.getResource();
        } catch (Exception e) {
            log.warn("RDB复制 ：从池中获取RedisClient失败（准备重试）" + e.getMessage());
            try {
                targetJedisplus = targetJedisClientPool.getResource();
            } catch (Exception ex) {
                log.warn("RDB复制：从池中获取RedisClient失败（重试依旧失败）：" + ex.getMessage());
            }

        }

        if (db != null && (index = dbbnum) != targetJedisplus.getDbNum()) {
            try {
                targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
            } catch (Exception e) {
                log.warn("RDB复制：从池中获取链接 失败(重试): {}", e.getMessage());
                try {
                    targetJedisplus = targetJedisClientPool.selectDb(index, targetJedisplus);
                } catch (Exception exx) {
                    log.warn("RDB复制: 从池中获取链接 失败(重试失败): {}", exx.getMessage());
                }
            }

            info.append("SELECT:");
            info.append(index);
            log.info(info.toString());
        }

        return targetJedisplus;
    }
}
