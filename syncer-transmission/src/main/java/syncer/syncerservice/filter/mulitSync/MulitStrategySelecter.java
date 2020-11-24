package syncer.syncerservice.filter.mulitSync;

import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltCreateTaskData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 双向同步任务选择器
 * @Date 2020/11/13
 */
public class MulitStrategySelecter {
    private  static volatile Map<MulitSyncStrategyGroupType, IMulitStrategyFactory> strategyGroupMap=null;

    public static final Object lock=new Object();

    public synchronized static IMulitCommonStrategy select(MulitSyncStrategyGroupType type, MuiltCreateTaskData data, ParentMultiTaskModel parentMultiTaskModel){
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
        List<IMulitCommonStrategy> muliCommonBaseStrategyList=strategyGroupMap.get(type).getStrategyList(data, parentMultiTaskModel);
        IMulitCommonStrategy  result=null;
        //组装链式结构
        if(muliCommonBaseStrategyList!=null&&muliCommonBaseStrategyList.size()>0){
            for (int i = 0; i <muliCommonBaseStrategyList.size() ; i++) {
                if(i<muliCommonBaseStrategyList.size()-1){
                    IMulitCommonStrategy filter=muliCommonBaseStrategyList.get(i);
                    filter.setNext(muliCommonBaseStrategyList.get(i+1));
                }
            }
            result=muliCommonBaseStrategyList.get(0);
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
                    strategyGroupMap.put(MulitSyncStrategyGroupType.NODISTINCT,MulitCreateDistinctStrategyFactory.builder().build());

                }
            }
        }

    }
}
