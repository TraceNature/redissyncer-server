package syncer.syncerservice.filter.strategy_factory;

import syncer.syncerservice.filter.redis_start_check_strategy.IRedisStartCheckBaseStrategy;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/26
 */
public interface IRedisTaskStrategyGroupFactory {
    List<IRedisStartCheckBaseStrategy> getStrategyGroup();
}
