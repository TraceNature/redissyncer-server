package syncer.syncerplusredis.model;

import lombok.*;

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
    private String redis_version;
    /**
     * rdb版本
     */
    private Integer rdb_version;

}
