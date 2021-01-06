package syncer.transmission.util.token;

import syncer.transmission.model.UserModel;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/8
 */
public class TokenUtils {
    public static final IToken tokenManger=new MemoryToken();


    public static String putTokenUser(UserModel user) {
        return tokenManger.putTokenUser(user);
    }


    public static String putTokenUser(String token, UserModel user) {
        return tokenManger.putTokenUser(token,user);
    }


    public static boolean delToken(String token) {
        return tokenManger.delToken(token);
    }


    public static void deleteExpiryToken() {
        tokenManger.deleteExpiryToken();
    }


    public static Boolean checkToken(String token) {
        return tokenManger.checkToken(token);
    }


    public static UserModel getUser(String token) {
        return tokenManger.getUser(token);
    }
}
