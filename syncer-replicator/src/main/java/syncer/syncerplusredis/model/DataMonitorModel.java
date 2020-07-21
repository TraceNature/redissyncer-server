package syncer.syncerplusredis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author zhanenqiang
 * @Description 数据监控类
 * @Date 2020/4/26
 */
@Data
@AllArgsConstructor
@Builder
public class DataMonitorModel {
    private int id;
    private String taskId;
    private String groupId;
    private String createTime;
    private int allKeyCount;
    private int hashCount;
    private int stringCount;
    private int listCount;
    private int setCount;
    private int zSetCount;
    private int idempotentCount;
    /**
     * 数据补偿总数
     */
    private int dataCompensationCount;
    /**
     * 淘汰Key总数
     */
    private int abandonCount;
}
