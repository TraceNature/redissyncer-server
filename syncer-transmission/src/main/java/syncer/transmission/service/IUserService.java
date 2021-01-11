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

package syncer.transmission.service;

import org.apache.ibatis.annotations.Param;
import syncer.transmission.po.UserLoginDto;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/8
 */
public interface IUserService {
    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    UserLoginDto login(String username,String password)throws Exception ;

    /**
     * 用户修改密码
     * @param id
     * @param password
     * @return
     * @throws Exception
     */
    boolean changeUserPassword(@Param("id") int id, @Param("password")String password)throws Exception;
}
