package syncer.transmission.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/8
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserLoginDto {
    private String username;
    private String name;
    private String token;
}
