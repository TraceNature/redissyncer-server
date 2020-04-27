package syncer.syncerplusredis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author zhanenqiang
 * @Description 数据补偿类
 * @Date 2020/4/26
 */
@Data
@AllArgsConstructor
@Builder
public class DataCompensationModel {
    private int id;
    private String taskId;
    private String groupId;
    private String command;
    private String value;
    private String key;
    private int times;
    private String createTime;
}
