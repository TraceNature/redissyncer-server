package syncer.syncerplusredis.dao.dbUtils;

import syncer.syncerpluscommon.util.Type2TypeUtils;
import syncer.syncerpluscommon.util.db.SqlUtils;
import syncer.syncerplusredis.dao.AbandonCommandMapper;
import syncer.syncerplusredis.model.AbandonCommandModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class DUAbandonCommandMapper implements AbandonCommandMapper {
    @Override
    public List<AbandonCommandModel> selectAll() throws Exception {
        String sql="SELECT * FROM t_abandon_command";
        List<AbandonCommandModel>data= SqlUtils.getForList(AbandonCommandModel.class,sql);
        return data;
    }

    @Override
    public List<AbandonCommandModel> findAbandonCommandListByTaskId(String taskId) throws Exception {
        String sql="SELECT * FROM t_abandon_command WHERE taskId =?";
        Object[]objects={taskId};
        List<AbandonCommandModel>data= SqlUtils.getForList(AbandonCommandModel.class,sql,objects);
        return data;
    }

    @Override
    public List<AbandonCommandModel> findAbandonCommandListByGroupId(String groupId) throws Exception {
        String sql="SELECT * FROM t_abandon_command WHERE groupId =?";
        Object[]objects={groupId};
        List<AbandonCommandModel>data= SqlUtils.getForList(AbandonCommandModel.class,sql,objects);
        return data;
    }

    @Override
    public boolean insertAbandonCommandModel(AbandonCommandModel abandonCommandModel) throws Exception {
        String sql="INSERT INTO t_abandon_command(taskId,groupId,command,key,value,type,ttl,exception,result,desc) VALUES(?,?,?,?,?,?,?,?,?,?)";
        Object[]objects={abandonCommandModel.getTaskId(),
                abandonCommandModel.getGroupId(),
                abandonCommandModel.getCommand(),
                abandonCommandModel.getKey(),
                abandonCommandModel.getValue(),
                abandonCommandModel.getType(),
                abandonCommandModel.getTtl(),
                abandonCommandModel.getException(),
                abandonCommandModel.getResult(),
                abandonCommandModel.getDesc()};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public boolean insertSimpleAbandonCommandModel(AbandonCommandModel abandonCommandModel) throws Exception {
        String sql="INSERT INTO t_abandon_command(taskId,command,key,type,exception,desc,ttl,groupId) VALUES(?,?,?,?,?,?,?,?)";
        Object[]objects={abandonCommandModel.getTaskId(),
                abandonCommandModel.getCommand(),
                abandonCommandModel.getKey(),
                abandonCommandModel.getType(),
                abandonCommandModel.getException(),
                abandonCommandModel.getDesc(),
                abandonCommandModel.getTtl(),
                abandonCommandModel.getGroupId()
               };
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public void deleteAbandonCommandModelById(String id) throws Exception {
        String sql="DELETE FROM t_abandon_command WHERE id=?";
        Object[]objects={id};
        SqlUtils.update(sql,objects);
    }

    @Override
    public void deleteAbandonCommandModelByTaskId(String taskId) throws Exception {
        String sql="DELETE FROM t_abandon_command WHERE taskId=?";
        Object[]objects={taskId};
        SqlUtils.update(sql,objects);
    }

    @Override
    public void deleteAbandonCommandModelByGroupId(String groupId) throws Exception {
        String sql="DELETE FROM t_abandon_command WHERE groupId=?";
        Object[]objects={groupId};
        SqlUtils.update(sql,objects);
    }
}
