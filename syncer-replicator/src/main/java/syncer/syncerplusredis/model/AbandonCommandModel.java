package syncer.syncerplusredis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author zhanenqiang
 * @Description 抛弃命令
 * @Date 2020/4/26
 */
@Data
@Builder
@AllArgsConstructor
public class AbandonCommandModel {
    private int id;
    private String taskId;
    private String groupId;
    private String command;
    private String key;
    private String value;
    private int type;
    private long ttl;
    private String exception;
    private String result;
    private String desc;
    private String createTime;

}
