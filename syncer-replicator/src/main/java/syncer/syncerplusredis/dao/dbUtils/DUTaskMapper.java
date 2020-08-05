package syncer.syncerplusredis.dao.dbUtils;

import syncer.syncerpluscommon.util.Type2TypeUtils;
import syncer.syncerpluscommon.util.db.SqlUtils;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.model.TaskModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class DUTaskMapper implements TaskMapper {
    @Override
    public List<TaskModel> selectAll() throws Exception {
        String sql="SELECT * FROM t_task";
        List<TaskModel>data= SqlUtils.getForList(TaskModel.class,sql);
        return data;
    }

    @Override
    public TaskModel findTaskById(String id) throws Exception {
        String sql="SELECT * FROM t_task WHERE id =? limit 1";
        Object[]objects={id};
        TaskModel data=SqlUtils.get(TaskModel.class,sql,objects);
        return data;

    }

    @Override
    public int countItem() throws Exception {
        String sql="select count(*) from t_task limit 1";
        return SqlUtils.get(Integer.class,sql);
    }

    @Override
    public List<TaskModel> findTaskBytaskName(String taskName) throws Exception {
        String sql="SELECT * FROM t_task WHERE taskName =?";
        Object[]objects={taskName};
        List<TaskModel>data= SqlUtils.getForList(TaskModel.class,sql,objects);
        return data;
    }

    @Override
    public List<TaskModel> findTaskBytaskMd5(String md5) throws Exception {
        String sql="SELECT * FROM t_task WHERE md5 =?";
        Object[]objects={md5};
        List<TaskModel>data= SqlUtils.getForList(TaskModel.class,sql,objects);
        return data;
    }

    @Override
    public List<TaskModel> findTaskBytaskStatus(Integer status) throws Exception {
        String sql="SELECT * FROM t_task WHERE status =?";
        Object[]objects={status};
        List<TaskModel>data= SqlUtils.getForList(TaskModel.class,sql,objects);
        return data;

    }

    @Override
    public List<TaskModel> findTaskByGroupId(String groupId) throws Exception {
        String sql="SELECT * FROM t_task WHERE groupId =?";
        Object[]objects={groupId};
        List<TaskModel>data= SqlUtils.getForList(TaskModel.class,sql,objects);
        return data;
    }

    @Override
    public boolean insertTask(TaskModel taskModel) throws Exception {

        String sql="INSERT INTO t_task(id, groupId,taskName,sourceRedisAddress,sourcePassword,targetRedisAddress,targetPassword,autostart,afresh,batchSize,tasktype,offsetPlace,taskMsg,offset,status,redisVersion,rdbVersion,sourceRedisType,targetRedisType,syncType,dbMapper,md5,replId,fileAddress,sourceAcl,targetAcl,sourceUserName,targetUserName) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Object[]objects={taskModel.getId(),
                taskModel.getGroupId(),
                taskModel.getTaskName(),
                taskModel.getSourceRedisAddress(),
                taskModel.getSourcePassword(),
                taskModel.getTargetRedisAddress(),
                taskModel.getTargetPassword(),
                taskModel.isAutostart(),
                taskModel.isAfresh(),
                taskModel.getBatchSize(),
                taskModel.getTasktype(),
                taskModel.getOffsetPlace(),
                taskModel.getTaskMsg(),
                taskModel.getOffset(),
                taskModel.getStatus(),
                taskModel.getRedisVersion(),
                taskModel.getRdbVersion(),
                taskModel.getSourceRedisType(),
                taskModel.getTargetRedisType(),
                taskModel.getSyncType(),
                taskModel.getDbMapper(),
                taskModel.getMd5(),
                taskModel.getReplId(),
                taskModel.getFileAddress(),
                taskModel.isSourceAcl(),
                taskModel.isTargetAcl(),
                taskModel.getSourceUserName(),
                taskModel.getTargetUserName()
        };
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public int insertTaskList(List<TaskModel> taskModelList) {
        String sql="INSERT INTO t_task(id, groupId,taskName,sourceRedisAddress,sourcePassword,targetRedisAddress,targetPassword,autostart,afresh,batchSize,tasktype,offsetPlace,taskMsg,offset,status,redisVersion,rdbVersion,sourceRedisType,targetRedisType,syncType,dbMapper,md5,replId,fileAddress,sourceAcl,targetAcl,sourceUserName,targetUserName) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        List<Object[]>objects=new ArrayList<>();
        for (TaskModel taskModel:taskModelList
             ) {
            if(taskModel!=null){
                Object[]object={taskModel.getId(),
                        taskModel.getGroupId(),
                        taskModel.getTaskName(),
                        taskModel.getSourceRedisAddress(),
                        taskModel.getSourcePassword(),
                        taskModel.getTargetRedisAddress(),
                        taskModel.getTargetPassword(),
                        taskModel.isAutostart(),
                        taskModel.isAfresh(),
                        taskModel.getBatchSize(),
                        taskModel.getTasktype(),
                        taskModel.getOffsetPlace(),
                        taskModel.getTaskMsg(),
                        taskModel.getOffset(),
                        taskModel.getStatus(),
                        taskModel.getRedisVersion(),
                        taskModel.getRdbVersion(),
                        taskModel.getSourceRedisType(),
                        taskModel.getTargetRedisType(),
                        taskModel.getSyncType(),
                        taskModel.getDbMapper(),
                        taskModel.getMd5(),
                        taskModel.getReplId(),
                        taskModel.getFileAddress(),
                        taskModel.isSourceAcl(),
                        taskModel.isTargetAcl(),
                        taskModel.getSourceUserName(),
                        taskModel.getTargetUserName()
                };
                objects.add(object);
            }
        }


        try {
            return SqlUtils.updateBatch(sql,objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean deleteTaskById(String id) throws Exception {
        String sql="DELETE FROM t_task WHERE id=?";
        Object[]objects={id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public int deleteTasksByGroupId(String groupId) throws Exception {
        String sql="DELETE FROM t_task WHERE groupId=?";
        Object[]objects={groupId};
        return SqlUtils.update(sql,objects);
    }

    @Override
    public int deleteAllTask() {
        String sql="DELETE FROM t_task";
        return SqlUtils.update(sql);
    }

    @Override
    public boolean updateTask(TaskModel taskModel) throws Exception {
        String sql="UPDATE t_task SET groupId=? ,taskName=?,sourceRedisAddress=?,sourcePassword=?,targetRedisAddress=?,targetPassword=?,autostart=?,afresh=?,batchSize=?,tasktype=?,offsetPlace=?,taskMsg=?,offset=?,status=?,redisVersion=?,rdbVersion=?,targetRedisType=?,sourceRedisType=?,syncType=?,dbMapper=?,updateTime=(datetime('now', 'localtime')) ,md5=?,sourceAcl=?,targetAcl=?,sourceUserName=?,targetUserName=? WHERE id=?";

        Object[]objects={taskModel.getGroupId(),
                taskModel.getTaskName(),
                taskModel.getSourceRedisAddress(),
                taskModel.getSourcePassword(),
                taskModel.getTargetRedisAddress(),
                taskModel.getTargetPassword(),
                taskModel.isAutostart(),
                taskModel.isAfresh(),
                taskModel.getBatchSize(),
                taskModel.getTasktype(),
                taskModel.getOffsetPlace(),
                taskModel.getTaskMsg(),
                taskModel.getOffset(),
                taskModel.getStatus(),
                taskModel.getRedisVersion(),
                taskModel.getRdbVersion(),
                taskModel.getTargetRedisType(),
                taskModel.getSourceRedisType(),
                taskModel.getSyncType(),
                taskModel.getDbMapper(),
                taskModel.getMd5(),
                taskModel.isSourceAcl(),
                taskModel.isTargetAcl(),
                taskModel.getSourceUserName(),
                taskModel.getTargetUserName(),
                taskModel.getTaskId()

        };
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateTaskStatusById(String id, int status) throws Exception {
        String sql="UPDATE t_task SET status=?,updateTime=(datetime('now', 'localtime')) WHERE id=?";
        Object[]objects={status,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateTaskStausByGroupId(String groupId, int status) throws Exception {
        String sql="UPDATE t_task SET status=?,updateTime=(datetime('now', 'localtime')) WHERE groupId=?";
        Object[]objects={status,groupId};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateTaskOffsetById(String id, long offset) throws Exception {
        String sql="UPDATE t_task SET offset=?,updateTime=(datetime('now', 'localtime')) WHERE id=?";
        Object[]objects={offset,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateAfreshsetById(String id, boolean afresh) throws Exception {
        String sql="UPDATE t_task SET afresh=?,updateTime=(datetime('now', 'localtime')) WHERE id=?";
        Object[]objects={afresh,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateTaskMsgAndStatusById(Integer status, String taskMsg, String id) throws Exception {
        String sql="UPDATE t_task SET status=?,taskMsg=?,updateTime=(datetime('now', 'localtime')) WHERE id=?";
        Object[]objects={status,taskMsg,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateTaskMsgById(String taskMsg, String id) throws Exception {
        String sql="UPDATE t_task SET taskMsg=?,updateTime=(datetime('now', 'localtime')) WHERE id=?";
        Object[]objects={taskMsg,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateTime(String id) throws Exception {
        String sql="UPDATE t_task set updateTime=(datetime('now', 'localtime')) where id=?";
        Object[]objects={id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateOffset(String id, Long offset) throws Exception {
        String sql="UPDATE t_task set offset=? where id=?";
        Object[]objects={offset,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateOffsetAndReplId(String id, Long offset, String replId) throws Exception {
        String sql="UPDATE t_task set offset=?,replId=?  where id=?";
        Object[]objects={offset,replId,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateOffsetAndReplIdAndAllKey(String id, Long offset, String replId, String allKeyCount, String realKeyCount) throws Exception {
        String sql="UPDATE t_task set offset=?,replId=?,allKeyCount=?,realKeyCount=? where id=?";
        Object[]objects={offset,replId,allKeyCount,realKeyCount,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateDataAnalysis(String id, String dataAnalysis) throws Exception {
        String sql="UPDATE t_task set dataAnalysis=? where id=?";
        Object[]objects={dataAnalysis,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateRdbKeyCountById(String id, Long rdbKeyCount) throws Exception {
        String sql="UPDATE t_task set rdbKeyCount=? where id=?";
        Object[]objects={rdbKeyCount,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateRealKeyCountById(String id, Long realKeyCount) throws Exception {
        String sql="UPDATE t_task set realKeyCount=? where id=?";
        Object[]objects={realKeyCount,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateAllKeyCountById(String id, Long allKeyCount) throws Exception {
        String sql="UPDATE t_task set allKeyCount=? where id=?";
        Object[]objects={allKeyCount,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateKeyCountById(String id, Long rdbKeyCount, Long allKeyCount, Long realKeyCount) throws Exception {
        String sql="UPDATE t_task set  rdbKeyCount=?, allKeyCount=?,realKeyCount=? where id=?";
        Object[]objects={rdbKeyCount,allKeyCount,realKeyCount,id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }
}
