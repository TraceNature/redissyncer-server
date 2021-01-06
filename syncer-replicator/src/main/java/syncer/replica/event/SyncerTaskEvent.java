package syncer.replica.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import syncer.replica.entity.TaskStatusType;

/**
 * @author zhanenqiang
 * @Description 任务状态变化时间
 * @Date 2020/12/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncerTaskEvent {
    private SyncerEvent event;
    private String msg;
    private String replid;
    private Long offset;
    private TaskStatusType taskStatusType;
}
