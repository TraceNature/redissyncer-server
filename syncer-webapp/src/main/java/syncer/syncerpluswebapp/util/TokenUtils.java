package syncer.syncerpluswebapp.util;

import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.model.UserModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/24
 */
final public  class TokenUtils {

    public static long EXPIRY_TIME = 1000*60*30;//过期时间30分钟
    public static Map<String,Object> tokenMap = new ConcurrentHashMap<>();



    /**
     * 用userId添加token
     */
    public static String  putTokenMap(UserModel user){
        Map<String,Object> tokenInfo = new HashMap<>();
        String token= TemplateUtils.uuid();
        tokenInfo.put("token", token);
        tokenInfo.put("expiryTime",System.currentTimeMillis()+EXPIRY_TIME);
        tokenInfo.put("dbUser",user);
        tokenMap.put(token,tokenInfo);
        return token;
    }

    /**
     * 删除过期token
     */
    public static void deleteExpiryToken(){
        Iterator<Map.Entry<String, Object>> iterator = tokenMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> next = iterator.next();
            Map<String,Object> tokenInfo = (Map<String,Object>)next.getValue();
            if ((long)tokenInfo.get("expiryTime")<System.currentTimeMillis()){
                iterator.remove();
            }
        }
    }

    /**
     * 验证token(有userId)
     */
//    public static Boolean checkToken(String token,String userId){
//        //调用时遍历，删除已过期的token
//        deleteExpiryToken();
//
//        Iterator<Map.Entry<String, Object>> iterator = tokenMap.entrySet().iterator();
//        while (iterator.hasNext()){
//            Map.Entry<String, Object> next = iterator.next();
//            Map<String,Object> tokenInfo = (Map<String,Object>)next.getValue();
//            if (tokenInfo.get("token").equals(token)&&next.getKey().equals(userId)){
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * 验证token(无token)
     */
    public static Boolean checkToken(String token){
        //调用时遍历，删除已过期的token
        deleteExpiryToken();
        if(tokenMap.containsKey(token)){
            return true;
        }
        return false;
    }


    public static UserModel getUser(String token){
        //调用时遍历，删除已过期的token
        deleteExpiryToken();
        if(tokenMap.containsKey(token)){
            Map<String,Object> tokenInfo =(Map<String,Object>)tokenMap.get(token);
            return (UserModel) tokenInfo.get("dbUser");
        }
        return null;
    }
}
