package syncer.syncerplusredis.entity.dto.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ApiModel(value = "启动任务", description = "启动任务")
public class TaskStartMsgDto {
    @ApiModelProperty(value = "任务id", required = true, example = "DE034278589D47FAB92D3B3DCBC668D1")
    private String taskid;

    @ApiModelProperty(value = "任务组id",  example = "DE034278589D47FAB92D3B3DCBC668D1")
    private String groupId;

    @ApiModelProperty(value = "是否从头开始同步任务")
    @Builder.Default
    private boolean afresh = true;
}
