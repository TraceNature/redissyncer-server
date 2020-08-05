package syncer.syncerplusredis.entity;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 任务OFFSET
 * @Date 2020/7/22
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class TaskOffsetEntity {
    private int id;
    private String taskId;
    private String replId;
    private long offset;
}
