package syncer.syncerplusredis.entity.dto.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "停止任务", description = "停止任务")
public class TaskMsgDto {

    //    @NotEmpty(message = "taskids不能为空")
    @ApiModelProperty(value = "任务id")
    private List<String> taskids;
    @ApiModelProperty(value = "任务组id")
    private List<String> groupIds;
//    @NotEmpty(message = "taskid不能为空")
//    private String taskid;

}
