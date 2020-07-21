package syncer.syncerpluscommon.constant;

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


    TASK_MSG_TASK_IS_NULL_ERROR("4028","任务不存在","任务不存在");


    private String code;
    private String msg;
    private String desc;
}
