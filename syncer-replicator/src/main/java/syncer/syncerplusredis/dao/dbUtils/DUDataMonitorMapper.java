package syncer.syncerplusredis.dao.dbUtils;

import syncer.syncerpluscommon.util.Type2TypeUtils;
import syncer.syncerpluscommon.util.db.SqlUtils;
import syncer.syncerplusredis.dao.DataMonitorMapper;
import syncer.syncerplusredis.model.DataMonitorModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class DUDataMonitorMapper implements DataMonitorMapper {
    @Override
    public List<DataMonitorModel> selectAll() throws Exception {
        String sql="SELECT * FROM t_data_monitoring";
        List<DataMonitorModel>data= SqlUtils.getForList(DataMonitorModel.class,sql);
        return data;
    }

    @Override
    public List<DataMonitorModel> findDataMonitorModelListByTaskId(String taskId) throws Exception {
        String sql="SELECT * FROM t_data_monitoring WHERE taskId =?";
        Object[]objects={taskId};
        List<DataMonitorModel>data= SqlUtils.getForList(DataMonitorModel.class,sql,objects);
        return data;
    }

    @Override
    public List<DataMonitorModel> findDataMonitorModelListByGroupId(String groupId) throws Exception {
        String sql="SELECT * FROM t_data_monitoring WHERE groupId =?";
        Object[]objects={groupId};
        List<DataMonitorModel>data= SqlUtils.getForList(DataMonitorModel.class,sql,objects);
        return data;
    }

    @Override
    public boolean insertDataMonitorModelModel(DataMonitorModel dataMonitorModel) throws Exception {
        String sql="INSERT INTO t_data_monitoring(allKeyCount,hashCount,stringCount,listCount,setCount,zSetCount,idempotentCount,dataCompensationCount,abandonCount,taskId,groupId ) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        Object[]objects={dataMonitorModel.getAllKeyCount(),
                dataMonitorModel.getHashCount(),
                dataMonitorModel.getStringCount(),
                dataMonitorModel.getListCount(),
                dataMonitorModel.getSetCount(),
                dataMonitorModel.getZSetCount(),
                dataMonitorModel.getIdempotentCount(),
                dataMonitorModel.getDataCompensationCount(),
                dataMonitorModel.getAbandonCount(),
                dataMonitorModel.getTaskId(),
                dataMonitorModel.getGroupId()
        };
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public void deleteDataMonitorModelById(String id) throws Exception {
        String sql="DELETE FROM t_data_monitoring WHERE id=?";
        Object[]objects={id};
        SqlUtils.update(sql,objects);
    }

    @Override
    public void deleteDataMonitorModelByTaskId(String taskId) throws Exception {
        String sql="DELETE FROM t_data_monitoring WHERE taskId=?";
        Object[]objects={taskId};
        SqlUtils.update(sql,objects);
    }

    @Override
    public void deleteDataMonitorModelByGroupId(String groupId) throws Exception {
        String sql="DELETE FROM t_data_monitoring WHERE groupId=?";
        Object[]objects={groupId};
        SqlUtils.update(sql,objects);
    }
}
