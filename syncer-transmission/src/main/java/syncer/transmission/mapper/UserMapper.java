// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import syncer.transmission.model.UserModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/24
 */
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
    boolean updateUserPasswordById(@Param("id") int id, @Param("password") String password)throws Exception;
}
