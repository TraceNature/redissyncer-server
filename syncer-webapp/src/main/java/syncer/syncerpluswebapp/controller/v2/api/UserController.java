package syncer.syncerpluswebapp.controller.v2.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.ServletUtils;
import syncer.syncerpluscommon.util.common.TokenNameUtils;
import syncer.syncerplusredis.dao.UserMapper;
import syncer.syncerplusredis.model.ChangeUser;
import syncer.syncerplusredis.model.User;
import syncer.syncerplusredis.model.UserModel;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerpluswebapp.util.TokenUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/24
 */
@RestController
public class UserController {
    @Autowired
    private UserMapper userMapper;
    @RequestMapping(value = "/login",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap login(@RequestBody @Validated User user) throws Exception {
        List<UserModel> userModelList= SqliteOPUtils.findUserByUsername(user.getUsername());
        if(userModelList!=null&&userModelList.size()>0){
            UserModel dbUser=userModelList.get(0);
            Map<String,String> map=new HashMap<>();
            if(dbUser.getUsername().equals(user.getUsername())&&dbUser.getPassword().equals(user.getPassword())){
                map.put("username",dbUser.getUsername());
                map.put("name",dbUser.getName());
                String token=TokenUtils.putTokenMap(dbUser);
                map.put("token",token);
                return ResultMap.builder().code("2000").msg("success").data(map);
            }
        }
        return ResultMap.builder().code("1000").msg("登陆失败，请检查账号密码是否正确");
    }

    @RequestMapping(value = "/logout",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap logout() throws Exception {
        TokenUtils.delToken(ServletUtils.request().getHeader(TokenNameUtils.TOKEN_NAME));

        return ResultMap.builder().code("2000").msg("success");
    }

    @RequestMapping(value = "/logoutByToken",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap logout(String token) throws Exception {
        TokenUtils.delToken(token);
        return ResultMap.builder().code("2000").msg("success");
    }

    @RequestMapping(value = "/changePassword",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap changePassword(@RequestBody @Validated ChangeUser user, HttpServletRequest request) throws Exception {
        String token=request.getHeader(TokenNameUtils.TOKEN_NAME);
        System.out.println(token);
        UserModel tokenUser=TokenUtils.getUser(token);
        if(null==tokenUser){
            return ResultMap.builder().code("1000").msg("登陆用户不存在[请检查Token]");
        }
        if(user.getNewPassword().length()<6){
            return ResultMap.builder().code("1000").msg("新密码长度必须大于5位");
        }

        if(!tokenUser.getPassword().equals(user.getPassword())){
            return ResultMap.builder().code("1000").msg("旧密码不正确");
        }
         if(tokenUser.getPassword().equals(user.getNewPassword())){
            return ResultMap.builder().code("1000").msg("新密码不能和旧密码重复");
        }

        boolean status=userMapper.updateUserPasswordById(tokenUser.getId(),user.getNewPassword());
         if(status){
             tokenUser.setPassword(user.getNewPassword());
             TokenUtils.putTokenMap(token,tokenUser);
             return ResultMap.builder().code("2000").msg("修改密码成功");
         }

        return ResultMap.builder().code("1000").msg("修改密码失败");

    }

    @RequestMapping(value = "/info",method = {RequestMethod.POST,RequestMethod.GET})
    public ResultMap info(@RequestParam("token")String token) throws Exception {
        UserModel user= TokenUtils.getUser(token);
        if(user!=null){
            Map<String,Object>map=new HashMap<>();
            map.put("roles",new String[]{"admin"});
            map.put("introduction","I am a super administrator");
            map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
            map.put("name",user.getName());
            return ResultMap.builder().code("2000").msg("success").data(map);
        }

        return ResultMap.builder().code("1000").msg("请检查token");
    }
}
