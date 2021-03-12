package syncer.transmission.constants;

/**
 * @author: Eq Zhan
 * @create: 2021-02-24
 **/
public class EtcdKeyCmd {
    /**
     * taskId
     * /tasks/taskid/{taskId}  json
     * @param taskId
     * @return
     */
    public static String getTasksTaskId(String taskId){
        return "/tasks/taskid/"+taskId;
    }

    /**
     * 查询所有任务前缀
     * @return
     */
    public static  String getTasksTaskIdPrefix(){
        return "/tasks/taskid/";
    }

    /**
     * /tasks/node/{nodeId}/{taskId} taskid
     */
    public static String getNodeIdTaskId(String nodeId,String taskId){
        return new StringBuilder("/tasks/node/").append(nodeId).append("/").append(taskId).toString();
    }

    /**
     * 查询当前nodeId下所有任务
     * @return
     */
    public static  String getTasksByNodeId(String nodeId){
        return new StringBuilder("/tasks/node/").append(nodeId).toString();
    }

    /**
     * /tasks/groupid/{groupid}/{taskId}  groupid
     */
    public static String getGroupIdTaskId(String groupId,String taskId){
        return new StringBuilder("/tasks/groupid/").append(groupId).append("/").append(taskId).toString();

    }


    /**
     *
     *  md5
     * /tasks/md5/{md5}  {"taskid":"","groupId":"","nodeId":""}
     */
    public static String getMd5(String md5){
        return new StringBuilder("/tasks/md5/").append(md5).toString();
    }

    /**
     *  获取任务类型Key
     *  taskType
     * /tasks/type/{type}/{taskId}  {"taskid":"","groupId":"","nodeId":""}
     */
    public static String getTaskType(Integer type,String taskId){
        return new StringBuilder("/tasks/type/").append(type).append("/").append(taskId).toString();
    }


    /**
     *  获取任务类型前缀
     *  taskTypePrefix
     * /tasks/type/{type}/
     */
    public static String getTaskTypePrefix(Integer type){
        return new StringBuilder("/tasks/type/").append(type).append("/").toString();
    }
    /**
     * /tasks/groupid/{groupid}/{taskId}  groupid
     */
    public static String getGroupIdPrefix(String groupId){
        return new StringBuilder("/tasks/groupid/").append(groupId).append("/").toString();
    }

    /**
     *  /tasks/offset/{taskId}  offset
     * @param taskId
     * @return
     */
    public static String getOffset(String taskId){
        return new StringBuilder("/tasks/offset/").append(taskId).toString();
    }

    /**
     * /tasks/status/{currentstatus}/{taskid}  taskId
     */
    public static String getStatusTaskId(Integer status,String taskId){
        return new StringBuilder("/tasks/status/").append(status).append("/").append(taskId).toString();
    }

    /**
     * 根据状态查询当前状态的虽有任务
     * @param status
     * @return
     */
    public static  String getTaskListByStatusPrex(Integer status){
        return new StringBuilder("/tasks/status/").append(status).toString();
    }

    /**
     * /tasks/name/{taskname}  taskId
     */
    public  static String getNodeIdTaskName(String taskName){
        return new StringBuilder("/tasks/name/").append(taskName).toString();
    }

    /**
     *  /tasks/lock/{lockName}/{taskId}
     * @param lockName
     * @param taskId
     * @return
     */
    public static String getLockName(String lockName,String taskId){
        return new StringBuilder("/tasks/lock/").append(lockName).append("/").append(taskId).toString();
    }


    public static String getLockName(String lockName){
        return new StringBuilder("/tasks/lock/others/").append(lockName).toString();
    }


    /**
     * rdbVersion 相关
     */

    public static String getRdbVersionPrefix(){
        return new StringBuilder("/tasks/rdbversion/").toString();
    }

    /**
     * /tasks/rdbversion/{redisVersion}
     * @param redisVersion
     * @return
     */
    public static String getRdbVersionByRedisVersionPrefix(String redisVersion){
        return new StringBuilder("/tasks/rdbversion/").append(redisVersion).append("/").toString();
    }




    /**
     * /tasks/rdbversion/{redisVersion}/{rdbVersion}
     * @param redisVersion
     * @return
     */
    public static String getRdbVersionByRedisVersionAndRdbVerison(String redisVersion,Integer rdbVersion){
        return new StringBuilder("/tasks/rdbversion/").append(redisVersion).append("/").append(rdbVersion).toString();
    }


    /**
     * user 相关
     *  * /tasks/user/{username}  {"id":"","username":"","name":"","password":"","salt":""}
     */

    public static  String getUserByUserName(String username){
        return new StringBuilder("/tasks/user/").append(username).toString();
    }

    public static String getUserPrefix(){
        return "/tasks/user/";
    }


    /**
     * bigKey  /tasks/bigkey/{taskId}/{bigKey}
     */


    public static String getBigKeyPrefix(){
        return "/tasks/bigkey/";
    }

    public static String getBigKeyByTaskIdAndId(String taskId,String id){
        return new StringBuilder("/tasks/bigkey/").append(taskId).append("/").append(id).toString();
    }

    /**
     * taskId
     * @param taskId
     * @return
     */
    public static String getBigKeyByTaskIdPrefix(String taskId){
        return new StringBuilder("/tasks/bigkey/").append(taskId).toString();
    }


    /**
     * Compensation 数据补偿记录
     *
     * /tasks/compensation/{taskId}/{compensationId}    {}
     * /tasks/compensation/{groupId}/{compensationId}   {"compensationId": 1,"taskId":"xxx"}
     */
    public static String getCompensationByTaskIdPrefix(String taskId){
        return new StringBuilder("/tasks/compensation/").append(taskId).toString();
    }


    public static String getCompensationByTaskId(String taskId,Integer compenId){
        return new StringBuilder("/tasks/compensation/").append(taskId).append("/").append(compenId).toString();
    }

    public static String getCompensationPrefix(){
        return new StringBuilder("/tasks/compensation/").toString();
    }

    public static String getCompensationByGroupIdPrefix(String groupId){
        return new StringBuilder("/tasks/compensation/").append(groupId).toString();
    }

    public static String getCompensationByGroupId(String groupId,Integer compenId){
        return new StringBuilder("/tasks/compensation/").append(groupId).append("/").append(compenId).toString();
    }

    /**
     * abandon
     * 抛弃key
     *
     *  * /tasks/abandon/{taskId}/{abandonId}     {"id":1,"taskId":"xxx","groupId":"xxx","command":"xxx","key":"xxx","value":"xxx","type":1,"ttl":1000,"exception":"xxx","result":"xxx","desc":"xxx","createTime":"xxx"}
     *  * /tasks/abandon/{groupId}/{abandonId}   {"abandonId": 1,"taskId":"xxx"}
     */

    public static String getAbandonCommandPrefix(){
        return "/tasks/abandon/";
    }

    public static String getAbandonCommandByTaskId(String taskId,Integer abandonId){
        return new StringBuilder("/tasks/abandon/").append(taskId).append("/").append(abandonId).toString();
    }

    public static String getAbandonCommandByTaskIdPrefix(String taskId){
        return new StringBuilder("/tasks/abandon/").append(taskId).append("/").toString();
    }


    public static String getAbandonCommandByGroupIdPrefix(String groupId){
        return new StringBuilder("/tasks/abandon/").append(groupId).append("/").toString();
    }


    public static String getAbandonCommandByGroupId(String groupId,Integer abandonId){
        return new StringBuilder("/tasks/abandon/").append(groupId).append("/").append(abandonId).toString();
    }

    /**
     * 全局id key
     * @param key
     * @return
     */
    public static String getIdKey(String key){
        return new StringBuilder("/tasks/id/").append(key).toString();
    }
}
