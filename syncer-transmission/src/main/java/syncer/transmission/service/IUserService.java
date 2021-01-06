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
