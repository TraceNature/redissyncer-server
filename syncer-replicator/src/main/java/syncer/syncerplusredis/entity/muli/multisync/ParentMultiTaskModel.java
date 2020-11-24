package syncer.syncerplusredis.entity.muli.multisync;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltCreateTaskData;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 双活任务父线程
 * @Date 2020/9/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParentMultiTaskModel {

    /**
     * A 节点列表
     */
    private List<MultiTaskModel> redisNodeA;

    /**
     * B节点列表
     */
    private List<MultiTaskModel> redisNodeB;


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
     * 用于重复任务判断
     */
    private MuiltCreateTaskData data;


}
