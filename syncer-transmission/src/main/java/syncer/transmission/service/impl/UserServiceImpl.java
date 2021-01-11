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

package syncer.transmission.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syncer.transmission.mapper.UserMapper;
import syncer.transmission.model.UserModel;
import syncer.transmission.po.UserLoginDto;
import syncer.transmission.service.IUserService;
import syncer.transmission.util.token.TokenUtils;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/8
 */
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public UserLoginDto login(String username, String password) throws Exception {
        List<UserModel> userModelList=userMapper.findUserByUsername(username);

        if(userModelList!=null&&userModelList.size()>0){
            UserModel dbUser=userModelList.get(0);
            if(dbUser.getUsername().equals(username)&&dbUser.getPassword().equals(password)){
                String token= TokenUtils.putTokenUser(dbUser);
                return UserLoginDto.builder()
                        .name(dbUser.getName())
                        .username(dbUser.getUsername())
                        .token(token)
                        .build();
            }
        }
        return null;
    }

    @Override
    public boolean changeUserPassword(int id, String password) throws Exception {

        return userMapper.updateUserPasswordById(id,password);
    }
}
