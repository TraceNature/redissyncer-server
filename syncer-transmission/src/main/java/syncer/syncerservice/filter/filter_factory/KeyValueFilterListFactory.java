package syncer.syncerservice.filter.filter_factory;

import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.filter.CommonFilter;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/18
 */
public interface KeyValueFilterListFactory {
     List<CommonFilter> getStrategyList(TaskModel taskModel, JDRedisClient client);
}
