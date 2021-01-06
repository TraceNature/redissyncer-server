package syncer.transmission.util.token;

import syncer.common.util.TemplateUtils;
import syncer.transmission.model.UserModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/8
 */
public class MemoryToken implements IToken{
    static long EXPIRY_TIME = 1000*60*60*24*7;
    static long MIN_TIME = 1000*60*60*6;
    Map<String,Object> tokenMap = new ConcurrentHashMap<>();
    static final String EXPIRETIME="expiryTime";
    static final String TOKEN_NAME="token";
    static final String DB_USER="dbUser";

    @Override
    public String putTokenUser(UserModel user) {
        Map<String,Object> tokenInfo = new HashMap<>();
        String token= TemplateUtils.uuid();
        tokenInfo.put(TOKEN_NAME, token);
        tokenInfo.put(EXPIRETIME,tokenTime(EXPIRY_TIME));
        tokenInfo.put(DB_USER,user);
        tokenMap.put(token,tokenInfo);
        return token;
    }

    @Override
    public String putTokenUser(String token, UserModel user) {
        Map<String,Object> tokenInfo = new HashMap<>();
        tokenInfo.put(TOKEN_NAME, token);
        tokenInfo.put(EXPIRETIME,tokenTime(EXPIRY_TIME));
        tokenInfo.put(DB_USER,user);
        tokenMap.put(token,tokenInfo);
        return token;
    }

    @Override
    public boolean delToken(String token) {
        if(tokenMap.containsKey(token)){
            tokenMap.remove(token);
        }
        return true;
    }

    @Override
    public void deleteExpiryToken() {
        Iterator<Map.Entry<String, Object>> iterator = tokenMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> next = iterator.next();
            Map<String,Object> tokenInfo = (Map<String,Object>)next.getValue();
            if ((long)tokenInfo.get(EXPIRETIME)<System.currentTimeMillis()){
                iterator.remove();
            }
        }
    }

    @Override
    public Boolean checkToken(String token) {
        //调用时遍历，删除已过期的token
        deleteExpiryToken();
        if(tokenMap.containsKey(token)){
            return true;
        }
        return false;
    }

    @Override
    public UserModel getUser(String token) {
        //调用时遍历，删除已过期的token
        deleteExpiryToken();
        if(tokenMap.containsKey(token)){
            Map<String,Object> tokenInfo =(Map<String,Object>)tokenMap.get(token);
            if((Long)tokenInfo.get(EXPIRETIME)-System.currentTimeMillis()<MIN_TIME){
                tokenInfo.put(EXPIRETIME,tokenTime(EXPIRY_TIME));
                tokenMap.put(token,tokenInfo);
            }
            return (UserModel) tokenInfo.get(DB_USER);
        }
        return null;
    }


    long tokenTime(long expireTime){
        return System.currentTimeMillis()+expireTime;
    }
}
