package syncer.syncerplusredis.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/10
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
