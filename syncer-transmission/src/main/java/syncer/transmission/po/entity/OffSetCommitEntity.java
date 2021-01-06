package syncer.transmission.po.entity;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/25
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OffSetCommitEntity {
    private String taskId;
    private String replId;
    private long offset;
}
