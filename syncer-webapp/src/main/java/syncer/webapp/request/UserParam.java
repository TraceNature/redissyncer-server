package syncer.webapp.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserParam {
    @NotBlank(message ="用户名不能为空")
    private String username;
    @NotBlank(message ="密码不能为空")
    private String password;

}
