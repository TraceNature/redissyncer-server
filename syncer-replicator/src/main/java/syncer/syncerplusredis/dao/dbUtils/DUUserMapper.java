package syncer.syncerplusredis.dao.dbUtils;

import syncer.syncerpluscommon.util.db.SqlUtils;
import syncer.syncerplusredis.dao.UserMapper;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.model.UserModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class DUUserMapper implements UserMapper {
    @Override
    public List<UserModel> selectAll() throws Exception {
        String sql="SELECT * FROM t_user";
        List<UserModel>data= SqlUtils.getForList(UserModel.class,sql);
        return data;
    }

    @Override
    public UserModel findUserById(int id) throws Exception {
        String sql="SELECT * FROM t_user WHERE id =? limit 1";
        Object[]objects={id};
        UserModel data=SqlUtils.get(UserModel.class,sql,objects);
        return data;
    }

    @Override
    public List<UserModel> findUserByUsername(String username) throws Exception {
        String sql="SELECT * FROM t_user WHERE username =?";
        Object[]objects={username};
        List<UserModel> data=SqlUtils.getForList(UserModel.class,sql,objects);
        return data;
    }

    @Override
    public boolean updateUserPasswordById(int id, String password) throws Exception {
        return false;
    }
}
