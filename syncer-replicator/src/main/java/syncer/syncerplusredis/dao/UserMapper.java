package syncer.syncerplusredis.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import syncer.syncerplusredis.model.UserModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/24
 */
//@Component
@Mapper
public interface UserMapper {
    // 根据 ID 查询
    @Select("SELECT * FROM t_user")
    List<UserModel> selectAll()throws Exception;
    @Select("SELECT * FROM t_user WHERE id =#{id}")
    UserModel findUserById(@Param("id") int id)throws Exception;
    @Select("SELECT * FROM t_user WHERE username =#{username}")
    List<UserModel> findUserByUsername(@Param("username") String username)throws Exception;

    @Update("UPDATE t_user SET password=#{password} WHERE id =#{id}")
    boolean updateUserPasswordById(@Param("id") int id,@Param("password")String password)throws Exception;
}
