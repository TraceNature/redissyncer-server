package syncer.transmission.model;


import lombok.*;

/**
 * @author zhanenqiang
 * @Description 用户表
 * @Date 2020/4/24
 */
@Getter@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {
    private int id;
    private String username;
    private String name;
    private String password;
    private String salt;
}
