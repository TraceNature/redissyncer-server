package syncer.syncerservice.filter.redis_start_check_strategy;

import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.filter.strategy_type.RedisTaskStrategyGroupType;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/26
 */
public class RedisTaskStrategyGroupSelecter {

    private  static volatile Map<RedisTaskStrategyGroupType, IRedisStartCheckStrategyFactory> strategyGroupMap=null;

    public static final Object lock=new Object();

    public synchronized static IRedisStartCheckBaseStrategy select(RedisTaskStrategyGroupType type, JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps){
        if(null==strategyGroupMap){
            initGroupMap();
        }
        if(!strategyGroupMap.containsKey(type)){
            //初始化
            return null;
        }

        /**
         * 组装策略结构
         */
        List<IRedisStartCheckBaseStrategy>redisStartCheckBaseStrategyList=strategyGroupMap.get(type).getStrategyList(client, taskModel, redisPoolProps);
        IRedisStartCheckBaseStrategy result=null;
        //组装链式结构
        if(redisStartCheckBaseStrategyList!=null&&redisStartCheckBaseStrategyList.size()>0){
            for (int i = 0; i <redisStartCheckBaseStrategyList.size() ; i++) {
                if(i<redisStartCheckBaseStrategyList.size()-1){
                    IRedisStartCheckBaseStrategy filter=redisStartCheckBaseStrategyList.get(i);
                    filter.setNext(redisStartCheckBaseStrategyList.get(i+1));
                }
            }
            result=redisStartCheckBaseStrategyList.get(0);
        }

        return result;
    }


    /**
     * 初始化strategyGroupMap
     */
    private static void initGroupMap(){

        //双重校验锁
        if(null==strategyGroupMap){
            //类对象加锁
            synchronized(lock){
                //再次判断
                if (null==strategyGroupMap){
                    strategyGroupMap=new ConcurrentHashMap<>();
                    //初始化策略工厂
                    strategyGroupMap.put(RedisTaskStrategyGroupType.SYNCGROUP,SyncStartCheckStrategyFactory.builder().build());
                    strategyGroupMap.put(RedisTaskStrategyGroupType.NODISTINCT,SyncStartCheckNoDistinctStrategyFactory.builder().build());
                    strategyGroupMap.put(RedisTaskStrategyGroupType.FILEGROUP,SyncStartCheckFileStrategyFactory.builder().build());
                    strategyGroupMap.put(RedisTaskStrategyGroupType.COMMANDUPGROUP,SyncCommnadUpStrategyFactory.builder().build());



                    //
                }
            }
        }

    }
}
