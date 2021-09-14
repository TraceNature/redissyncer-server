package syncer.transmission.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
/**
 *  用于etcd上报
 *  | /tasks/keytime/|{taskId} |{"lastKeyCommitTime": 1,"lastKeyUpdateTime": 1,"taskId":"xxx","groupId":"xxx"}
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@Slf4j
@AllArgsConstructor
public class LastKeyTimeModel {
    private String groupId;
    private String taskId;
    /**
     * 当前Key的最后一次更新时间（数据流入）
     */
    private volatile  long lastKeyUpdateTime=0L;

    /**
     * 当前Key的最后一次pipeline提交时间（数据流出）
     */
    private volatile long lastKeyCommitTime=0L;
}
