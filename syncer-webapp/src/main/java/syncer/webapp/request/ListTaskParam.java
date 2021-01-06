package syncer.webapp.request;

import io.swagger.annotations.ApiModel;
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
@ApiModel(value = "任务查询", description = "任务查询")
public class ListTaskParam {
    @ApiModelProperty(name="任务查询规则",value = "任务查询规则", required = true,
            allowableValues = "all,bynames,byids,bygroupids,bystatus",notes = "任务查询规则",
            example = "bystatus")
    @NotBlank(message = "regulation不能为空")
    private String regulation;
    @ApiModelProperty(value = "任务名称", allowableValues = "regulation为bynames时填写")
    @Builder.Default
    private List<String> tasknames = new ArrayList<>();
    @ApiModelProperty(value = "任务状态",
            allowableValues = "CREATING,CREATED,RUN,STOP,PAUSE,BROKEN,RDBRUNING,COMMANDRUNING"
            , example = "COMMANDRUNING")
    private String taskstatus;
    @ApiModelProperty(value = "任务id", allowableValues = "regulation为byids时填写")
    @Builder.Default
    private List<String> taskids = new ArrayList<>();
    @ApiModelProperty(value = "任务组id", allowableValues = "regulation为bygroupids时填写")
    @Builder.Default
    private List<String> groupIds = new ArrayList<>();

    @Builder.Default
    int currentPage = 1;
    @Builder.Default
    int pageSize = 10;
}
