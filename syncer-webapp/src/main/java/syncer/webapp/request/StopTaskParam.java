package syncer.webapp.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/16
 */

@Getter
@Setter
@ApiModel(value = "任务描述", description = "任务描述")
public class StopTaskParam {
    @ApiModelProperty(value = "任务id")
    private List<String> taskids;
    @ApiModelProperty(value = "任务组id")
    private List<String> groupIds;
}
