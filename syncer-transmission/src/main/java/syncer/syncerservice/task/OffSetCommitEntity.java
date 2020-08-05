package syncer.syncerservice.task;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/4
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
