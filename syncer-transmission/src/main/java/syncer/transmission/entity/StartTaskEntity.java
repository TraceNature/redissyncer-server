package syncer.transmission.entity;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/23
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StartTaskEntity {
    private String code;
    private String taskId;
    private String groupId;
    private String msg;
    private Object data;
}
