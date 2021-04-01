package syncer.replica.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import syncer.replica.status.TaskStatus;

/**
 * 任务状态变化事件
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncerTaskEvent {
    private String taskId;
    private String msg;
    private String replid;
    private Long offset;
    private TaskStatus event;
}