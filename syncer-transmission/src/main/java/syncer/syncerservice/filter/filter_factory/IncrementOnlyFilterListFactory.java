package syncer.syncerservice.filter.filter_factory;

import lombok.Builder;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.filter.CommonFilter;
import syncer.syncerservice.filter.KeyValueCommandSyncEventFilter;
import syncer.syncerservice.filter.KeyValueEventDBMappingFilter;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 只增量策略链表生成工厂
 * @Date 2020/3/18
 */
@Builder
public class IncrementOnlyFilterListFactory implements KeyValueFilterListFactory{
    @Override
    public List<CommonFilter> getStrategyList(TaskModel taskModel, JDRedisClient client) {
        List<CommonFilter> strategyList = new ArrayList<>();
        strategyList.add(KeyValueEventDBMappingFilter.builder().taskId(taskModel.getId()).client(client).build());

//            commonFilterList.add(KeyValueSizeCalulationFilter.builder().taskId(taskId).client(client).build());

        strategyList.add(KeyValueCommandSyncEventFilter.builder().taskId(taskModel.getId()).client(client).build());
        return strategyList;
    }
}
