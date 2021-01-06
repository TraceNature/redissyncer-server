package syncer.transmission.constants;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/14
 */
public class TaskMsgConstant {

    public static final String TASK_MSG_SYSTEM_ERROR="系统出错，请联系管理员";
    public static final String TASK_MSG_SYSTEM_ERROR_CODE="5000";

    public static final String TASK_MSG_REDIS_ERROR="目标redis连接失败";
    public static final String TASK_MSG_REDIS_ERROR_CODE="4000";

    public static final String TASK_MSG_REDIS_URI_ERROR="scheme must be [redis].";
    public static final String TASK_MSG_REDIS_URI_ERROR_CODE="4025";

    public static final String TASK_MSG_TARGET_REDIS_CONNECT_ERROR="目标redis连接失败";



    public static final String TASK_MSG_TASKSETTING_ERROR="相同配置任务已存在，请修改任务名";
    public static final String TASK_MSG_TASKSETTING_ERROR_CODE="4002";


    public static final String ERROR_CODE="1000";



    public static final String TASK_ERROR_MESSAGE="线程开小差了...";
    public static final String TASK_MSG_PARSE_ERROR_CODE="任务名称已存在....";


    public static final String TASK_MSG_URI_ERROR="任务URI信息有误，请检查";
    public static final String TASK_MSG_URI_ERROR_CODE="4001";



    public static final String TASK_MSG_REDIS_CONNECT_ERROR="无法连接redis,请检查redis配置以及其可用性";
    public static final String TASK_MSG_REDIS_CONNECT_ERROR_CODE="4003";






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


    public static final String TASK_MSG_REDIS_DB_ERROR="dbMaping中库号超出Redis库的最大大小";
    public static final String TASK_MSG_REDIS_DB_ERROR_CODE="4026";

    public static final String TASK_MSG_INCREMENT_ERROR="incrementtype参数错误 只能为（beginbuffer/endbuffer）";
    public static final String TASK_MSG_INCREMENT_ERROR_CODE="4027";



    public static final String TASK_MSG_SYNCTYPE_ERROR="SyncType参数错误";
    public static final String TASK_MSG_SYNCTYPE_ERROR_CODE="4028";
}
