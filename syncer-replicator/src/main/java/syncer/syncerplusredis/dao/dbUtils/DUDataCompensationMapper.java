package syncer.syncerplusredis.dao.dbUtils;

import syncer.syncerpluscommon.util.Type2TypeUtils;
import syncer.syncerpluscommon.util.db.SqlUtils;
import syncer.syncerplusredis.dao.DataCompensationMapper;
import syncer.syncerplusredis.model.DataCompensationModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class DUDataCompensationMapper implements DataCompensationMapper {
    @Override
    public List<DataCompensationModel> selectAll() throws Exception {
        String sql="SELECT * FROM t_data_compensation";
        List<DataCompensationModel>data= SqlUtils.getForList(DataCompensationModel.class,sql);
        return data;
    }

    @Override
    public List<DataCompensationModel> findDataCompensationModelListByTaskId(String taskId) throws Exception {
        String sql="SELECT * FROM t_data_compensation WHERE taskId =?";
        Object[]objects={taskId};
        List<DataCompensationModel>data= SqlUtils.getForList(DataCompensationModel.class,sql,objects);
        return data;
    }

    @Override
    public List<DataCompensationModel> findDataCompensationModelListByGroupId(String groupId) throws Exception {
        String sql="SELECT * FROM t_data_compensation WHERE groupId =?";
        Object[]objects={groupId};
        List<DataCompensationModel>data= SqlUtils.getForList(DataCompensationModel.class,sql,objects);
        return data;
    }

    @Override
    public boolean insertDataCompensationModel(DataCompensationModel abandonCommandModel) throws Exception {
        String sql="INSERT INTO t_data_compensation(taskId,groupId,key,value,times,command) VALUES( ?,?,?,?,?,?)";
        Object[]objects={abandonCommandModel.getTaskId(),
                abandonCommandModel.getGroupId(),
                abandonCommandModel.getKey(),
                abandonCommandModel.getValue(),
                abandonCommandModel.getTimes(),
                abandonCommandModel.getCommand()};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public void deleteDataCompensationModelById(String id) throws Exception {
        String sql="DELETE FROM t_data_compensation WHERE id=?";
        Object[]objects={id};
       SqlUtils.update(sql,objects);
    }

    @Override
    public void deleteDataCompensationModelByTaskId(String taskId) throws Exception {
        String sql="DELETE FROM t_data_compensation WHERE taskId=?";
        Object[]objects={taskId};
        SqlUtils.update(sql,objects);
    }

    @Override
    public void deleteDataCompensationModelByGroupId(String groupId) throws Exception {
        String sql="DELETE FROM t_data_compensation WHERE groupId=?";
        Object[]objects={groupId};
        SqlUtils.update(sql,objects);
    }
}
