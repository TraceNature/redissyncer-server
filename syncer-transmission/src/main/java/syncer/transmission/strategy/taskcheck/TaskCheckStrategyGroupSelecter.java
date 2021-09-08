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

package syncer.transmission.strategy.taskcheck;

import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.factory.TaskCheckFileStrategyFactory;
import syncer.transmission.strategy.taskcheck.factory.TaskCheckNotDistinctStrategyFactory;
import syncer.transmission.strategy.taskcheck.factory.TaskCheckStrategyFactory;
import syncer.transmission.strategy.taskcheck.factory.TaskCommnadUpStrategyFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 任务策略选择器
 * @Date 2020/12/14
 */
public class TaskCheckStrategyGroupSelecter {
    /**
     * 策略组
     */
    private static volatile Map<RedisTaskStrategyGroupType, ITaskCheckStrategyFactory> strategyGroupMap = null;

    public static final Object LOCK = new Object();


    public synchronized static ITaskCheckStrategy select(RedisTaskStrategyGroupType type, RedisClient client, TaskModel taskModel) {
        if (null == strategyGroupMap) {
            initGroupMap();
        }
        if (!strategyGroupMap.containsKey(type)) {
            //初始化
            return null;
        }

        /**
         * 组装策略结构
         */
        List<ITaskCheckStrategy> redisStartCheckBaseStrategyList = strategyGroupMap.get(type).getStrategyList(client, taskModel);
        ITaskCheckStrategy result = null;
        //组装链式结构
        if (redisStartCheckBaseStrategyList != null && redisStartCheckBaseStrategyList.size() > 0) {
            for (int i = 0; i < redisStartCheckBaseStrategyList.size(); i++) {
                if (i < redisStartCheckBaseStrategyList.size() - 1) {
                    ITaskCheckStrategy filter = redisStartCheckBaseStrategyList.get(i);
                    filter.setNext(redisStartCheckBaseStrategyList.get(i + 1));
                }
            }
            result = redisStartCheckBaseStrategyList.get(0);
        }

        return result;
    }


    /**
     * 初始化strategyGroupMap
     */
    private static void initGroupMap() {

        //双重校验锁
        if (null == strategyGroupMap) {
            //类对象加锁
            synchronized (LOCK) {
                //再次判断
                if (null == strategyGroupMap) {
                    strategyGroupMap = new ConcurrentHashMap<>();
                    //初始化策略工厂

                    strategyGroupMap.put(RedisTaskStrategyGroupType.SYNCGROUP, TaskCheckStrategyFactory.builder().build());
                    strategyGroupMap.put(RedisTaskStrategyGroupType.NODISTINCT, TaskCheckNotDistinctStrategyFactory.builder().build());
                    strategyGroupMap.put(RedisTaskStrategyGroupType.FILEGROUP, TaskCheckFileStrategyFactory.builder().build());
                    strategyGroupMap.put(RedisTaskStrategyGroupType.COMMANDUPGROUP, TaskCommnadUpStrategyFactory.builder().build());

                }
            }
        }

    }
}
