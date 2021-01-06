package syncer.transmission.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/25
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ListTaskParamDto {
    @NotBlank(message = "regulation不能为空")
    private String regulation;
    @Builder.Default
    private List<String> tasknames = new ArrayList<>();

    private String taskstatus;
    @Builder.Default
    private List<String> taskids = new ArrayList<>();
    @Builder.Default
    private List<String> groupIds = new ArrayList<>();
    @Builder.Default
    int currentPage = 1;
    @Builder.Default
    int pageSize = 10;
}
