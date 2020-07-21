package syncer.syncerplusredis.model;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/6
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BigKeyModel {
    private int id;
    private String taskId;
    private String command;
    private String command_type;
}
