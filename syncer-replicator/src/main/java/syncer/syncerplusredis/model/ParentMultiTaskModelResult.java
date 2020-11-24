package syncer.syncerplusredis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/11/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParentMultiTaskModelResult {

    /**
     * A 节点列表
     */
    private List<MultiTaskModelResult> redisNodeA;

    /**
     * B节点列表
     */
    private List<MultiTaskModelResult> redisNodeB;


    /**
     * taskId
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 自动启动
     */
    @Builder.Default
    private int  autostart=0;


    /**
     * 全局任务状态
     */
    private int status;
}
