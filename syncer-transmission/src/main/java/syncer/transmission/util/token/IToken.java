package syncer.transmission.util.token;

import syncer.transmission.model.UserModel;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/8
 */
public interface IToken {
   String  putTokenUser(UserModel user);
   String  putTokenUser(String token,UserModel user);
   boolean  delToken(String  token);

    /**
     * 删除过期的Token
     */
   void deleteExpiryToken();

    /**
     * 验证token
     * @param token
     * @return
     */
   Boolean checkToken(String token);

   UserModel getUser(String token);
}
