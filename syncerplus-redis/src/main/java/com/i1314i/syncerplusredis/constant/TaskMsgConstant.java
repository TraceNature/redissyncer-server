package com.i1314i.syncerplusredis.constant;


/**
 * 状态码规范
 *
 * 2XXX	以 2 开头的状态码，表示成功。
 * 4XXX	以 4 开头的状态码，表示业务错误。
 * 5XXX	以 5 开头的状态码，表示系统错误。
 */
public class TaskMsgConstant {
    public static final String Task_ERROR_MESSAGE="线程开小差了...";
    public static final String Task_MSG_PARSE_ERROR_CODE="任务名称已存在....";

    public static final String TASK_MSG_REDIS_ERROR="目标redis连接失败";
    public static final String TASK_MSG_REDIS_ERROR_CODE="4000";

    public static final String TASK_MSG_URI_ERROR="任务URI信息有误，请检查";
    public static final String TASK_MSG_URI_ERROR_CODE="4001";

    public static final String TASK_MSG_TASKSETTING_ERROR="相同配置任务已存在，请修改任务名";
    public static final String TASK_MSG_TASKSETTING_ERROR_CODE="4002";

    public static final String TASK_MSG_REDIS_CONNECT_ERROR="无法连接redis,请检查redis配置以及其可用性";
    public static final String TASK_MSG_REDIS_CONNECT_ERROR_CODE="4003";



    public static final String TASK_MSG_TARGET_REDIS_CONNECT_ERROR="目标redis连接失败";
//    public static final String TASK_MSG_TARGET_REDIS_CONNECT_ERROR_CODE="4004";


    public static final String TASK_MSG_TASKID_UNCREATE__ERROR="taskid为【taskId】的任务还未创建";
    public static final String TASK_MSG_TASKID_UNCREATE_ERROR_CODE="4005";

    public static final String TASK_MSG_TASKID_NULL_ERROR="taskids中不能存在空值";
    public static final String TASK_MSG_TASKID_NULL_ERROR_CODE="4006";

    public static final String TASK_MSG_TASKID_RUN_ERROR="请先停止taskids中处于运行状态的任务";
    public static final String TASK_MSG_TASKID_RUN_ERROR_CODE="4007";

    public static final String TASK_MSG_TASKID_RUNING_ERROR="请先停止taskids中处于运行状态的任务";
    public static final String TASK_MSG_TASKID_RUNING_ERROR_CODE="4008";


    public static final String TASK_MSG_TASKNAME_RUNING_ERROR="tasknames 不能有为空的参数";
    public static final String TASK_MSG_TASKNAME_RUNING_ERROR_CODE="4009";



    public static final String TASK_MSG_TASKSTATUS_NULL_ERROR=" taskstatus 不能有为空";
    public static final String TASK_MSG_TASKSTATUS_NULL_ERROR_CODE="4010";


    public static final String TASK_MSG_TASKID_EXIST_ERROR="不存在任务id为:taskId的任务";
    public static final String TASK_MSG_TASKID_EXIST_ERROR_CODE="4020";



    public static final String TASK_MSG_TASKNAME_TYPE_ERROR="bynames 参数类型错误";
    public static final String TASK_MSG_TASKNAME_TYPE_ERROR_CODE="4021";


    public static final String TASK_MSG_TASKID_REGULATION_ERROR="regulation 参数类型错误";
    public static final String TASK_MSG_TASKID_REGULATION_ERROR_CODE="4022";

    public static final String TASK_MSG_TASK_EDIT_ERROR="不能编辑正在运行中的任务【TaskId】";
    public static final String TASK_MSG_TASK_EDIT_ERROR_CODE="4023";


    public static final String TASK_MSG_REDIS_MSG_ERROR="targetRedisVersion can not be empty /targetRedisVersion error";
    public static final String TASK_MSG_REDIS_MSG_ERROR_CODE="4024";

    public static final String TASK_MSG_REDIS_URI_ERROR="scheme must be [redis].";
    public static final String TASK_MSG_REDIS_URI_ERROR_CODE="4025";


    public static final String TASK_MSG_REDIS_DB_ERROR="dbMaping中库号超出Redis库的最大大小";
    public static final String TASK_MSG_REDIS_DB_ERROR_CODE="4026";

    ///dbMaping中库号超出Redis库的最大大小

    //targetRedisVersion can not be empty /targetRedisVersion error
//    public static final String TASK_MSG_TASK_EDIT_ERROR="scheme must be [redis].";
//    public static final String TASK_MSG_TASK_EDIT_ERROR_CODE="4014";

    //不能编辑正在运行中的任务【"+syncDataDto.getTaskId()+"】

    //"scheme must be [redis]."
   //"regulation 参数类型错误"  taskids中不能存在空值

    ///throw new TaskMsgException("不存在任务id为："+taskId+"的任务");  exist
    //tasknames 不能有为空的参数

    //"taskid为【"+taskId+"】的任务还未创建"


//任务：【"+taskId+"】已经在运行中  任务：【"+taskId+"】已经在运行中

//    taskid为【"+taskId+"】的任务还未创建
//taskids中不能存在空值
    //请先停止taskids中处于运行状态的任务
    //不存在任务id为："+taskId+"的任务



    public static final String TASK_MSG_SYSTEM_ERROR="系统出错，请联系管理员";
    public static final String TASK_MSG_SYSTEM_ERROR_CODE="5000";

}
