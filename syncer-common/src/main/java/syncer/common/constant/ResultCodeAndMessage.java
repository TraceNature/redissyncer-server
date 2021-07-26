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
package syncer.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/24
 */
@Getter
@AllArgsConstructor
public enum ResultCodeAndMessage {
    /**
     * listTasks 按name查询接口name不能为空
     */
    TASK_NAMES_NOT_EMPTY("4009","tasknames参数不能有为空","taskNames不能为空"),
    /**
     * listTasks 按状态查询接口status不能为空
     */
    TASK_STATUS_NOT_EMPTY("4010","taskstatus 不能有为空","taskstatus不为空"),

    /**
     * listTasks 按状态查询接口status格式不正确
     */
    TASK_STATUS_NOT_FIND("4011","taskstatus 格式不正确","taskstatus 格式不正确"),

    /**
     * listTasks 按GroupId查询接口groupIds不能为空
     */
    TASK_GROUPID_NOT_EMPTY("4012","groupIds不能有为空","groupIds不为空"),

    TASK_MSG_TASKSETTING_ERROR("4002","相同配置任务已存在，请修改任务名","相同配置任务已存在，请修改任务名"),

    TASK_MSG_REDIS_MSG_ERROR("4024","targetRedisVersion can not be empty /targetRedisVersion error","redis version错误"),

    TASK_MSG_ONLINEFILE_ADDRESS_ERROR("4025","在线文件不存在，请检查FileAddress链接是否正确","在线文件不存在"),

    TASK_MSG_TASK_ID_ERROR("4026","请检查taskId是否填写","请检查taskId是否填写"),

    TASK_EDIT_MSG_TASK_NOT_STOP_ERROR("4027","无法编辑未停止的任务","无法编辑未停止的任务"),

    TASK_MSG_TASK_IS_NULL_ERROR("4028","任务不存在","任务不存在"),

    TASK_MSG_TASK_SOURCE_REDIS_TYPE_NULL("4029","源Redis集群sourceRedisType类型不能为空","源Redis集群类型不能为空"),
    TASK_MSG_TASK_TARGET_REDIS_TYPE_NULL("4029","目标Redis集群targetRedisType类型不能为空","源Redis集群类型不能为空"),

    TASK_MSG_TASK_SOURCE_MASTER_REDIS_NAME_NULL("4030","源Redis集群sourceRedisMasterName类型不能为空","源Redis集群sourceRedisMasterName类型不能为空"),
    TASK_MSG_TASK_TARGET_MASTER_REDIS_NAME_NULL("4030","目标Redis集群targetRedisMasterName类型不能为空","目标Redis集群targetRedisMasterName类型不能为空"),
    TASK_MSG_TASK_TARGET_TOPIC_NAME_NULL("4031","kafka命令订阅模式topicName不能为空","kafka命令订阅模式topicName不能为空"),
    TASK_MSG_TASK_TARGET_KAFKA_ADDRESS_NULL("4032","kafka命令订阅模式targetKafkaAddress不能为空","kafka命令订阅模式targetKafkaAddress不能为空"),

    TASK_MSG_RDB_VERSION_MSG_ERROR("4033","rdb版本获取失败,暂不支持目标Redis版本","rdb version错误"),

    TASK_MSG_TASK_TARGET_REDIS_ADDRESS_NULL("100","目标redis地址不能为空","目标redis地址不能为空")



    ;


    private String code;
    private String msg;
    private String desc;
}
