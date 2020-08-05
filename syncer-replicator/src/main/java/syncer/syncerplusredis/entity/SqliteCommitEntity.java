package syncer.syncerplusredis.entity;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description sqlite串行化
 * @Date 2020/7/23
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SqliteCommitEntity {
    private Integer type;
    private Object object;
    private String msg;
}
