// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.webapp.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/28
 */
@Getter
@Setter
@ApiModel(value = "任务描述", description = "任务描述")
public class RemoveTaskParam {
    @ApiModelProperty(value = "任务id")
    private List<String> taskids;
    @ApiModelProperty(value = "任务组id")
    private List<String> groupIds;
}
