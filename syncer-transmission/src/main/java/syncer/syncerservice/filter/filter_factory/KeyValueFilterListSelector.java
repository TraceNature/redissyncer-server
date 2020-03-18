package syncer.syncerservice.filter.filter_factory;

import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.filter.CommonFilter;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/18
 */

public class KeyValueFilterListSelector {
    private static final Map<TaskRunTypeEnum,KeyValueFilterListFactory>kvFilterListSelectorMap=new ConcurrentHashMap<>();

    public static List<CommonFilter> getStrategyList(TaskRunTypeEnum taskRunTypeEnum, TaskModel taskModel, JDRedisClient client){
            if(!kvFilterListSelectorMap.containsKey(taskRunTypeEnum)){
                if(taskRunTypeEnum.equals(TaskRunTypeEnum.TOTAL)){
                    kvFilterListSelectorMap.put(taskRunTypeEnum,TotalFilterListFactory.builder().build());
                }else if(taskRunTypeEnum.equals(TaskRunTypeEnum.STOCKONLY)){
                    kvFilterListSelectorMap.put(taskRunTypeEnum,StockonlyFilterListFactory.builder().build());
                }else if(taskRunTypeEnum.equals(TaskRunTypeEnum.INCREMENTONLY)){
                    kvFilterListSelectorMap.put(taskRunTypeEnum,IncrementOnlyFilterListFactory.builder().build());
                }
            }
            return kvFilterListSelectorMap.get(taskRunTypeEnum).getStrategyList(taskModel,client);
    }

}
