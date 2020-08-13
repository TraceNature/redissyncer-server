package syncer.syncerplusredis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeUser {
    @NotBlank(message ="旧密码不能为空")
    private String password;
    @NotBlank(message ="新密码不能为空")
    private String newPassword;
}
