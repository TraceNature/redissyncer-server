package syncer.webapp.constants;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/7
 */
public class ApiConstants {
    public static final String SUCCESS_CODE="2000";

    public static final String REQUEST_SUCCESS_MSG="request is successful";


    public static final String ERROR_CODE="1000";

    public static final String REQUEST_ERROR_MSG="request is fail";

    public static final String INSERT_RDB_VERSION_ERROR_MSG="新增RDB映射关系失败";

    public static final String SUCCESS_MSG="success";

    public static final String FAIL_MSG="fail";

    public static final String TOKEN_ERROR="请检查token";

    public static final String USER_NOT_EXIST="登陆用户不存在[请检查Token]";

    public static final String PASSWORD_LEN_ERROR="新密码长度必须大于5位";

    public static final String PASSWORD_OLD_ERROR="旧密码不正确";

    public static final String PASSWORD_REPEAT_ERROR="新密码不能和旧密码重复";

    public static final String PASSWORD_CHANGE_SUCCESS="修改密码成功";

    public static final String PASSWORD_CHANGE_ERROR="修改密码失败";

    public static final String TASK_CREATE_LIST_EMPTY="任务列表为空，请检查填入任务信息";


    public static final String TASK_ABOVE_THRESHOLD_CODE="1005";
    public static final String TASK_ABOVE_THRESHOLD_MSG="当前系统已处于高负载状态,已开启任务数量限制，请稍后再创建任务";

    public static final String TASK_TASK_GROUP_ID_NOT_NULL_CODE="4000";
    public static final String TASK_TASK_GROUP_ID_NOT_NULL_MSG="taskids或GroupId不能为空";

    //4000
    public static final String TASK_PARAM_ERROR="参数错误";

}
