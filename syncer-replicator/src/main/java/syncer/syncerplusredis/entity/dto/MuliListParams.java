package syncer.syncerplusredis.entity.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 双向List查询
 *@Date 2020/11/16
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MuliListParams {
    @Builder.Default
    private List<String> groupIds=new ArrayList<>();

    private String taskId;

//    @NotBlank(message = "regulation不能为空")
    private String regulation;

    private String tasknames;

    private String taskstatus;

    @Builder.Default
    private int currentPage=1;
    @Builder.Default
    private int pageSize=10;
}
