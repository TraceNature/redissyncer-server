package syncer.replica.event;

import syncer.replica.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 监听订阅模式 STATUS消息通知 携带taskid
 * @Date 2020/11/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncerEvent {
    private String taskId;
    private Status status;
}
