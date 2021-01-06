package syncer.transmission.model;

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
public class TaskOffsetModel {
    private int id;
    private String taskId;
    private String replId;
    private long offset;
}
