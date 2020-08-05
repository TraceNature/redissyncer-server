package syncer.syncerplusredis.dao.dbUtils;

import syncer.syncerpluscommon.util.Type2TypeUtils;
import syncer.syncerpluscommon.util.db.SqlUtils;
import syncer.syncerplusredis.dao.BigKeyMapper;
import syncer.syncerplusredis.model.AbandonCommandModel;
import syncer.syncerplusredis.model.BigKeyModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class DUBigKeyMapper implements BigKeyMapper {
    @Override
    public List<BigKeyModel> findBigKeyCommandListByTaskId(String taskId) throws Exception {
        String sql="SELECT * FROM t_big_key WHERE taskId =?";
        Object[]objects={taskId};
        List<BigKeyModel>data= SqlUtils.getForList(BigKeyModel.class,sql,objects);
        return data;
    }

    @Override
    public boolean insertBigKeyCommandModel(BigKeyModel bigKeyModel) throws Exception {
        String sql="INSERT INTO t_big_key(taskId,command,command_type) VALUES(?,?,?)";
        Object[]objects={bigKeyModel.getTaskId(),
                bigKeyModel.getCommand(),
                bigKeyModel.getCommand_type()};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public void deleteBigKeyCommandModelById(String id) throws Exception {
        String sql="DELETE FROM t_big_key WHERE id=?";
        Object[]objects={id};
        SqlUtils.update(sql,objects);
    }

    @Override
    public void deleteBigKeyCommandModelByTaskId(String taskId) throws Exception {
        String sql="DELETE FROM t_big_key WHERE taskId=?";
        Object[]objects={taskId};
        SqlUtils.update(sql,objects);
    }

    @Override
    public void deleteBigKeyCommandModelByGroupId(String groupId) throws Exception {

    }
}
