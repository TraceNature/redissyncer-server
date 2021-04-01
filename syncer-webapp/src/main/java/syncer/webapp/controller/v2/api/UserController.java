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

package syncer.webapp.controller.v2.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import syncer.common.entity.ResponseResult;
import syncer.transmission.model.UserModel;
import syncer.transmission.po.InfoDto;
import syncer.transmission.po.UserLoginDto;
import syncer.transmission.service.IUserService;
import syncer.transmission.util.token.TokenUtils;
import syncer.webapp.constants.ApiConstants;
import syncer.webapp.request.ChangeUserParam;
import syncer.webapp.request.UserParam;
import syncer.webapp.util.TokenNameUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/3
 */
@RestController
@Api( tags = "用户管理接口")
@Validated
public class UserController {
    @Autowired
    private IUserService userService;
    public static final int PASSWORD_LEN=6;

    @RequestMapping(value = "/login",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult<UserLoginDto> login(@RequestBody @Validated UserParam param) throws Exception {
        UserLoginDto userLoginDto=userService.login(param.getUsername(),param.getPassword());
        if(Objects.isNull(userLoginDto)){
            return ResponseResult.<UserLoginDto>builder()
                    .code(ApiConstants.ERROR_CODE)
                    .msg(ApiConstants.LOGIN_FAIL_MSG)
                    .build();
        }
        return ResponseResult.<UserLoginDto>builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.SUCCESS_MSG)
                .data(userLoginDto)
                .build();
    }


    @RequestMapping(value = "/logout",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult logout(HttpServletRequest request) throws Exception {
        TokenUtils.delToken(request.getHeader(TokenNameUtils.TOKEN_NAME));
        return ResponseResult
                .builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.SUCCESS_MSG)
                .build();
    }


    @RequestMapping(value = "/logoutByToken",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult logout(String token) throws Exception {
        TokenUtils.delToken(token);
        return ResponseResult.builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.SUCCESS_MSG)
                .build();
    }

    @RequestMapping(value = "/changePassword",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult changePassword(@RequestBody @Validated ChangeUserParam user, HttpServletRequest request) throws Exception {
        String token=request.getHeader(TokenNameUtils.TOKEN_NAME);
        UserModel tokenUser=TokenUtils.getUser(token);
        if(Objects.isNull(tokenUser)){
            return ResponseResult.builder()
                    .code(ApiConstants.ERROR_CODE)
                    .msg(ApiConstants.USER_NOT_EXIST)
                    .build();
        }

        if(user.getNewPassword().length()<PASSWORD_LEN){
            return ResponseResult.builder()
                    .code(ApiConstants.ERROR_CODE)
                    .msg(ApiConstants.PASSWORD_LEN_ERROR)
                    .build();
        }

        if(!tokenUser.getPassword().equals(user.getPassword())){
            return ResponseResult.builder()
                    .code(ApiConstants.ERROR_CODE)
                    .msg(ApiConstants.PASSWORD_OLD_ERROR)
                    .build();
        }
        if(tokenUser.getPassword().equals(user.getNewPassword())){
            return ResponseResult.builder()
                    .code(ApiConstants.ERROR_CODE)
                    .msg(ApiConstants.PASSWORD_REPEAT_ERROR)
                    .build();
        }

        boolean status=userService.changeUserPassword(tokenUser.getId(),user.getNewPassword());
        if(status){
            tokenUser.setPassword(user.getNewPassword());
            TokenUtils.putTokenUser(token,tokenUser);
            return ResponseResult.builder()
                    .code(ApiConstants.SUCCESS_CODE)
                    .msg(ApiConstants.PASSWORD_CHANGE_SUCCESS)
                    .build();
        }
        return ResponseResult.builder()
                .code(ApiConstants.ERROR_CODE)
                .msg(ApiConstants.PASSWORD_CHANGE_ERROR)
                .build();

    }

    @RequestMapping(value = "/info",method = {RequestMethod.POST,RequestMethod.GET})
    public ResponseResult<InfoDto> info(@RequestParam("token")String token) throws Exception {
        UserModel user= TokenUtils.getUser(token);
        if(user!=null){
            InfoDto infoDto=InfoDto.builder()
                    .roles(new String[]{"admin"})
                    .avatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                    .introduction("I am a super administrator")
                    .name(user.getName())
                    .build();

            return ResponseResult.<InfoDto>builder()
                    .code(ApiConstants.SUCCESS_CODE)
                    .msg(ApiConstants.SUCCESS_MSG)
                    .data(infoDto)
                    .build();
        }
        return ResponseResult.<InfoDto>builder()
                .code(ApiConstants.ERROR_CODE)
                .msg(ApiConstants.TOKEN_ERROR)
                .build();
    }
}
