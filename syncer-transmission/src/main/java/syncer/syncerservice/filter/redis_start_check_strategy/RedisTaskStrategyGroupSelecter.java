package syncer.syncerservice.filter.redis_start_check_strategy;

import syncer.syncerservice.filter.strategy_factory.IRedisTaskStrategyGroupFactory;
import syncer.syncerservice.filter.strategy_type.RedisTaskStrategyGroupType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/26
 */
public class RedisTaskStrategyGroupSelecter {

    private  static volatile Map<RedisTaskStrategyGroupType, IRedisTaskStrategyGroupFactory> strategyGroupMap=null;

    public static final Object lock=new Object();

    public List<IRedisStartCheckBaseStrategy> select(RedisTaskStrategyGroupType type){
        if(null==strategyGroupMap){
            initGroupMap();
        }
        if(!strategyGroupMap.containsKey(type)){
            //初始化
            return null;
        }

        return strategyGroupMap.get(type).getStrategyGroup();
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
                    strategyGroupMap.put(RedisTaskStrategyGroupType.TEST,null);
                    //
                }
            }
        }

    }
}
