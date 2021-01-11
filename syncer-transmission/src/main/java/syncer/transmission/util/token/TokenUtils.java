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
