package syncer.syncerpluswebapp.controller.v2.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.dao.UserMapper;
import syncer.syncerplusredis.model.User;
import syncer.syncerplusredis.model.UserModel;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerpluswebapp.util.TokenUtils;

import javax.servlet.http.HttpSession;
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
        return ResultMap.builder().code("1000").msg("m");
    }

    @RequestMapping(value = "/logout",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap logout() throws Exception {

        return ResultMap.builder().code("2000").msg("success");
    }

    @RequestMapping(value = "/logoutByToken",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap logout(String token) throws Exception {
        TokenUtils.delToken(token);
        return ResultMap.builder().code("2000").msg("success");
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
