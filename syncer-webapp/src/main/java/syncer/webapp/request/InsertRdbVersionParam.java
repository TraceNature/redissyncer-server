package syncer.webapp.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/28
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsertRdbVersionParam {
    /**
     * redis版本
     */
    @NotBlank(message = "redis_version不能为空")
    private String redis_version;
    /**
     * rdb版本
     */
//    @NotBlank(message = "rdb_version不能为空")
    private Integer rdb_version;
}
