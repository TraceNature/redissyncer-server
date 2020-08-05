package syncer.syncerplusredis.dao.dbUtils;

import syncer.syncerpluscommon.util.Type2TypeUtils;
import syncer.syncerpluscommon.util.db.SqlUtils;
import syncer.syncerplusredis.dao.TaskOffsetMapper;
import syncer.syncerplusredis.entity.TaskOffsetEntity;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class DUTaskOffsetMapper implements TaskOffsetMapper {
    @Override
    public boolean insetTaskOffset(TaskOffsetEntity taskOffsetEntity) throws Exception {
        String sql="INSERT INTO t_task_offset(taskId,offset,replId) VALUES(?,?,?)";
        Object[]objects={taskOffsetEntity.getTaskId(),taskOffsetEntity.getOffset(),taskOffsetEntity.getReplId()};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateOffsetByTaskId(boolean taskId, long offset) throws Exception {
        String sql="UPDATE t_task_offset SET offset=? WHERE taskId=?";
        Object[]objects={offset,taskId};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateReplIdByTaskId(boolean taskId, String replId) throws Exception {
        String sql="UPDATE t_task_offset SET replId=? WHERE taskId=?";
        Object[]objects={replId,taskId};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean updateOffsetAndReplIdByTaskId(boolean taskId, String replId, long offset) throws Exception {
        String sql="UPDATE t_task_offset SET replId=?,offset=? WHERE taskId=?";
        Object[]objects={replId,offset,taskId};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public int delOffsetEntityByTaskId(String taskId) throws Exception {
        String sql="DELETE FROM t_task_offset WHERE taskId=?";
        Object[]objects={taskId};
        return SqlUtils.update(sql,objects);
    }

    @Override
    public int delOffsetEntityByGroupId(String groupId) throws Exception {
        return 1;
    }


}
