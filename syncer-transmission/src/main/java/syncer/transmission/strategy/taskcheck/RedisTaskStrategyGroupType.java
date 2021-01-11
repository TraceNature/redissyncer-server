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

/**
 * @author zhanenqiang
 * @Description 任务创建检测策略类型
 * @Date 2020/12/14
 */
public enum  RedisTaskStrategyGroupType {

    /**
     * sync组
     */
    SYNCGROUP,

    /**
     * 统一策略不判断是否重复
     */
    NODISTINCT,

    /**
     * 文件组
     */
    FILEGROUP,

    /**
     * 实时备份AOF任务组
     */
    COMMANDUPGROUP
}