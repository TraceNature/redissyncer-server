package syncer.transmission.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zhanenqiang
 * @Description RDB版本
 * @Date 2020/12/7
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RdbVersionModel {
    /**
     * id
     */

    private Integer id;
    /**
     * redis版本
     */
    @NotBlank(message = "redis_version不能为空")
    private String redis_version;
    /**
     * rdb版本
     */
    private Integer rdb_version;

}
